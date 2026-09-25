package com.junghaebom.geonganghaegym.schedule.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;

public record RetrieveTrainerScheduleByLessonDt(
	@Schema(description = "조회할 수업 일자", example = "2024-04-01")
	String lessonDt
) {
	public static RetrieveTrainerScheduleByLessonDt of(String lessonDt) {
		return new RetrieveTrainerScheduleByLessonDt(lessonDt);
	}
}
