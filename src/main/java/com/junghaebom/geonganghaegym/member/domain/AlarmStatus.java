package com.junghaebom.geonganghaegym.member.domain;

import com.junghaebom.geonganghaegym.common.enums.EnumMapperType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AlarmStatus implements EnumMapperType {
	ENABLED("알림켜기"),
	DISABLE("알림끄기");

	private final String description;

	@Override
	public String getCode() {
		return name();
	}
}