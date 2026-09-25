package com.junghaebom.geonganghaegym.push.application;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;
import com.google.firebase.messaging.WebpushNotification;
import com.junghaebom.geonganghaegym.common.NotificationSenderInfo;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandRegisterToken;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandRegisterTokenWithWebView;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandSendPushAlarm;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandSendPushAlarmToMember;
import com.junghaebom.geonganghaegym.push.presentation.dto.out.CommandRegisterTokenResult;
import com.junghaebom.geonganghaegym.push.presentation.dto.out.CommandSendPushAlarmResult;
import com.junghaebom.geonganghaegym.push.domain.DeviceType;
import com.junghaebom.geonganghaegym.push.domain.MemberToken;
import com.junghaebom.geonganghaegym.push.repository.MemberTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PushCommandService {

	private final MemberRepository memberRepository;
	private final MemberTokenRepository memberTokenRepository;

	public CommandRegisterTokenResult registerFcmToken(CommandRegisterToken request, Long memberId) {
		Member findMember = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		MemberToken findMemberToken = memberTokenRepository.findByMemberId(findMember.getId())
			.orElseGet(() -> memberTokenRepository.save(
				MemberToken.register(findMember, request.token(), DeviceType.WEB)
			));

		findMemberToken.changeToken(request.token(), DeviceType.WEB);

		return new CommandRegisterTokenResult(findMember.getName(), request.token());
	}

	public void registerFcmTokenWithWebView(CommandRegisterTokenWithWebView request) {
		Member findMember = memberRepository.findById(request.memberId())
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		MemberToken findMemberToken = memberTokenRepository.findByMemberId(findMember.getId())
			.orElseGet(() -> memberTokenRepository.save(
				MemberToken.register(findMember, request.token(), request.deviceType())
			));

		findMemberToken.changeToken(request.token(), request.deviceType());
	}

	public CommandSendPushAlarmResult sendPushAlarm(CommandSendPushAlarm request) {
		Message message = createMessage(request.token(), request.title(), request.message(),
			request.clickUrl());

		try {
			String response = FirebaseMessaging
				.getInstance()
				.sendAsync(message)
				.get();

			log.info("Sent message: {}", response);
		} catch (ExecutionException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed to send push alarm", e);
		}

		return CommandSendPushAlarmResult.from(request.title(), request.message());
	}

	public CommandSendPushAlarmResult sendPushAlarm(Long memberId, CommandSendPushAlarmToMember request) {
		MemberToken findMemberToken = memberTokenRepository.findByMemberId(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		Message message = createMessage(findMemberToken.getToken(), request.title(), request.message(), null);

		try {
			String response = FirebaseMessaging
				.getInstance()
				.sendAsync(message)
				.get();

			log.info("Sent message: {}", response);
		} catch (ExecutionException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed to send push alarm", e);
		}

		return CommandSendPushAlarmResult.from(request.title(), request.message());
	}

	private Message createMessage(String token, String title, String message, String clickUrl) {
		String resolvedClickUrl = clickUrl != null ? clickUrl : "";

		return Message.builder()
			.setNotification(
				Notification.builder()
					.setTitle(title)
					.setBody(message)
					.setImage(NotificationSenderInfo.DEFAULT_PROFILE_URL)
					.build()
			)
			.setAndroidConfig(
				AndroidConfig.builder()
					.setTtl(3600 * 1000L)
					.setNotification(
						AndroidNotification.builder()
							.setClickAction(resolvedClickUrl)
							.build()
					)
					.build()
			)
			.setApnsConfig(
				ApnsConfig.builder()
					.setAps(
						Aps.builder()
							.setAlert(
								ApsAlert.builder()
									.setTitle(title)
									.setBody(message)
									.build()
							)
							.setSound("default")
							.build()
					)
					.putHeader("apns-push-type", "alert")
					.putHeader("apns-priority", "10")
					.putHeader("apns-topic", "com.geonganghaejim.app")
					.build()
			)
			.setWebpushConfig(
				WebpushConfig.builder()
					.setNotification(
						new WebpushNotification(title, message)
					)
					.setFcmOptions(WebpushFcmOptions.withLink(resolvedClickUrl))
					.build()
			)
			.setToken(token)
			.build();
	}
}
