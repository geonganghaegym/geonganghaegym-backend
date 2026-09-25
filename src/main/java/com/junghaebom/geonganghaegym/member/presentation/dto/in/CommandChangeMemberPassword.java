package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "비밀번호 변경 DTO")
public record CommandChangeMemberPassword(
	@Schema(description = "변경할 비밀번호", example = "12345678aaa")
	@NotEmpty(message = "변경할 비밀번호를 입력해 주세요.")
	String changePassword1,

	@Schema(description = "변경할 비밀번호", example = "12345678aaa")
	@NotEmpty(message = "변경할 비밀번호를 다시 입력해 주세요.")
	String changePassword2
) {
}
