package com.junghaebom.geonganghaegym.notification.presentation;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.common.KotlinCustomPaging;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.notification.application.NotificationService;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.CommandNotificationStatusResult;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.RetrieveNotificationWithRedDotResult.RetrieveNotificationResult;
import com.junghaebom.geonganghaegym.notification.domain.NotificationCategory;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/{notificationCategory}")
	public ApiResult<KotlinCustomPaging<RetrieveNotificationResult>> findAllNotification(
		@PathVariable NotificationCategory notificationCategory,
		@AuthenticationPrincipal CustomMemberDetails member,
		@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
		return ApiResult.success("전체 알림을 조회하였습니다.", notificationService.findAllNotification(notificationCategory, member.getMemberId(), pageable));
	}

	@PatchMapping("/{notificationId}")
	public ApiResult<CommandNotificationStatusResult> updateNotificationStatus(
		@PathVariable Long notificationId,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("해당 알림을 읽음 처리 하였습니다.", notificationService.updateNotificationStatus(notificationId, member.getMemberId()));
	}

	@GetMapping("/red-dot")
	public ApiResult<Boolean> findNotificationWithRedDot(
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("red-dot 상태를 조회하였습니다.", notificationService.findRedDotStatus(member.getMemberId()));
	}
}
