package com.junghaebom.geonganghaegym.schedule.presentation.dto.in;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record StudentScheduleCond(
	@Schema(description = "조회할 수업 일자", example = "2024-04")
	String lessonDt,

	@Schema(description = "조회할 수업 시작 일자", example = "2024-04-01")
	LocalDate lessonStartDt,

	@Schema(description = "조회할 수업 종료 일자", example = "2024-04-30")
	LocalDate lessonEndDt,

	Long courseId
) {
	public StudentScheduleCond() {
		this(null, null, null, null);
	}
}
