package com.junghaebom.geonganghaegym.push.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.push.application.PushCommandService;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandRegisterToken;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandRegisterTokenWithWebView;
import com.junghaebom.geonganghaegym.push.presentation.dto.out.CommandRegisterTokenResult;

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
}
