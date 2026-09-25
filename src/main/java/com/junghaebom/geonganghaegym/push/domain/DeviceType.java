package com.junghaebom.geonganghaegym.push.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceType {
	WEB("웹"),
	AOS("안드로이드"),
	IOS("애플");

	private final String description;
}
