package com.junghaebom.geonganghaegym.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationCategory {

	SCHEDULE("스케줄"),
	COMMUNITY("커뮤니티");

	private final String description;
}
