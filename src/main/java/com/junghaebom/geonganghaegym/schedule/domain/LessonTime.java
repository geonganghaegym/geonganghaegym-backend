package com.junghaebom.geonganghaegym.schedule.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LessonTime {
	HALF_HOUR(30),
	ONE_HOUR(60),
	ONE_AND_HALF_HOUR(90),
	TWO_HOUR(120),
	TWO_AND_HALF_HOUR(150),
	THREE_HOUR(180);

	private final Integer description;
}
