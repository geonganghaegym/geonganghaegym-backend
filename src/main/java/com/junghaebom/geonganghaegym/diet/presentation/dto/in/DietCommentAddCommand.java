package com.junghaebom.geonganghaegym.diet.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record DietCommentAddCommand(
	@Schema(description = "부모 댓글 아이디", example = "1") Long parentCommentId,
	@Schema(description = "댓글 내용", example = "댓글입니다.")
	@NotEmpty(message = "내용을 입력해 주세요.") String content
) {
}
