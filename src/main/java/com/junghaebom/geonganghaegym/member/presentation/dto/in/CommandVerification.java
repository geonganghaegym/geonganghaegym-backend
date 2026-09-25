package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import jakarta.validation.constraints.NotEmpty;

public record CommandVerification(
	@NotEmpty String email,
	@NotEmpty String emailKey
) {
}
