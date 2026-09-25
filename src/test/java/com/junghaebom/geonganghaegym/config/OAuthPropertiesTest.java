package com.junghaebom.geonganghaegym.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * 클라이언트가 보낸 redirect URL은 허용 origin + 콜백 경로와 정확히 일치할 때만 토큰 교환에 쓰인다.
 */
class OAuthPropertiesTest {

	private static final String APPLE_PATH = "/api/callback/apple";
	private static final String FALLBACK = "https://geonganghaejim.site/api/callback/apple";

	@ParameterizedTest
	@ValueSource(strings = {
		"https://geonganghaejim.site/api/callback/apple",
		"https://health.junghaebom.com/api/callback/apple",
		"https://geonganghaegym.junghaebom.com/api/callback/apple"
	})
	@DisplayName("허용된 origin의 콜백 주소는 그대로 사용한다")
	void allowedOrigin(String requested) {
		assertEquals(requested, OAuthProperties.resolveRedirectUri(requested, APPLE_PATH, FALLBACK));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"https://evil.example.com/api/callback/apple",
		"https://geonganghaejim.site.evil.com/api/callback/apple",
		"http://geonganghaejim.site/api/callback/apple",
		"https://geonganghaejim.site/kakao/callback",
		"https://geonganghaejim.site/api/callback/apple?next=https://evil.example.com"
	})
	@DisplayName("허용되지 않은 호스트·스킴·경로면 설정값으로 폴백한다")
	void foreignRedirectFallsBack(String requested) {
		assertEquals(FALLBACK, OAuthProperties.resolveRedirectUri(requested, APPLE_PATH, FALLBACK));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"  "})
	@DisplayName("redirect URL이 비어 있으면 설정값으로 폴백한다")
	void blankFallsBack(String requested) {
		assertEquals(FALLBACK, OAuthProperties.resolveRedirectUri(requested, APPLE_PATH, FALLBACK));
	}

	@Test
	@DisplayName("카카오·구글은 각자의 콜백 경로로 검증한다")
	void providerSpecificPath() {
		String kakao = "https://health.junghaebom.com/kakao/callback";
		assertEquals(kakao, OAuthProperties.resolveRedirectUri(kakao, "/kakao/callback", null));
		assertNull(OAuthProperties.resolveRedirectUri(kakao, "/google/callback", null));
	}
}
