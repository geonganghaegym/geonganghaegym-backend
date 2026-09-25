package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record CommandChangeEmail(
	@NotEmpty(message = "이메일을 입력해 주세요.")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "올바른 이메일 형식을 입력해 주세요.")
	String email,

	@NotEmpty(message = "이메일 인증번호를 입력해 주세요.")
	String emailKey
) {
}
