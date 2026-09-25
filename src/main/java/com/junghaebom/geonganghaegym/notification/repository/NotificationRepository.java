package com.junghaebom.geonganghaegym.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

	Notification findByIdAndReceiverId(Long notificationId, Long receiverId);
}
