package com.junghaebom.geonganghaegym.member.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandLogout;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.push.domain.DeviceType;
import com.junghaebom.geonganghaegym.push.domain.MemberToken;
import com.junghaebom.geonganghaegym.push.repository.MemberTokenRepository;

@ExtendWith(MockitoExtension.class)
class MemberLogoutTest {

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private RedisService redisService;
	@Mock
	private MemberTokenRepository memberTokenRepository;
	@InjectMocks
	private MemberCommandService memberCommandService;

	private MemberToken webToken;
	private MemberToken appToken;

	@BeforeEach
	void setUp() {
		Member member = Member.builder().id(1L).userId("user").build();
		webToken = MemberToken.register(member, "web-token", DeviceType.WEB);
		appToken = MemberToken.register(member, "app-token", DeviceType.AOS);
		member.getMemberToken().addAll(List.of(webToken, appToken));
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
	}

	@Test
	@DisplayName("로그아웃 요청에 FCM 토큰이 있으면 그 기기 토큰만 삭제하고 다른 기기 토큰은 남긴다")
	void logoutWithTokenDeletesOnlyThatDevice() {
		memberCommandService.logout(1L, new CommandLogout("web-token", null));

		assertThat(deletedTokens()).containsExactly(webToken);
	}

	@Test
	@DisplayName("FCM 토큰 없이 로그아웃하면(앱 웹뷰·구버전 클라이언트) 모든 기기 토큰을 삭제한다")
	void logoutWithoutTokenDeletesAllDevices() {
		memberCommandService.logout(1L, null);

		assertThat(deletedTokens()).containsExactlyInAnyOrder(webToken, appToken);
	}

	@SuppressWarnings("unchecked")
	private List<MemberToken> deletedTokens() {
		ArgumentCaptor<Iterable<MemberToken>> captor = ArgumentCaptor.forClass(Iterable.class);
		verify(memberTokenRepository).deleteAll(captor.capture());
		return (List<MemberToken>)captor.getValue();
	}
}
