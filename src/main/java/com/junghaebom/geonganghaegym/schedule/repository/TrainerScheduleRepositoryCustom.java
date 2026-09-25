package com.junghaebom.geonganghaegym.schedule.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.UnwrittenLessonHistorySearchCond;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.CommandRegisterSchedule;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.FeedbackNotificationToTrainer;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonDtResult;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.domain.TrainerScheduleInfo;

public interface TrainerScheduleRepositoryCustom {

	RetrieveTrainerScheduleByLessonDtResult findOneTrainerTodaySchedule(String lessonDt, Long trainerId);

	boolean validateDuplicateSchedule(
		TrainerScheduleInfo trainerScheduleInfo,
		CommandRegisterSchedule request,
		Long trainerId
	);

	Optional<Schedule> findAvailableWaitingId(Long scheduleId);

	List<Schedule> findAllSchedule(
		String lessonDt,
		LocalDate lessonStartDt,
		LocalDate lessonEndDt,
		Long trainerId
	);

	List<Schedule> findAllSchedule(
		List<Long> scheduleIds,
		List<ReservationStatus> reservationStatus,
		Long trainerId
	);

	Schedule findAllSchedule(Long scheduleId, ReservationStatus reservationStatus, Long trainerId);

	List<Schedule> findAllDisabledSchedule(LocalDate lessonStartDt, LocalDate lessonEndDt);

	List<Schedule> findAllUnwrittenLessonHistory(UnwrittenLessonHistorySearchCond request, Long memberId);

	List<Schedule> findAllSimpleLessonHistoryByMemberId(Long studentId, Long trainerId);

	Page<Schedule> findAllScheduleByStudentId(Long studentId, Pageable pageable, Long trainerId);

	List<FeedbackNotificationToTrainer> findAllFeedbackNotificationToTrainer();
}
