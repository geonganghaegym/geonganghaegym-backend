package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import com.junghaebom.geonganghaegym.member.domain.MemberType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 DTO")
public record CommandSocialLogin(
	@Schema(description = "인가코드(카카오, 네이버, 구글 소셜 로그인시 필요)", example = "인가코드(카카오, 네이버, 구글 소셜 로그인시 필요)")
	String code,

	@Schema(description = "상태코드(CSRF 방지, 네이버 소셜 로그인시 필요)", example = "STATE_STRING(네이버 로그인시 필요)")
	String state,

	@Schema(description = "회원구분(모든 소셜 로그인시 필요)", example = "STUDENT || TRAINER(모든 소셜 로그인시 필요)")
	MemberType memberType,

	@Schema(description = "Redirect URL(카카오, 구글 소셜 로그인시 필요)", example = "http://localhost:3000/kakao/callback(카카오, 구글 소셜 로그인시 필요)")
	String redirectUrl,

	@Schema(description = "초대링크로 가입하는 경우 uuid")
	String uuid,

	@Schema(description = "애플 토큰 데이터")
	String id_token,

	CommandAppleUserInfo user
) {
}
