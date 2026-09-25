package com.junghaebom.geonganghaegym.lessonhistory.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LessonAttendanceStatus {
	ATTENDED("출석"),
	ABSENT("미출석");

	private final String description;
}
