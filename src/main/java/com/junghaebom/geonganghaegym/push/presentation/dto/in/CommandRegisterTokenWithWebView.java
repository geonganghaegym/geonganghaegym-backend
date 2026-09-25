package com.junghaebom.geonganghaegym.push.presentation.dto.in;

import com.junghaebom.geonganghaegym.push.domain.DeviceType;

public record CommandRegisterTokenWithWebView(
	Long memberId,
	String token,
	DeviceType deviceType
) {
}
