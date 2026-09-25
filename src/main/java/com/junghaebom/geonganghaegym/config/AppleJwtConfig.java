package com.junghaebom.geonganghaegym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * 애플 소셜 로그인의 id_token 은 클라이언트가 보내오는 값이므로 서명을 검증해야 한다.
 * 검증 없이 payload 의 sub 를 신뢰하면 임의로 서명한 토큰으로 타인의 세션을 발급받을 수 있다.
 * <p>
 * JWK 는 NimbusJwtDecoder 가 캐시한다. 애플 JWKS 엔드포인트에 접근할 수 없으면
 * 애플 로그인은 실패(fail closed)한다 — 서명 검증을 건너뛰는 것보다 안전한 선택이다.
 */
@Configuration
public class AppleJwtConfig {

	private static final String APPLE_ISSUER = "https://appleid.apple.com";
	private static final String APPLE_JWK_SET_URI = "https://appleid.apple.com/auth/keys";

	/**
	 * 이 애플리케이션의 자체 액세스 토큰(HS256)을 다루는 JwtTokenProvider 와 섞이지 않도록
	 * 이름을 지정해 애플 전용으로만 주입한다.
	 */
	@Bean
	public JwtDecoder appleJwtDecoder(OAuthProperties oAuthProperties) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(APPLE_JWK_SET_URI)
			.jwsAlgorithm(SignatureAlgorithm.RS256)
			.build();

		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
			new JwtTimestampValidator(),
			new JwtIssuerValidator(APPLE_ISSUER),
			audienceValidator(oAuthProperties)
		));

		return decoder;
	}

	/**
	 * aud 는 애플에 등록한 Service ID(= client_id)여야 한다.
	 * 다른 앱을 위해 발급된 토큰이 우리 서비스에서 통용되는 것을 막는다.
	 */
	private OAuth2TokenValidator<Jwt> audienceValidator(OAuthProperties oAuthProperties) {
		return jwt -> {
			String clientId = oAuthProperties.getApple().getClientId();

			if (jwt.getAudience() != null && jwt.getAudience().contains(clientId)) {
				return OAuth2TokenValidatorResult.success();
			}

			return OAuth2TokenValidatorResult.failure(new OAuth2Error(
				"invalid_token",
				"애플 id_token의 aud가 등록된 client_id와 일치하지 않습니다.",
				null
			));
		};
	}
}
