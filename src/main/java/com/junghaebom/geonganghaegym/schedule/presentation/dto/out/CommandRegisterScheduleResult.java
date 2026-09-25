package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.domain.TrainerScheduleInfo;

public record CommandRegisterScheduleResult(
	List<LocalDate> lessonDt,
	LocalTime lessonStartTime,
	LocalTime lessonEndTime,
	int lessonTime,
	LocalTime lunchStartTime,
	LocalTime lunchEndTime
) {
	public static CommandRegisterScheduleResult from(
		List<Schedule> schedule,
		TrainerScheduleInfo trainerScheduleInfo
	) {
		return new CommandRegisterScheduleResult(
			schedule.stream()
				.map(Schedule::getLessonDt)
				.distinct()
				.collect(Collectors.toList()),
			trainerScheduleInfo.getLessonStartTime(),
			trainerScheduleInfo.getLessonEndTime(),
			trainerScheduleInfo.getLessonTime().getDescription(),
			trainerScheduleInfo.getLunchStartTime(),
			trainerScheduleInfo.getLunchEndTime()
		);
	}
}
