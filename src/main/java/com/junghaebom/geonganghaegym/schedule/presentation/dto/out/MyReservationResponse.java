package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.util.List;

import org.springframework.util.ObjectUtils;

import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;

public record MyReservationResponse(
	CourseDto course,
	List<MyReservation> reservations
) {
	public static MyReservationResponse create(CourseDto course, List<MyReservation> reservations) {
		return new MyReservationResponse(
			course,
			ObjectUtils.isEmpty(reservations) ? null : reservations
		);
	}
}
