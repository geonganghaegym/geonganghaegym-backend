package com.junghaebom.geonganghaegym.trainer.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record MemberLessonCommand(
	@Schema(description = "수업할 PT 횟수", example = "10")
	@Positive(message = "양수를 입력해주세요.")
	int lessonCnt
) {
}
