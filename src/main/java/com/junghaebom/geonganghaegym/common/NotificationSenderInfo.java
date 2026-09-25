package com.junghaebom.geonganghaegym.common;

import com.junghaebom.geonganghaegym.notification.domain.NotificationSenderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class NotificationSenderInfo {

	public static final String DEFAULT_PROFILE_URL = "https://geonganghaegym.junghaebom.com/images/default-profile.png";

	private NotificationSenderInfo() {
	}

	public static SenderInfo getSenderInfo() {
		return new SenderInfo();
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SenderInfo {

		@Builder.Default
		private String profileUrl = DEFAULT_PROFILE_URL;

		@Builder.Default
		private NotificationSenderType senderType = NotificationSenderType.SYSTEM;
	}
}
