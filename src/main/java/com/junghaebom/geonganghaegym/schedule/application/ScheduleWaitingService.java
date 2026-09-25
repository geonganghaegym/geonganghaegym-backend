package com.junghaebom.geonganghaegym.schedule.application;

import static com.junghaebom.geonganghaegym.common.Utils.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.schedule.application.TrainerScheduleCommandService.ONE_DAY;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.FindMyScheduleWaitingResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.MyScheduleWaiting;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.domain.ScheduleWaiting;
import com.junghaebom.geonganghaegym.schedule.repository.TrainerScheduleRepository;
import com.junghaebom.geonganghaegym.schedule.repository.waiting.ScheduleWaitingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScheduleWaitingService {

	private final MemberRepository memberRepository;
	private final TrainerScheduleRepository trainerScheduleRepository;
	private final ScheduleWaitingRepository scheduleWaitingRepository;
	private final CourseService courseService;

	public String registerScheduleWaiting(Long scheduleId, Long memberId) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		Schedule schedule = trainerScheduleRepository.findAvailableWaitingId(scheduleId)
			.orElseThrow(() -> new CustomException(SCHEDULE_NOT_FOUND));

		CourseDto usingCourse = courseService.getNowUsingCourse(memberId);
		if (usingCourse == null)
			throw new CustomException(COURSE_NOT_FOUND);
		if (usingCourse.remainLessonCnt() == 0)
			throw new CustomException(LESSON_CNT_NOT_VALID);

		LocalDateTime lessonDateTime = LocalDateTime.of(schedule.getLessonDt(), schedule.getLessonStartTime());

		if (lessonDateTime.minusDays(ONE_DAY).isAfter(LocalDateTime.now())) {
			ScheduleWaiting scheduleWaiting = ScheduleWaiting.register(member, schedule);
			scheduleWaitingRepository.save(scheduleWaiting);
			log.info("[대기 신청] member: {}, schedule: {}, trainer: {}",
				member, schedule, schedule.getTrainer());
			return schedule.getLessonStartTime().format(formatter_hmm);
		} else {
			log.error("[대기 신청] member: {}, schedule: {}, trainer: {}, message:{}",
				member, schedule, schedule.getTrainer(), NOT_SCHEDULE_WAITING);
			throw new CustomException(NOT_SCHEDULE_WAITING);
		}
	}

	public String cancelScheduleWaiting(Long scheduleId, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		Schedule schedule = trainerScheduleRepository.findAvailableWaitingId(scheduleId)
			.orElseThrow(() -> new CustomException(SCHEDULE_NOT_FOUND));

		LocalDateTime before24Hour = LocalDateTime.of(schedule.getLessonDt().minusDays(1),
			schedule.getLessonStartTime());
		if (LocalDateTime.now().isAfter(before24Hour))
			throw new CustomException(RESERVATION_CANCEL_NOT_VALID);

		ScheduleWaiting scheduleWaiting = scheduleWaitingRepository.findByScheduleIdAndMemberId(scheduleId, memberId)
			.orElseThrow(() -> new CustomException(SCHEDULE_WAITING_NOT_FOUND));
		scheduleWaitingRepository.delete(scheduleWaiting);
		log.info("[대기 취소] member: {}, schedule: {}, trainer: {}", member, schedule, schedule.getTrainer());
		return scheduleWaiting.getSchedule().getLessonStartTime().format(formatter_hmm);
	}

	public FindMyScheduleWaitingResult findAllMyScheduleWaiting(Long memberId) {
		List<MyScheduleWaiting> result = scheduleWaitingRepository.findAllMyScheduleWaiting(memberId);
		return FindMyScheduleWaitingResult.create(courseService.getNowUsingCourse(memberId), result);
	}

}
