package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 찾기 DTO")
public record CommandFindMemberPassword(
	@Schema(description = "이메일", example = "geonganghaegym@example.com")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "올바른 이메일 형식을 입력해 주세요.")
	@NotEmpty(message = "이메일을 입력해 주세요.")
	String email,

	@Schema(description = "실명", example = "홍길동")
	@NotEmpty(message = "실명을 입력해 주세요.")
	String name
) {
}
