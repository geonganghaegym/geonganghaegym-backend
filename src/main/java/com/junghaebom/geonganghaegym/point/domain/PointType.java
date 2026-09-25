package com.junghaebom.geonganghaegym.point.domain;

import com.junghaebom.geonganghaegym.common.enums.EnumMapperType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PointType implements EnumMapperType {
	NO_SHOW("수업불참"),
	NO_SHOW_CANCEL("수업불참 취소"),
	WORKOUT("운동기록"),
	DIET("식단기록");

	private final String description;

	@Override
	public String getCode() {
		return name();
	}
}
