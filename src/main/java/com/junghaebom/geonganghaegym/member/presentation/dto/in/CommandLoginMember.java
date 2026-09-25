package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import com.junghaebom.geonganghaegym.member.domain.MemberType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "로그인 DTO")
public record CommandLoginMember(
	@Schema(description = "아이디", example = "healthy-trainer0")
	@NotEmpty(message = "아이디를 입력해 주세요.")
	String userId,

	@Schema(description = "비밀번호", example = "12345678a")
	@NotEmpty(message = "비밀번호를 입력해 주세요.")
	String password,

	@Schema(description = "회원 구분", example = "TRAINER")
	@NotNull(message = "회원 구분이 필요합니다.")
	MemberType memberType,

	@Schema(description = "체험하기 로그인 여부", example = "true", defaultValue = "false")
	boolean complimentaryLogin
) {
}
