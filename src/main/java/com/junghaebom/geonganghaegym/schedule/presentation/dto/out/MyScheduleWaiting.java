package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.time.LocalDate;
import java.time.LocalTime;

import com.junghaebom.geonganghaegym.schedule.domain.ScheduleWaiting;

public record MyScheduleWaiting(
	Long scheduleId,
	String trainerName,
	LocalDate lessonDt,
	LocalTime lessonStartTime,
	LocalTime lessonEndTime,
	String reservationStatus
) {
	public static MyScheduleWaiting from(ScheduleWaiting scheduleWaiting) {
		return new MyScheduleWaiting(
			scheduleWaiting.getSchedule().getId(),
			scheduleWaiting.getSchedule().getTrainer().getName() + " 트레이너",
			scheduleWaiting.getSchedule().getLessonDt(),
			scheduleWaiting.getSchedule().getLessonStartTime(),
			scheduleWaiting.getSchedule().getLessonEndTime(),
			null
		);
	}
}
