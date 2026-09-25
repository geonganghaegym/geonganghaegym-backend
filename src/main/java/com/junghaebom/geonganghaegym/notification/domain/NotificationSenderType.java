package com.junghaebom.geonganghaegym.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationSenderType {

	SYSTEM("시스템"),
	USER("사용자");

	private final String description;
}
