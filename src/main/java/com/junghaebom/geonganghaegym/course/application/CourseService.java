package com.junghaebom.geonganghaegym.course.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.course.domain.CourseStatus.*;
import static com.junghaebom.geonganghaegym.course.domain.CourseHistoryType.*;
import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;
import static com.junghaebom.geonganghaegym.point.domain.Calculation.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;
import com.junghaebom.geonganghaegym.course.presentation.dto.CourseHistoryDto;
import com.junghaebom.geonganghaegym.course.domain.CourseStatus;
import com.junghaebom.geonganghaegym.course.presentation.dto.in.CourseAddCommand;
import com.junghaebom.geonganghaegym.course.presentation.dto.in.CourseUpdateCommand;
import com.junghaebom.geonganghaegym.course.presentation.dto.out.CourseGetResult;
import com.junghaebom.geonganghaegym.course.domain.Course;
import com.junghaebom.geonganghaegym.course.domain.CourseHistory;
import com.junghaebom.geonganghaegym.course.domain.CourseHistoryType;
import com.junghaebom.geonganghaegym.course.repository.CourseHistoryRepository;
import com.junghaebom.geonganghaegym.course.repository.CourseRepository;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.point.domain.Calculation;
import com.junghaebom.geonganghaegym.schedule.application.CommonScheduleService;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.StudentScheduleCond;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.MyReservation;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.repository.common.CommonScheduleRepository;
import com.junghaebom.geonganghaegym.schedule.repository.student.StudentScheduleRepository;
import com.junghaebom.geonganghaegym.schedule.repository.waiting.ScheduleWaitingRepository;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CourseService {

	private final MemberRepository memberRepository;
	private final CourseRepository courseRepository;
	private final CourseHistoryRepository courseHistoryRepository;
	private final TrainerMemberMappingRepository mappingRepository;
	private final CommonScheduleRepository commonScheduleRepository;
	private final StudentScheduleRepository studentScheduleRepository;
	private final CommonScheduleService commonScheduleService;
	private final ScheduleWaitingRepository scheduleWaitingRepository;

	public void addCourse(Long trainerId, CourseAddCommand command) {
		Member trainer = memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainerId, TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));
		Member member = memberRepository.findByIdAndMemberTypeAndDelYnFalse(command.memberId(), STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		mappingRepository.findByTrainerIdAndMemberId(trainerId, member.getId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_MAPPED));

		checkCourseAlreadyExists(member.getId());
		if (command.lessonCnt() < 1)
			throw new CustomException(LESSON_CNT_NOT_VALID);
		if (500 < command.lessonCnt())
			throw new CustomException(LESSON_CNT_MAX);
		Course course = courseRepository.save(
			Course.create(member, trainer, command.lessonCnt(), command.lessonCnt()));
		courseHistoryRepository.save(
			CourseHistory.create(course, course.getTotalLessonCnt(), PLUS, COURSE_CREATE, trainer));
		log.info("[수강권 등록] trainer: {}, course: {}, member:{}", trainer, course, course.getMember());
	}

	public void addCourseByNonmember(Long trainerId, CourseAddCommand command, Member nonmember) {
		Member trainer = memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainerId, TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));
		mappingRepository.findByTrainerIdAndMemberId(trainerId, nonmember.getId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_MAPPED));

		checkCourseAlreadyExists(nonmember.getId());
		if (command.lessonCnt() < 1)
			throw new CustomException(LESSON_CNT_NOT_VALID);
		if (500 < command.lessonCnt())
			throw new CustomException(LESSON_CNT_MAX);
		Course course = courseRepository.save(
			Course.create(nonmember, trainer, command.lessonCnt(), command.lessonCnt()));
		courseHistoryRepository.save(
			CourseHistory.create(course, course.getTotalLessonCnt(), PLUS, COURSE_CREATE, trainer));
		log.info("[수강권 등록] trainer: {}, course: {}, member:{}", trainer, course, course.getMember());
	}

	private void checkCourseAlreadyExists(Long memberId) {
		Long cnt = courseRepository.countByMemberIdAndRemainLessonCntGreaterThan(memberId, 0);
		if (0 < cnt)
			throw new CustomException(COURSE_ALREADY_EXISTS);
	}

	public void deleteCourseByTrainer(Long trainerId, Long courseId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(COURSE_NOT_FOUND));
		Long memberId = course.getMember().getId();

		//수업 진행 횟수가 1회이상이면 삭제 불가
		Long completedLessonCnt = getCompletedLessonCnt(memberId, courseId);
		if (0 < completedLessonCnt) {
			throw new CustomException(COURSE_IS_USING);
		} else {
			//해당 수강권으로 예약된 수업 조회
			StudentScheduleCond searchCond = new StudentScheduleCond(null, null, null, courseId);
			List<MyReservation> result = studentScheduleRepository.findNewReservation(memberId, searchCond);

			//예약된 수업이 있으면 수강권 삭제 불가
			if (!result.isEmpty())
				throw new CustomException(RESERVATION_ALREADY_EXISTS);
		}
		//대기내역 삭제
		scheduleWaitingRepository.deleteByMemberId(memberId);
		deleteCourse(trainerId, course);
	}

	public void deleteCourseAndCancelReservation(Long trainerId, Long courseId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(COURSE_NOT_FOUND));
		Long memberId = course.getMember().getId();

		//해당 수강권으로 예약된 수업 조회
		StudentScheduleCond searchCond = new StudentScheduleCond(null, null, null, courseId);
		List<MyReservation> result = studentScheduleRepository.findNewReservation(memberId, searchCond);

		//예약된 수업이 있으면 수업 취소
		if (!result.isEmpty())
			result.forEach(r -> commonScheduleService.cancelMemberScheduleForce(r.scheduleId(), memberId));
		//대기내역 삭제
		scheduleWaitingRepository.deleteByMemberId(memberId);
		deleteCourse(trainerId, course);
	}

	public void deleteCourse(Long trainerId, Course course) {
		Long completedLessonCnt = getCompletedLessonCnt(course.getMember().getId(), course.getCourseId());
		course.deleteSchedule();
		Long courseId = course.getCourseId();
		Member trainer = memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainerId, TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));
		courseRepository.deleteByCourseIdAndTrainerId(courseId, trainerId);
		log.info("[수강권 삭제] trainer: {}, course: {}, member: {}, completedLessonCnt: {}", trainer, course,
			course.getMember(), completedLessonCnt);
	}

	public CustomPaging getCourse(Member loginMember, Pageable pageable, Long memberId, String searchDate) {
		Member member = memberRepository.findByIdAndMemberTypeAndDelYnFalse(memberId, STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		Long trainerId = TRAINER.equals(loginMember.getMemberType()) ? loginMember.getId() : null;
		Page<CourseHistory> histories = courseHistoryRepository.getCourseHistory(memberId, trainerId, pageable,
			searchDate);
		List<CourseHistoryDto> courseHistoryDtos = histories.map(CourseHistoryDto::from).stream().toList();

		CustomPaging customPaging = new CustomPaging<>(courseHistoryDtos, histories.getPageable().getPageNumber(),
			histories.getPageable().getPageSize(), histories.getTotalPages(), histories.getTotalElements(),
			histories.isLast());

		CourseGetResult courseGetResult = CourseGetResult.create(getNowUsingCourse(memberId),
			member.getGym().getName());
		customPaging.setMainData(courseGetResult);
		return customPaging;
	}

	public CourseDto getNowUsingCourse(Long memberId) {
		Optional<Course> optCourse = courseRepository.findTop1ByMemberIdAndRemainLessonCntGreaterThanOrderByCreatedAtDesc(
			memberId, -1);
		if (optCourse.isPresent()) {
			CourseDto courseDto = CourseDto.from(optCourse.get());
			Long completedLessonCnt = getCompletedLessonCnt(memberId, courseDto.courseId());
			return courseDto.withCompletedLessonCnt(completedLessonCnt.intValue());
		}
		return null;
	}

	private Long getCompletedLessonCnt(Long memberId, Long courseDto) {
		return commonScheduleRepository.getCompletedLessonCnt(memberId, courseDto);
	}

	public CourseStatus getCourseStatus(CourseDto courseDto) {
		if (courseDto == null) {
			return NONE;
		} else if (courseDto.remainLessonCnt() == 0) {
			return EXPIRED;
		} else {
			return USING;
		}
	}

	public void updateCourseByTrainer(Long trainerId, Long courseId, CourseUpdateCommand command) {
		Member trainer = memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainerId, TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));
		Member member = memberRepository.findByIdAndMemberTypeAndDelYnFalse(command.memberId(), STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		mappingRepository.findByTrainerIdAndMemberId(trainerId, member.getId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_MAPPED));
		Course course = courseRepository.findByCourseIdAndMemberIdAndTrainerId(courseId, member.getId(), trainerId)
			.orElseThrow(() -> new CustomException(COURSE_NOT_FOUND));
		updateCourse(command, trainer, course, null);
	}

	public void updateCourseByMember(Long scheduleId, Long trainerId, CourseUpdateCommand command) {
		Member trainer = memberRepository.findByIdAndMemberTypeAndDelYnFalse(trainerId, TRAINER)
			.orElseThrow(() -> new CustomException(TRAINER_NOT_FOUND));
		Member member = memberRepository.findByIdAndMemberTypeAndDelYnFalse(command.memberId(), STUDENT)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		mappingRepository.findByTrainerIdAndMemberId(trainerId, member.getId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_MAPPED));

		CourseDto usingCourse = getNowUsingCourse(member.getId());
		CourseStatus courseStatus = getCourseStatus(usingCourse);
		Calculation calculation = command.calculation();

		switch (calculation) {
			case PLUS:
				if (NONE.equals(courseStatus))
					throw new CustomException(LESSON_CNT_NOT_VALID);
				break;
			case MINUS:
				if (NONE.equals(courseStatus) || EXPIRED.equals(courseStatus))
					throw new CustomException(LESSON_CNT_NOT_VALID);
				break;
		}

		Course course = courseRepository.findById(usingCourse.courseId())
			.orElseThrow(() -> new CustomException(LESSON_CNT_NOT_VALID));

		updateCourse(command, trainer, course, scheduleId);
		updateScheduleCourse(scheduleId, course, calculation);
	}

	private void updateCourse(CourseUpdateCommand command, Member trainer, Course course, Long scheduleId) {
		switch (command.calculation()) {
			case PLUS:
				if (command.updateCnt() < 1)
					throw new CustomException(COURSE_ONLY_PLUS);
				break;
			case MINUS:
				if (command.updateCnt() < 1)
					throw new CustomException(COURSE_POSITIVE);
				break;
		}

		int result = command.calculation().apply(course.getRemainLessonCnt(), command.updateCnt());
		if (result < 0)
			throw new CustomException(LESSON_CNT_NOT_VALID);
		if (500 < result)
			throw new CustomException(LESSON_CNT_MAX);

		course.updateRemainLessonCnt(command.calculation(), command.updateCnt());
		//수강권 변경 주체가 트레이너인 경우 -> 총 횟수도 함께 업데이트
		if (CourseHistoryType.getEnumByGroup(TRAINER).contains(command.type())) {
			course.updateTotalLessonCnt(command.calculation(), command.updateCnt());
		}
		CourseHistory history = courseHistoryRepository.save(
			CourseHistory.create(course, command.updateCnt(), command.calculation(), command.type(), trainer));
		log.info("[수강권 증감] trainer: {}, course: {}, history: {}, member: {}, scheduleId: {}", trainer, course, history,
			course.getMember(), scheduleId);
	}

	private void updateScheduleCourse(Long scheduleId, Course course, Calculation calculation) {
		Schedule schedule = commonScheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new CustomException(SCHEDULE_NOT_FOUND));
		switch (calculation) {
			case PLUS -> schedule.deleteCourse(); //수업취소
			case MINUS -> schedule.registerCourse(course); //수업예약
		}
	}

}
