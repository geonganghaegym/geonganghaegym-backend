package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.time.LocalTime;

import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public record CommandCancelStudentReservationResult(
	Long scheduleId,
	LocalTime lessonStartTime,
	LocalTime lessonEndTime,
	Long trainerId,
	String trainerName,
	Long studentId,
	String studentName,
	Long waitingStudentId
) {
	public static CommandCancelStudentReservationResult from(
		Schedule schedule,
		Long applicantId,
		String applicantName
	) {
		return new CommandCancelStudentReservationResult(
			schedule.getId(),
			schedule.getLessonStartTime(),
			schedule.getLessonEndTime(),
			schedule.getTrainer().getId(),
			schedule.getTrainer().getName() + " 트레이너",
			applicantId,
			applicantName,
			schedule.getScheduleWaiting() != null && !schedule.getScheduleWaiting().isEmpty()
				? schedule.getScheduleWaiting().get(0).getId()
				: null
		);
	}
}
