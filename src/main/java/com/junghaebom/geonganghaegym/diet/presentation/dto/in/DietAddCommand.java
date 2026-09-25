package com.junghaebom.geonganghaegym.diet.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record DietAddCommand(
	@Schema(description = "아침 파일") String breakfastFile,
	@Schema(description = "점심 파일") String lunchFile,
	@Schema(description = "저녁 파일") String dinnerFile,
	@Schema(description = "아침 단식 여부", example = "false") boolean breakfastFast,
	@Schema(description = "점심 단식 여부", example = "false") boolean lunchFast,
	@Schema(description = "저녁 단식 여부", example = "false") boolean dinnerFast,
	@Schema(description = "먹은 날짜", example = "2024-05-28")
	@NotEmpty(message = "날짜를 입력해 주세요.") String eatDate
) {
}
