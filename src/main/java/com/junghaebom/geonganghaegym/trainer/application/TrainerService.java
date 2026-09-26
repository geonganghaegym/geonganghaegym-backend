package com.junghaebom.geonganghaegym.trainer.application;

import static com.junghaebom.geonganghaegym.common.Utils.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.Utils;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;
import com.junghaebom.geonganghaegym.course.presentation.dto.in.CourseAddCommand;
import com.junghaebom.geonganghaegym.diet.application.DietService;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietDto;
import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymDto;
import com.junghaebom.geonganghaegym.member.presentation.dto.MemberDto;
import com.junghaebom.geonganghaegym.member.repository.dto.MemberDetailResult;
import com.junghaebom.geonganghaegym.member.repository.dto.MemberInTeamResult;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.NonMember;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.member.repository.NonMemberRepository;
import com.junghaebom.geonganghaegym.point.presentation.dto.out.PointDto;
import com.junghaebom.geonganghaegym.point.presentation.dto.out.RankDto;
import com.junghaebom.geonganghaegym.point.repository.PointRepository;
import com.junghaebom.geonganghaegym.schedule.repository.waiting.ScheduleWaitingRepository;
import com.junghaebom.geonganghaegym.trainer.presentation.dto.TrainerMemberMappingDto;
import com.junghaebom.geonganghaegym.trainer.presentation.dto.in.MemberInviteCommand;
import com.junghaebom.geonganghaegym.trainer.presentation.dto.in.MemberLessonCommand;
import com.junghaebom.geonganghaegym.trainer.presentation.dto.out.MemberInviteResultCommand;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TrainerService {

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
