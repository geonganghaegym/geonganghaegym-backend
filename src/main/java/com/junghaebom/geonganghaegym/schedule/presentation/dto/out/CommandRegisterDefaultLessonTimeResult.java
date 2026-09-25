package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.time.DayOfWeek;
import java.util.List;

import com.junghaebom.geonganghaegym.common.LessonTimeFormatter;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.CommandRegisterDefaultLessonTime;

public record CommandRegisterDefaultLessonTimeResult(
	String lessonStartTime,
	String lessonEndTime,
	String lunchStartTime,
	String lunchEndTime,
	List<DayOfWeek> closedDays,
	int lessonTime
) {
	public static CommandRegisterDefaultLessonTimeResult from(CommandRegisterDefaultLessonTime request) {
		return new CommandRegisterDefaultLessonTimeResult(
			LessonTimeFormatter.formatLessonTime(request.lessonStartTime()),
			LessonTimeFormatter.formatLessonTime(request.lessonEndTime()),
			LessonTimeFormatter.formatLessonTime(request.lunchStartTime()),
			LessonTimeFormatter.formatLessonTime(request.lunchEndTime()),
			request.closedDays(),
			request.lessonTime()
		);
	}
}
