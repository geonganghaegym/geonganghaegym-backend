package com.junghaebom.geonganghaegym.notification.application;

import static com.junghaebom.geonganghaegym.common.Utils.WEB_BASE_URL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.KotlinCustomPaging;
import com.junghaebom.geonganghaegym.common.NotificationSenderInfo;
import com.junghaebom.geonganghaegym.member.domain.AlarmStatus;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.notification.presentation.dto.in.CommandSendNotification;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.CommandNotificationStatusResult;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.CommandSendNotificationResult;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.RetrieveNotificationWithRedDotResult;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.RetrieveNotificationWithRedDotResult.RetrieveNotificationResult;
import com.junghaebom.geonganghaegym.notification.domain.Notification;
import com.junghaebom.geonganghaegym.notification.domain.NotificationCategory;
import com.junghaebom.geonganghaegym.notification.domain.NotificationType;
import com.junghaebom.geonganghaegym.notification.repository.NotificationRepository;
import com.junghaebom.geonganghaegym.push.application.PushCommandService;
import com.junghaebom.geonganghaegym.push.domain.MemberToken;
import com.junghaebom.geonganghaegym.schedule.repository.TrainerScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final PushCommandService pushCommandService;
	private final TrainerScheduleRepository trainerScheduleRepository;

	public CommandSendNotificationResult sendNotificationFromSystem(CommandSendNotification request) {

		List<Member> receivers = memberRepository.findMemberTokenById(request.receiverIds());

		if (receivers.isEmpty()) {
			throw new IllegalArgumentException("수신자의 ID가 존재하지 않습니다.");
		}

		List<Notification> notifications = new ArrayList<>();

		for (Member receiver : receivers) {

			if (request.notificationCategory() == NotificationCategory.COMMUNITY
				&& receiver.getCommunityAlarmStatus() == AlarmStatus.DISABLE) {
				throw new IllegalArgumentException("커뮤니티 알림을 거부한 수신자입니다.");
			}

			List<MemberToken> memberTokens = receiver.getMemberToken();
			if (memberTokens != null && !memberTokens.isEmpty()) {
				pushCommandService.sendToAll(memberTokens, request.title(), request.content(), request.clickUrl());

				Notification notification = Notification.create(
					request.title(),
					request.content(),
					request.notificationCategory(),
					request.notificationType(),
					receiver,
					request.targetId(),
					request.clickUrl(),
					request.studentId(),
					request.studentName()
				);
				log.info("notification: {}", notification);
				notifications.add(notification);
			}
		}

		notificationRepository.saveAll(notifications);

		return CommandSendNotificationResult.from(notifications);
	}

	public KotlinCustomPaging<RetrieveNotificationResult> findAllNotification(
		NotificationCategory notificationCategory,
		Long receiverId,
		Pageable pageable) {

		Page<Notification> notification = notificationRepository.findAllByNotificationType(notificationCategory,
			receiverId, pageable);

		var redDotStatus = notificationRepository.findAllRedDotStatus(notificationCategory, receiverId);

		RetrieveNotificationWithRedDotResult results = RetrieveNotificationWithRedDotResult.from(notification,
			redDotStatus);

		List<RetrieveNotificationResult> content = results.content();

		return KotlinCustomPaging.<RetrieveNotificationResult>builder()
			.content(content.isEmpty() ? null : content)
			.pageNumber(notification.getPageable().getPageNumber())
			.pageSize(notification.getPageable().getPageSize())
			.totalPages(notification.getTotalPages())
			.totalElements(notification.getTotalElements())
			.isLast(notification.isLast())
			.redDotStatus(results.redDotStatus())
			.sender(NotificationSenderInfo.getSenderInfo())
			.build();
	}

	public boolean findRedDotStatus(Long memberId) {
		return notificationRepository.findRedDotStatus(memberId);
	}

	public CommandNotificationStatusResult updateNotificationStatus(Long notificationId, Long receiverId) {

		Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, receiverId);

		if (notification == null) {
			throw new IllegalArgumentException("해당 알림이 존재하지 않습니다.");
		}

		notification.updateNotificationStatus();

		return CommandNotificationStatusResult.from(notification);
	}

	public void sendFeedbackNotificationToTrainer() {
		trainerScheduleRepository.findAllFeedbackNotificationToTrainer().forEach(it ->
			sendNotificationFromSystem(
				new CommandSendNotification(
					NotificationType.FEEDBACK.getDescription(),
					String.format(NotificationType.FEEDBACK.getContent(), it.count()),
					List.of(it.trainerId()),
					NotificationType.FEEDBACK,
					NotificationCategory.SCHEDULE,
					null,
					WEB_BASE_URL + "/trainer/manage/feedback",
					null,
					null
				)
			)
		);
	}
}
