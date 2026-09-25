package com.junghaebom.geonganghaegym.notification.presentation.dto.out;

import java.util.List;
import java.util.stream.Collectors;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.notification.domain.Notification;

public record CommandSendNotificationResult(
	Long notificationId,
	String title,
	String content,
	List<NotificationReciverInfo> receivers
) {
	public static CommandSendNotificationResult from(List<Notification> notifications) {
		Notification first = notifications.isEmpty() ? null : notifications.get(0);
		return new CommandSendNotificationResult(
			first != null ? first.getId() : null,
			first != null ? first.getTitle() : null,
			first != null ? first.getContent() : null,
			notifications.stream()
				.map(n -> NotificationReciverInfo.from(n.getReceiver()))
				.collect(Collectors.toList())
		);
	}

	public record NotificationReciverInfo(
		Long receiverId,
		String receiverName
	) {
		public static NotificationReciverInfo from(Member receiver) {
			return new NotificationReciverInfo(
				receiver != null ? receiver.getId() : null,
				receiver != null ? receiver.getName() : null
			);
		}
	}
}
