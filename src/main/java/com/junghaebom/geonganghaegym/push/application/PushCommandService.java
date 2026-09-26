package com.junghaebom.geonganghaegym.push.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
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

		registerToken(findMember, request.token(), DeviceType.WEB);

		return new CommandRegisterTokenResult(findMember.getName(), request.token());
	}

	public void registerFcmTokenWithWebView(CommandRegisterTokenWithWebView request) {
		Member findMember = memberRepository.findById(request.memberId())
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		registerToken(findMember, request.token(), request.deviceType());
	}

	// 토큰은 기기 단위라 토큰 기준으로 저장한다. 같은 기기에서 다른 계정으로 로그인하면 소유자만 옮긴다.
	private void registerToken(Member member, String token, DeviceType deviceType) {
		memberTokenRepository.findFirstByToken(token)
			.ifPresentOrElse(
				found -> found.changeOwner(member, deviceType),
				() -> memberTokenRepository.save(MemberToken.register(member, token, deviceType))
			);
	}

	public CommandSendPushAlarmResult sendPushAlarm(CommandSendPushAlarm request) {
		try {
			send(createMessage(request.token(), request.title(), request.message(), request.clickUrl()));
		} catch (FirebaseMessagingException e) {
			throw new RuntimeException("Failed to send push alarm", e);
		}

		return CommandSendPushAlarmResult.from(request.title(), request.message());
	}

	public CommandSendPushAlarmResult sendPushAlarm(Long memberId, CommandSendPushAlarmToMember request) {
		List<MemberToken> memberTokens = memberTokenRepository.findAllByMemberId(memberId);
		if (memberTokens.isEmpty()) {
			throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
		}

		sendToAll(memberTokens, request.title(), request.message(), null);

		return CommandSendPushAlarmResult.from(request.title(), request.message());
	}

	public void sendToAll(List<MemberToken> memberTokens, String title, String message, String clickUrl) {
		for (MemberToken memberToken : memberTokens) {
			try {
				send(createMessage(memberToken.getToken(), title, message, clickUrl));
			} catch (FirebaseMessagingException e) {
				if (!isStaleToken(e)) {
					throw new RuntimeException("Failed to send push alarm", e);
				}
				log.info("다시 쓸 수 없는 FCM 토큰을 삭제한다. memberTokenId={}, code={}",
					memberToken.getId(), e.getMessagingErrorCode());
				memberTokenRepository.delete(memberToken);
			}
		}
	}

	// 앱 삭제·토큰 만료(UNREGISTERED), 다른 Firebase 프로젝트에서 발급된 토큰(SENDER_ID_MISMATCH)
	private boolean isStaleToken(FirebaseMessagingException exception) {
		MessagingErrorCode code = exception.getMessagingErrorCode();
		return code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.SENDER_ID_MISMATCH;
	}

	private void send(Message message) throws FirebaseMessagingException {
		String response = FirebaseMessaging.getInstance().send(message);
		log.info("Sent message: {}", response);
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
