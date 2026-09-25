package com.junghaebom.geonganghaegym.member.presentation.dto.in;

public record AppleToken(
	String access_token,
	String token_type,
	int expires_in,
	String refresh_token
) {
}
