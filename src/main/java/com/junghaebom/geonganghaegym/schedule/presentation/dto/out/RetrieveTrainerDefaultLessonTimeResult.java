package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.junghaebom.geonganghaegym.common.LessonTimeFormatter;
import com.junghaebom.geonganghaegym.schedule.domain.TrainerScheduleInfo;

public record RetrieveTrainerDefaultLessonTimeResult(
	String lessonStartTime,
	String lessonEndTime,
	String lunchStartTime,
	String lunchEndTime,
	Integer lessonTime,
	List<DayOfWeek> closedDays
) {
	public static RetrieveTrainerDefaultLessonTimeResult from(TrainerScheduleInfo trainerScheduleInfo) {
		if (trainerScheduleInfo == null) {
			return new RetrieveTrainerDefaultLessonTimeResult(
				LessonTimeFormatter.formatLessonTime(null),
				LessonTimeFormatter.formatLessonTime(null),
				LessonTimeFormatter.formatLessonTime(null),
				LessonTimeFormatter.formatLessonTime(null),
				null,
				new ArrayList<>()
			);
		}
		return new RetrieveTrainerDefaultLessonTimeResult(
			LessonTimeFormatter.formatLessonTime(trainerScheduleInfo.getLessonStartTime()),
			LessonTimeFormatter.formatLessonTime(trainerScheduleInfo.getLessonEndTime()),
			LessonTimeFormatter.formatLessonTime(trainerScheduleInfo.getLunchStartTime()),
			LessonTimeFormatter.formatLessonTime(trainerScheduleInfo.getLunchEndTime()),
			trainerScheduleInfo.getLessonTime().getDescription(),
			trainerScheduleInfo.getTrainerScheduleClosedDays() != null
				? trainerScheduleInfo.getTrainerScheduleClosedDays().stream()
				  .map(day -> day.getClosedDays())
				  .collect(Collectors.toList())
				: new ArrayList<>()
		);
	}
}
