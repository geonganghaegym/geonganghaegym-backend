package com.junghaebom.geonganghaegym.member.domain;

import com.junghaebom.geonganghaegym.common.enums.EnumMapperType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AlarmType implements EnumMapperType {
	PUSH("푸시"),
	COMMUNITY("커뮤니티"),
	FEEDBACK("피드백"),
	SCHEDULENOTICE("일정알림");

	private final String description;

	@Override
	public String getCode() {
		return name();
	}
}