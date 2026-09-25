package com.junghaebom.geonganghaegym.workout.domain;

import com.junghaebom.geonganghaegym.common.enums.EnumMapperType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExerciseCategory implements EnumMapperType {

	CORE("코어"),
	LEG("다리"),
	ARM("팔"),
	SHOULDER("어깨"),
	CHEST("가슴"),
	BACK("등"),
	TRAPEZIUS("승모근"),
	STRETCHING("스트레칭");

	private final String description;

	@Override
	public String getCode() {
		return name();
	}
}
