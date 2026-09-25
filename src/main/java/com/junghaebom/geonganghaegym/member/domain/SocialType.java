package com.junghaebom.geonganghaegym.member.domain;

import com.junghaebom.geonganghaegym.common.enums.EnumMapperType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SocialType implements EnumMapperType {
	NONE("없음"),
	KAKAO("카카오"),
	NAVER("네이버"),
	GOOGLE("구글"),
	APPLE("애플");

	private final String description;

	@Override
	public String getCode() {
		return name();
	}
}
