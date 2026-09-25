package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import com.junghaebom.geonganghaegym.common.LessonTimeFormatter;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public record CommandScheduleStatusResult(
	Long scheduleId,
	Long studentId,
	String studentName,
	Long trainerId,
	String lessonDt,
	String lessonTime,
	ReservationStatus reservationStatus
) {
	public static CommandScheduleStatusResult from(Schedule schedule) {
		return new CommandScheduleStatusResult(
			schedule.getId(),
			schedule.getApplicant() != null ? schedule.getApplicant().getId() : null,
			schedule.getApplicant() != null ? schedule.getApplicant().getName() : null,
			schedule.getTrainer() != null ? schedule.getTrainer().getId() : null,
			LessonTimeFormatter.formatLessonDt(schedule.getLessonDt()),
			LessonTimeFormatter.formatLessonTime(schedule.getLessonStartTime(), schedule.getLessonEndTime()),
			schedule.getReservationStatus()
		);
	}
}
