package com.junghaebom.geonganghaegym.notification.presentation.dto.out;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.junghaebom.geonganghaegym.notification.domain.Notification;

public record RetrieveNotificationWithRedDotResult(
	List<RetrieveNotificationResult> content,
	List<NotificationRedDotStatusResult> redDotStatus
) {
	public static RetrieveNotificationWithRedDotResult from(
		Page<Notification> notifications,
		List<NotificationRedDotStatusResult> redDotStatus) {
		return new RetrieveNotificationWithRedDotResult(
			notifications.getContent().stream()
				.map(RetrieveNotificationResult::from)
				.collect(Collectors.toList()),
			redDotStatus
		);
	}

	public record RetrieveNotificationResult(
		Long notificationId,
		String notificationCategoryAndType,
		Long receiverId,
		String receiverName,
		String title,
		String content,
		String createdAt,
		@JsonProperty("isRead") boolean isRead,
		Long targetId,
		Long studentId,
		String studentName
	) {
		public static RetrieveNotificationResult from(Notification notification) {
			return new RetrieveNotificationResult(
				notification.getId(),
				notification.getNotificationCategory().name() + "-" + notification.getNotificationType().name(),
				notification.getReceiver() != null ? notification.getReceiver().getId() : null,
				notification.getReceiver() != null ? notification.getReceiver().getName() : null,
				notification.getTitle(),
				notification.getContent(),
				notification.getCreatedAt().toString(),
				notification.isRead(),
				notification.getTargetId(),
				notification.getStudentId(),
				notification.getStudentName()
			);
		}
	}
}
