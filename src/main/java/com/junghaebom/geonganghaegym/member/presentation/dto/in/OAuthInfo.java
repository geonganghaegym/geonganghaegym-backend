package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthInfo(
	@JsonProperty("access_token")
	String accessToken,

	@JsonProperty("token_type")
	String tokenType,

	@JsonProperty("refresh_token")
	String refreshToken,

	@JsonProperty("id_token")
	String idToken,

	@JsonProperty("expires_in")
	int expiresIn,

	String scope,

	@JsonProperty("refresh_token_expires_in")
	int refreshTokenExpiresIn
) {

	public record NaverUserInfo(
		@JsonProperty("resultcode")
		String resultCode,

		String message,

		NaverUserInfoDetail response
	) {

		public record NaverUserInfoDetail(
			String id,

			@JsonProperty("profile_image")
			String profileImage,

			String email,
			String name
		) { }
	}

	public record KakaoUserInfo(
		Long id,

		@JsonProperty("connected_at")
		String connectedAt,

		Properties properties,

		@JsonProperty("kakao_account")
		KakaoAccount kakaoAccount
	) {

		public record Properties(
			String nickname,

			@JsonProperty("profile_image")
			String profileImage,

			@JsonProperty("thumbnail_image")
			String thumbnailImage
		) { }

		public record KakaoAccount(
			@JsonProperty("profile_nickname_needs_agreement")
			boolean profileNicknameNeedsAgreement,

			@JsonProperty("profile_image_needs_agreement")
			boolean profileImageNeedsAgreement,

			Profile profile,

			@JsonProperty("has_email")
			boolean hasEmail,

			@JsonProperty("email_needs_agreement")
			boolean emailNeedsAgreement,

			@JsonProperty("is_email_valid")
			boolean isEmailValid,

			@JsonProperty("is_email_verified")
			boolean isEmailVerified,

			String email
		) { }

		public record Profile(
			String nickname,

			@JsonProperty("thumbnail_image_url")
			String thumbnailImageUrl,

			@JsonProperty("profile_image_url")
			String profileImageUrl,

			@JsonProperty("is_default_image")
			boolean isDefaultImage,

			@JsonProperty("is_default_nickname")
			boolean isDefaultNickname
		) { }
	}

	/**
	 * 구글 사용자 정보는 엔드포인트 버전에 따라 식별자 키가 다르다.
	 * v2(/oauth2/v2/userinfo)는 id, v3·OIDC(/oauth2/v3/userinfo)는 sub로 내려주므로 둘 다 받는다.
	 */
	public record GoogleUserInfo(
		@JsonAlias("sub")
		String id,

		String email,
		String name,
		String picture
	) { }
}
