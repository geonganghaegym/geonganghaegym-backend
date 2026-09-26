package com.junghaebom.geonganghaegym.push.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import com.google.firebase.ErrorCode;
import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.push.domain.DeviceType;
import com.junghaebom.geonganghaegym.push.domain.MemberToken;
import com.junghaebom.geonganghaegym.push.presentation.dto.in.CommandRegisterTokenWithWebView;
import com.junghaebom.geonganghaegym.push.repository.MemberTokenRepository;

class PushCommandServiceTest {

	private final MemberRepository memberRepository = mock(MemberRepository.class);
	private final MemberTokenRepository memberTokenRepository = mock(MemberTokenRepository.class);
	private final PushCommandService service = new PushCommandService(memberRepository, memberTokenRepository);

	private final FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
	private MockedStatic<FirebaseMessaging> firebaseStatic;

	@BeforeEach
	void setUp() {
		firebaseStatic = mockStatic(FirebaseMessaging.class);
		firebaseStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
	}

	@AfterEach
	void tearDown() {
		firebaseStatic.close();
	}

	@Test
	@DisplayName("회원에게 이미 다른 기기 토큰이 있어도 새 기기 토큰은 덮어쓰지 않고 추가 저장한다")
	void registerNewDeviceTokenAddsRow() {
		Member member = Member.builder().id(1L).build();
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(memberTokenRepository.findFirstByToken("web-token")).thenReturn(Optional.empty());

		service.registerFcmTokenWithWebView(new CommandRegisterTokenWithWebView(1L, "web-token", DeviceType.WEB));

		ArgumentCaptor<MemberToken> saved = ArgumentCaptor.forClass(MemberToken.class);
		verify(memberTokenRepository).save(saved.capture());
		assertThat(saved.getValue().getToken()).isEqualTo("web-token");
		assertThat(saved.getValue().getMember()).isSameAs(member);
		verify(memberTokenRepository, never()).findAllByMemberId(any());
	}

	@Test
	@DisplayName("이미 저장된 토큰을 다른 계정이 등록하면 새 행을 만들지 않고 소유자를 옮긴다")
	void registerExistingTokenMovesOwner() {
		Member previousOwner = Member.builder().id(1L).build();
		Member newOwner = Member.builder().id(2L).build();
		MemberToken existing = MemberToken.register(previousOwner, "device-token", DeviceType.AOS);
		when(memberRepository.findById(2L)).thenReturn(Optional.of(newOwner));
		when(memberTokenRepository.findFirstByToken("device-token")).thenReturn(Optional.of(existing));

		service.registerFcmTokenWithWebView(new CommandRegisterTokenWithWebView(2L, "device-token", DeviceType.AOS));

		assertThat(existing.getMember()).isSameAs(newOwner);
		verify(memberTokenRepository, never()).save(any());
	}

	@Test
	@DisplayName("모든 기기로 발송하고, 다시 쓸 수 없는 토큰만 삭제한 뒤 나머지 기기 발송을 계속한다")
	void sendToAllDeletesStaleTokenAndContinues() throws Exception {
		Member member = Member.builder().id(1L).build();
		MemberToken stale = MemberToken.register(member, "stale-token", DeviceType.WEB);
		MemberToken valid = MemberToken.register(member, "valid-token", DeviceType.AOS);
		FirebaseMessagingException unregistered = firebaseError(MessagingErrorCode.UNREGISTERED);
		when(firebaseMessaging.send(any(Message.class)))
			.thenThrow(unregistered)
			.thenReturn("ok");

		service.sendToAll(List.of(stale, valid), "제목", "내용", null);

		verify(firebaseMessaging, times(2)).send(any(Message.class));
		verify(memberTokenRepository).delete(stale);
		verify(memberTokenRepository, never()).delete(valid);
	}

	@Test
	@DisplayName("토큰 문제가 아닌 발송 실패는 토큰을 지우지 않고 예외를 던진다")
	void sendToAllRethrowsNonTokenFailure() throws Exception {
		Member member = Member.builder().id(1L).build();
		MemberToken token = MemberToken.register(member, "token", DeviceType.AOS);
		FirebaseMessagingException unavailable = firebaseError(MessagingErrorCode.UNAVAILABLE);
		when(firebaseMessaging.send(any(Message.class))).thenThrow(unavailable);

		assertThatThrownBy(() -> service.sendToAll(List.of(token), "제목", "내용", null))
			.isInstanceOf(RuntimeException.class)
			.hasCause(unavailable);
		verify(memberTokenRepository, never()).delete(any());
	}

	// 실패 보고 시 스택 트레이스가 필요해 mock 대신 SDK의 패키지 내부 팩토리로 실제 예외를 만든다
	private FirebaseMessagingException firebaseError(MessagingErrorCode code) throws Exception {
		Method factory = FirebaseMessagingException.class.getDeclaredMethod(
			"withMessagingErrorCode", FirebaseException.class, MessagingErrorCode.class);
		factory.setAccessible(true);
		FirebaseException base = new FirebaseException(ErrorCode.NOT_FOUND, code.name(), null);
		return (FirebaseMessagingException)factory.invoke(null, base, code);
	}
}
