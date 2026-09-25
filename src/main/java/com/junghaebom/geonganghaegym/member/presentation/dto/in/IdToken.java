package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IdToken(
	Long id,
	String aud,
	String sub,

	@JsonProperty("auth_time")
	int authTime,

	String iss,
	String nickname,
	int exp,
	int iat,
	String picture,
	String email,
	String name,

	@JsonProperty("c_hash")
	String cHash,

	@JsonProperty("email_verified")
	boolean emailVerified,

	@JsonProperty("nonce_supported")
	boolean nonceSupported,

	@JsonProperty("is_private_email")
	boolean isPrivateEmail
) {

	/**
	 * 애플 id_token 은 서명 검증을 거친 클레임에서만 만든다.
	 * 애플이 내려주지 않는 항목(nickname, picture 등)은 비워 둔다.
	 */
	public static IdToken ofApple(String sub, String email, String aud, String iss, boolean emailVerified) {
		return new IdToken(null, aud, sub, 0, iss, null, 0, 0, null, email, null, null,
			emailVerified, false, false);
	}
}
