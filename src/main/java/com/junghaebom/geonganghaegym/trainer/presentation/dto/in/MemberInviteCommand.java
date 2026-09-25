package com.junghaebom.geonganghaegym.trainer.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public record MemberInviteCommand(
	@Schema(description = "이름", example = "임채린")
	@NotEmpty(message = "회원 이름을 추가해 주세요.")
	String name,

	@Schema(description = "수업할 PT 횟수", example = "10")
	@Positive(message = "양수를 입력해주세요.")
	int lessonCnt
) {
}
