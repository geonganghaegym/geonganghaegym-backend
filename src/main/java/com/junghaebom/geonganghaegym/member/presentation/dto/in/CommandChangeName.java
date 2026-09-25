package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import jakarta.validation.constraints.NotEmpty;

public record CommandChangeName(
	@NotEmpty String name
) {
}
