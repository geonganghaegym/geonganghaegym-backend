package com.junghaebom.geonganghaegym.push.presentation.dto.in;

import com.junghaebom.geonganghaegym.push.domain.DeviceType;

public record CommandSendPushAlarm(
	String title,
	String message,
	String token,
	String clickUrl,
	DeviceType deviceType
) {
}
