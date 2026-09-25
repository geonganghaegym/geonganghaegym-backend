package com.junghaebom.geonganghaegym.course.presentation.dto.out;

import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;

public record CourseGetResult(
	CourseDto course,
	String gymName
) {

	public static CourseGetResult create(CourseDto courseDto, String gymName) {
		return new CourseGetResult(courseDto, gymName);
	}
}
