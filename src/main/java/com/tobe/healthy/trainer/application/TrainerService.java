package com.tobe.healthy.trainer.application;

import static com.tobe.healthy.common.Utils.*;
import static com.tobe.healthy.common.error.ErrorCode.*;
import static com.tobe.healthy.member.domain.MemberType.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tobe.healthy.common.Utils;
import com.tobe.healthy.common.error.CustomException;
import com.tobe.healthy.common.redis.RedisKeyPrefix;
import com.tobe.healthy.common.redis.RedisService;
import com.tobe.healthy.course.application.CourseService;
import com.tobe.healthy.course.presentation.dto.CourseDto;
import com.tobe.healthy.course.presentation.dto.in.CourseAddCommand;
import com.tobe.healthy.diet.application.DietService;
import com.tobe.healthy.diet.presentation.dto.DietDto;
import com.tobe.healthy.gym.presentation.dto.out.GymDto;
import com.tobe.healthy.member.presentation.dto.MemberDto;
import com.tobe.healthy.member.repository.dto.MemberDetailResult;
import com.tobe.healthy.member.repository.dto.MemberInTeamResult;
import com.tobe.healthy.member.domain.Member;
import com.tobe.healthy.member.domain.NonMember;
import com.tobe.healthy.member.repository.MemberRepository;
import com.tobe.healthy.member.repository.NonMemberRepository;
import com.tobe.healthy.point.presentation.dto.out.PointDto;
import com.tobe.healthy.point.presentation.dto.out.RankDto;
import com.tobe.healthy.point.repository.PointRepository;
import com.tobe.healthy.schedule.repository.waiting.ScheduleWaitingRepository;
import com.tobe.healthy.trainer.presentation.dto.TrainerMemberMappingDto;
import com.tobe.healthy.trainer.presentation.dto.in.MemberInviteCommand;
import com.tobe.healthy.trainer.presentation.dto.in.MemberLessonCommand;
import com.tobe.healthy.trainer.presentation.dto.out.MemberInviteResultCommand;
import com.tobe.healthy.trainer.domain.TrainerMemberMapping;
import com.tobe.healthy.trainer.respository.TrainerMemberMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TrainerService {

	private final RedisService redisService;
	private final MemberRepository memberRepository;
	private final TrainerMemberMappingRepository mappingRepository;
	private final DietService dietService;
	private final CourseService courseService;
	private final ScheduleWaitingRepository scheduleWaitingRepository;
	private final PointRepository pointRepository;
	private final NonMemberRepository nonMemberRepository;

	public TrainerMemberMappingDto addStudentOfTrainer(Long trainerId, Long memberId, MemberLessonCommand command) {
		TrainerMemberMappingDto mappingDto = mappingMemberAndTrainer(trainerId, memberId);
		courseService.addCourse(trainerId, CourseAddCommand.create(memberId, command.lessonCnt()));
		return mappingDto;
	}

	public TrainerMemberMappingDto mappingMemberAndTrainer(Long trainerId, Long memberId) {
		Member trainer = memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainerId, TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));
		Member member = memberRepository.findByIdAndMemberTypeAndDelYnFalse(memberId, STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		mappingRepository.findByTrainerIdAndMemberId(trainerId, memberId)
			.ifPresent(i -> {
				throw new CustomException(MEMBER_ALREADY_MAPPED);
			});

		mappingRepository.deleteByMemberId(memberId);
		mappingRepository.flush();
		TrainerMemberMapping mapping = TrainerMemberMapping.create(trainer, member);
		mappingRepository.save(mapping);
		member.registerGym(trainer.getGym());
		log.info("[학생 매핑] trainer: {}, member: {}, mapping{}", trainer, member, mapping);
		return TrainerMemberMappingDto.from(mapping);
	}

	public void addStudentOfTrainerByNonmember(Long trainerId, Member nonmember, MemberLessonCommand command) {
		Member trainer = memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainerId, TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));

		//트레이너 매핑
		Long memberId = nonmember.getId();
		mappingRepository.deleteByMemberId(memberId);
		mappingRepository.flush();
		TrainerMemberMapping mapping = TrainerMemberMapping.create(trainer, nonmember);
		mappingRepository.save(mapping);
		nonmember.registerGym(trainer.getGym());

		//수강권 등록
		courseService.addCourseByNonmember(trainerId, CourseAddCommand.create(memberId, command.lessonCnt()),
			nonmember);
	}

	public MemberInviteResultCommand inviteMember(MemberInviteCommand command, Member trainer) {
		memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainer.getId(), TRAINER)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		String name = command.name();
		validateName(name);
		int lessonCnt = command.lessonCnt();
		if (lessonCnt < 1)
			throw new CustomException(LESSON_CNT_NOT_VALID);
		if (500 < lessonCnt)
			throw new CustomException(LESSON_CNT_MAX);

		String uuid = System.currentTimeMillis() + "-" + UUID.randomUUID();
		String invitationKey = RedisKeyPrefix.INVITATION.getDescription() + uuid;
		String invitationLink = WEB_BASE_URL + "/invite?type={type}&uuid={uuid}"
			.replace("{type}", STUDENT.getCode().toLowerCase())
			.replace("{uuid}", uuid);

		Map<String, String> invitedMapping = new HashMap<>() {{
			put("trainerId", trainer.getId().toString());
			put("name", name);
			put("lessonCnt", String.valueOf(lessonCnt));
		}};
		redisService.setValuesWithTimeout(invitationKey, JSONObject.toJSONString(invitedMapping), ONE_DAY); // 1days
		MemberInviteResultCommand response = new MemberInviteResultCommand(uuid, invitationLink);
		log.info("[학생 초대] trainer: {}, request: {}, response{}", trainer, command, response);
		return response;
	}

	public MemberInviteResultCommand inviteNonmember(MemberInviteCommand command, Member trainer) {
		memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainer.getId(), TRAINER)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		String name = command.name();
		validateName(name);
		int lessonCnt = command.lessonCnt();
		if (lessonCnt < 1)
			throw new CustomException(LESSON_CNT_NOT_VALID);
		if (500 < lessonCnt)
			throw new CustomException(LESSON_CNT_MAX);

		String uuid = System.currentTimeMillis() + "-" + UUID.randomUUID();
		String invitationLink = WEB_BASE_URL + "/invite?type={type}&uuid={uuid}"
			.replace("{type}", STUDENT.getCode().toLowerCase())
			.replace("{uuid}", uuid);

		//미가입 회원 DB 저장
		Member member = Member.join(null, null, name, STUDENT, null);
		memberRepository.save(member);

		NonMember nonMember = NonMember.create(member, invitationLink, name, trainer.getId(), lessonCnt);
		nonMemberRepository.save(nonMember);

		//트레이너 매핑 & 수강권 등록
		addStudentOfTrainerByNonmember(trainer.getId(), member, new MemberLessonCommand(command.lessonCnt()));
		MemberInviteResultCommand response = new MemberInviteResultCommand(uuid, invitationLink);
		log.info("[미가입 학생 직접 등록] trainer: {}, request: {}, response{}", trainer, command, response);
		return response;
	}

	private void validateName(String name) {
		if (Utils.validateNameLength(name)) {
			throw new CustomException(MEMBER_NAME_LENGTH_NOT_VALID);
		}

		if (Utils.validateNameFormat(name)) {
			throw new CustomException(MEMBER_NAME_NOT_VALID);
		}
	}

	public List<MemberInTeamResult> findAllMyMemberInTeam(Long trainerId, String searchValue, String sortValue,
		Pageable pageable) {
		List<MemberInTeamResult> members = memberRepository.findAllMyMemberInTeam(trainerId, searchValue, sortValue,
			pageable);
		return members.isEmpty() ? null : members;
	}

	public List<MemberDto> findAllUnattachedMembers(Member trainer, String searchValue, String sortValue,
		Pageable pageable) {
		Page<Member> members = memberRepository.findAllUnattachedMembers(trainer.getGym().getId(), searchValue,
			sortValue, pageable);
		List<MemberDto> memberDtos = members.stream().map(MemberDto::from).collect(Collectors.toList());
		return memberDtos.isEmpty() ? null : memberDtos;
	}

	public MemberDetailResult getMemberOfTrainer(Member trainer, Long memberId) {
		memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainer.getId(), TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));
		Member member = memberRepository.findByIdAndMemberTypeAndDelYnFalse(memberId, STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		//식단
		DietDto diet = dietService.getTodayDiet(memberId);
		MemberDetailResult result = memberRepository.getMemberOfTrainer(memberId);

		//수강권
		CourseDto course = courseService.getNowUsingCourse(memberId);

		//포인트
		String yyyyMM = getNowMonth();
		int monthPoint = pointRepository.getPointOfSearchMonth(memberId, yyyyMM);
		int totalPoint = pointRepository.getTotalPoint(memberId, yyyyMM);
		PointDto point = PointDto.create(yyyyMM, monthPoint, totalPoint);

		//트레이너 매핑 여부
		TrainerMemberMapping mapping = mappingRepository.findTop1ByMemberIdOrderByCreatedAtDesc(memberId).orElse(null);

		//랭킹
		long totalMemberCnt = mappingRepository.countByTrainerId(trainer.getId());
		RankDto rank = RankDto.create(mapping.getRanking(), mapping.getLastMonthRanking(), (int)totalMemberCnt);

		//헬스장 정보
		GymDto gym = member.getGym() == null ? null : GymDto.from(member.getGym());
		return result.withDetails(diet, course, point, rank, gym);
	}

	private String getNowMonth() {
		return LocalDate.now().toString().substring(0, 7);
	}

	public void deleteStudentOfTrainer(Member trainer, Long memberId) {
		Member member = memberRepository.findByIdAndMemberTypeAndDelYnFalse(memberId, STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		CourseDto courseDto = courseService.getNowUsingCourse(memberId);

		//수강권 횟수가 남아있는 경우 삭제 불가
		if (courseDto != null && isRemainLessonCnt(courseDto)) {
			throw new CustomException(COURSE_ALREADY_EXISTS);
		}
		//대기내역 삭제
		scheduleWaitingRepository.deleteByMemberId(memberId);
		mappingRepository.deleteByTrainerIdAndMemberId(trainer.getId(), member.getId());
		log.info("[학생 삭제] trainer: {}, member: {}", trainer, member);
	}

	private boolean isRemainLessonCnt(CourseDto courseDto) {
		return courseDto.completedLessonCnt() != courseDto.totalLessonCnt();
	}

	public void refundStudentOfTrainer(Member trainer, Long memberId) {
		Member member = memberRepository.findByIdAndMemberTypeAndDelYnFalse(memberId, STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		CourseDto courseDto = courseService.getNowUsingCourse(memberId);
		if (courseDto != null) {
			courseService.deleteCourseAndCancelReservation(trainer.getId(), courseDto.courseId());
		}
		mappingRepository.deleteByTrainerIdAndMemberId(trainer.getId(), member.getId());
		log.info("[학생 환불] trainer: {}, member: {}", trainer, member);
	}

}
