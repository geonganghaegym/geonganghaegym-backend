package com.junghaebom.geonganghaegym.notification.presentation.dto.out;

import com.querydsl.core.annotations.QueryProjection;
import com.junghaebom.geonganghaegym.notification.domain.NotificationCategory;

public record NotificationRedDotStatusResult(
	NotificationCategory notificationCategory,
	boolean redDotStatus
) {
	@QueryProjection
	public NotificationRedDotStatusResult {
	}
}
