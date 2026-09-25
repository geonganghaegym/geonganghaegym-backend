package com.junghaebom.geonganghaegym.member.domain;

import com.junghaebom.geonganghaegym.common.enums.EnumMapperType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TrainerFeedback implements EnumMapperType {
	ENABLED_RECORD("기록하기"),
	DISABLE_RECORD("기록끄기");

	private final String description;

	@Override
	public String getCode() {
		return name();
	}
}
