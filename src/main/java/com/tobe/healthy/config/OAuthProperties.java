package com.tobe.healthy.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.ToString;

@Component
@ConfigurationProperties("oauth")
@Data
@ToString
public class OAuthProperties {

	/**
	 * 같은 프론트엔드 배포가 서비스되는 origin 목록. 프론트엔드 entity/auth/consts.ts 와 맞춘다.
	 */
	private static final List<String> ALLOWED_REDIRECT_ORIGINS = List.of(
		"https://geonganghaejim.site",
		"https://health.junghaebom.com",
		"https://geonganghaegym.junghaebom.com"
	);

	private OAuthServiceProperties kakao;
	private OAuthServiceProperties naver;
	private OAuthServiceProperties google;
	private AppleProperties apple;

	/**
	 * 애플은 client_secret을 매 요청마다 ES256으로 서명해 생성하기 때문에
	 * 다른 소셜과 설정 항목이 다르다.
	 */
	@Data
	@ToString
	public static class AppleProperties {
		private String teamId;
		private String clientId;
		private String keyPath;
		private String loginKey;
		private String redirectUri;
	}

	@Data
	@ToString
	public static class OAuthServiceProperties {
		private String grantType;
		private String clientId;
		private String clientSecret;
		private String redirectUri; // Google과 Kakao에만 존재
		private String tokenUri;
		private String userInfoUri;
		private String adminKey;

		public String getAdminKey() {
			return adminKey;
		}
	}

	/**
	 * 클라이언트가 보낸 redirect URL이 허용 origin + callbackPath 와 정확히 일치할 때만 쓰고,
	 * 아니면(비어 있거나 다른 호스트·경로) 설정값으로 폴백한다. 임의 URL 주입을 막는다.
	 */
	public static String resolveRedirectUri(String requested, String callbackPath, String fallback) {
		boolean allowed = requested != null && ALLOWED_REDIRECT_ORIGINS.stream()
			.anyMatch(origin -> requested.equals(origin + callbackPath));
		return allowed ? requested : fallback;
	}
}
