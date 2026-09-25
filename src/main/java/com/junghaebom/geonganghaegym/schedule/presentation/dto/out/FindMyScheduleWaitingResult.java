package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.util.List;

import org.springframework.util.ObjectUtils;

import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;

public record FindMyScheduleWaitingResult(
	CourseDto course,
	List<MyScheduleWaiting> myScheduleWaitings
) {
	public static FindMyScheduleWaitingResult create(CourseDto course, List<MyScheduleWaiting> myScheduleWaitings) {
		return new FindMyScheduleWaitingResult(
			course,
			ObjectUtils.isEmpty(myScheduleWaitings) ? null : myScheduleWaitings
		);
	}
}
