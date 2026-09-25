package com.junghaebom.geonganghaegym.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.notification.presentation.dto.out.NotificationRedDotStatusResult;
import com.junghaebom.geonganghaegym.notification.domain.Notification;
import com.junghaebom.geonganghaegym.notification.domain.NotificationCategory;

public interface NotificationRepositoryCustom {

	Page<Notification> findAllByNotificationType(
		NotificationCategory notificationCategory,
		Long receiverId,
		Pageable pageable);

	List<NotificationRedDotStatusResult> findAllRedDotStatus(
		NotificationCategory notificationCategory,
		Long receiverId);

	boolean findRedDotStatus(Long receiverId);
}
