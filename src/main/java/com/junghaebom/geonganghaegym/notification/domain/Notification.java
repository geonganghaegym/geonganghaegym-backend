package com.junghaebom.geonganghaegym.notification.domain;

import static jakarta.persistence.EnumType.*;

import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class Notification extends BaseTimeEntity<Notification, Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long id;

	private Long studentId;

	private String studentName;

	private String clickUrl;

	private String title;

	private String content;

	@Enumerated(STRING)
	private NotificationCategory notificationCategory;

	@Enumerated(STRING)
	private NotificationType notificationType;

	@Enumerated(STRING)
	private NotificationSenderType senderType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id")
	private Member receiver;

	private Long targetId;

	private boolean isRead;

	private Notification(String title, String content, NotificationCategory notificationCategory,
		NotificationType notificationType, Member receiver, Long targetId,
		String clickUrl, Long studentId, String studentName) {
		this.title = title;
		this.content = content;
		this.notificationCategory = notificationCategory;
		this.notificationType = notificationType;
		this.senderType = NotificationSenderType.SYSTEM;
		this.receiver = receiver;
		this.targetId = targetId;
		this.clickUrl = clickUrl;
		this.studentId = studentId;
		this.studentName = studentName;
		this.isRead = false;
	}

	public static Notification create(String title, String content,
		NotificationCategory notificationCategory,
		NotificationType notificationType,
		Member receiver, Long targetId,
		String clickUrl, Long studentId, String studentName) {
		return new Notification(title, content, notificationCategory, notificationType,
			receiver, targetId, clickUrl, studentId, studentName);
	}

	public void updateNotificationStatus() {
		this.isRead = true;
	}
}
