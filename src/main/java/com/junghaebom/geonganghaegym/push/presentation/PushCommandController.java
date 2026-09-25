package com.junghaebom.geonganghaegym.push.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.push.application.PushCommandService;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandRegisterToken;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandRegisterTokenWithWebView;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandSendPushAlarm;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandSendPushAlarmToMember;
import com.junghaebom.geonganghaegym.push.presentation.dto.out.CommandRegisterTokenResult;
import com.junghaebom.geonganghaegym.push.presentation.dto.out.CommandSendPushAlarmResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushCommandController {

	private final PushCommandService pushCommandService;

	@PostMapping
	public ApiResult<CommandRegisterTokenResult> registerFcmToken(
		@RequestBody CommandRegisterToken request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("토큰을 저장하였습니다.", pushCommandService.registerFcmToken(request, member.getMemberId()));
	}

	@PostMapping("/webview")
	public void registerFcmTokenWithWebView(@RequestBody CommandRegisterTokenWithWebView request) {
		pushCommandService.registerFcmTokenWithWebView(request);
	}

	@PostMapping("/send")
	public ApiResult<CommandSendPushAlarmResult> sendPushAlarm(
		@RequestBody CommandSendPushAlarm request) {
		return ApiResult.success("푸시 전송에 성공하였습니다.", pushCommandService.sendPushAlarm(request));
	}

	@PostMapping("/{memberId}")
	public ApiResult<CommandSendPushAlarmResult> sendPushAlarm(
		@PathVariable Long memberId,
		@RequestBody CommandSendPushAlarmToMember request) {
		return ApiResult.success("푸시 전송에 성공하였습니다.", pushCommandService.sendPushAlarm(memberId, request));
	}
}
