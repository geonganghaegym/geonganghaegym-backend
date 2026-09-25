package com.junghaebom.geonganghaegym.push.presentation.dto.out;

public record CommandSendPushAlarmResult(
	String title,
	String message
) {
	public static CommandSendPushAlarmResult from(String title, String message) {
		return new CommandSendPushAlarmResult(title, message);
	}
}
