package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommandUpdateMemo(
	@Schema(description = "메모내용", example = "메모메모")
	String memo
) {
}
