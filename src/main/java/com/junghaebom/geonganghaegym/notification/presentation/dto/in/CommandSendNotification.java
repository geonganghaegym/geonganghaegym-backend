package com.junghaebom.geonganghaegym.notification.presentation.dto.in;

import java.util.List;

import com.junghaebom.geonganghaegym.notification.domain.NotificationCategory;
import com.junghaebom.geonganghaegym.notification.domain.NotificationType;

public record CommandSendNotification(
	String title,
	String content,
	List<Long> receiverIds,
	NotificationType notificationType,
	NotificationCategory notificationCategory,
	Long targetId,
	String clickUrl,
	Long studentId,
	String studentName
) {
}
