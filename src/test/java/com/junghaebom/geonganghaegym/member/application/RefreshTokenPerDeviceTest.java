package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.config.jwt.JwtTokenGenerator;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandLogout;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandRefreshToken;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.push.repository.MemberTokenRepository;

/**
 * 같은 계정으로 여러 기기(공유 체험 계정 포함)가 로그인해도 서로의 갱신 토큰을 덮어쓰거나 지우지 않는지 검증한다.
 * Redis는 TTL까지 기록하는 메모리 구현으로 바꿔 끼운다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTokenPerDeviceTest {

	private static final long REFRESH_TOKEN_VALID_SECONDS = 604800L;
	private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

	@Spy
	private InMemoryRedisService redisService = new InMemoryRedisService();
	@Spy
	private JwtTokenGenerator tokenGenerator =
		new JwtTokenGenerator(7200L, REFRESH_TOKEN_VALID_SECONDS, SECRET, redisService);
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private MemberTokenRepository memberTokenRepository;
	@InjectMocks
	private MemberAuthCommandService authService;
	@InjectMocks
	private MemberCommandService memberService;

	private final Member member = Member.builder().id(1L).userId("healthy-trainer0")
		.memberType(MemberType.TRAINER).build();

	@BeforeEach
	void setUp() {
		when(memberRepository.findByUserId("healthy-trainer0")).thenReturn(Optional.of(member));
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
	}

	@Test
	@DisplayName("같은 계정으로 두 기기가 로그인해도 먼저 로그인한 기기의 갱신 토큰이 계속 유효하다")
	void secondLoginDoesNotOverwriteFirstDevice() {
		String first = tokenGenerator.create(member).getRefreshToken();
		String second = tokenGenerator.create(member).getRefreshToken();

		assertThatCode(() -> authService.refreshToken(new CommandRefreshToken("healthy-trainer0", first)))
			.doesNotThrowAnyException();
		assertThatCode(() -> authService.refreshToken(new CommandRefreshToken("healthy-trainer0", second)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("한 기기가 로그아웃하면 그 기기의 갱신 토큰만 무효가 되고 다른 기기는 계속 갱신한다")
	void logoutRevokesOnlyThatDevice() {
		String first = tokenGenerator.create(member).getRefreshToken();
		String second = tokenGenerator.create(member).getRefreshToken();

		memberService.logout(1L, new CommandLogout(null, first));

		assertThatThrownBy(() -> authService.refreshToken(new CommandRefreshToken("healthy-trainer0", first)))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(REFRESH_TOKEN_NOT_FOUND);
		assertThatCode(() -> authService.refreshToken(new CommandRefreshToken("healthy-trainer0", second)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("다른 계정의 갱신 토큰을 내 아이디로 쓰면 거절하고, 그 토큰을 내 로그아웃으로 지울 수도 없다")
	void refreshTokenIsBoundToOwner() {
		Member other = Member.builder().id(2L).userId("other").memberType(MemberType.STUDENT).build();
		String othersToken = tokenGenerator.create(other).getRefreshToken();

		assertThatThrownBy(() -> authService.refreshToken(new CommandRefreshToken("healthy-trainer0", othersToken)))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode").isEqualTo(REFRESH_TOKEN_NOT_VALID);

		memberService.logout(1L, new CommandLogout(null, othersToken));
		assertThat(redisService.getValues(JwtTokenGenerator.refreshTokenKey(othersToken))).isEqualTo("other");
	}

	@Test
	@DisplayName("갱신 토큰은 만료 시각이 아니라 유효 기간(ms)을 TTL로 저장한다")
	void refreshTokenTtlIsValidityDuration() {
		String token = tokenGenerator.create(member).getRefreshToken();

		assertThat(redisService.timeouts.get(JwtTokenGenerator.refreshTokenKey(token)))
			.isEqualTo(REFRESH_TOKEN_VALID_SECONDS * 1000);
	}

	static class InMemoryRedisService extends RedisService {
		private final Map<String, String> values = new HashMap<>();
		private final Map<String, Long> timeouts = new HashMap<>();

		InMemoryRedisService() {
			super(null);
		}

		@Override
		public void setValuesWithTimeout(String key, String value, long timeout) {
			values.put(key, value);
			timeouts.put(key, timeout);
		}

		@Override
		public String getValues(String key) {
			return values.get(key);
		}

		@Override
		public void deleteValues(String key) {
			values.remove(key);
		}
	}
}
