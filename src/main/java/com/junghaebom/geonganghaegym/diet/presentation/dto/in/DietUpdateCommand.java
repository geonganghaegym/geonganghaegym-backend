package com.junghaebom.geonganghaegym.diet.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;

public record DietUpdateCommand(
	@Schema(description = "아침 파일") String breakfastFile,
	@Schema(description = "점심 파일") String lunchFile,
	@Schema(description = "저녁 파일") String dinnerFile,
	@Schema(description = "아침 단식 여부", example = "false") boolean breakfastFast,
	@Schema(description = "점심 단식 여부", example = "false") boolean lunchFast,
	@Schema(description = "저녁 단식 여부", example = "false") boolean dinnerFast
) {
}
