package com.junghaebom.geonganghaegym.diet.domain;

import com.junghaebom.geonganghaegym.common.enums.EnumMapperType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DietType implements EnumMapperType {

	BREAKFAST("breakfast"),
	LUNCH("lunch"),
	DINNER("dinner");

	private final String description;

	@Override
	public String getCode() {
		return name();
	}
}
