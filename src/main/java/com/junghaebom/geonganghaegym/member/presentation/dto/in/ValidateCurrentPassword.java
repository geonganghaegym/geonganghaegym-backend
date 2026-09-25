package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import jakarta.validation.constraints.NotEmpty;

public record ValidateCurrentPassword(
	@NotEmpty(message = "현재 비밀번호를 입력해 주세요.")
	String password
) {
}
