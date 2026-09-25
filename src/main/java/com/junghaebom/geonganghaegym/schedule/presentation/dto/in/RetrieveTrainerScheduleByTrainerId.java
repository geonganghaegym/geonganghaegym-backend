package com.junghaebom.geonganghaegym.schedule.presentation.dto.in;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

public record RetrieveTrainerScheduleByTrainerId(
	@Schema(description = "조회할 수업 시작 일자", example = "2024-04-01")
	LocalDate lessonStartDt,

	@Schema(description = "조회할 수업 종료 일자", example = "2024-04-30")
	LocalDate lessonEndDt
) {
	public RetrieveTrainerScheduleByTrainerId {
		if (lessonStartDt != null && lessonEndDt != null) {
			if (ChronoUnit.DAYS.between(lessonStartDt, lessonEndDt) > 31) {
				throw new CustomException(ErrorCode.SEARCH_LESS_THAN_31_DAYS);
			}
		}
	}

	public RetrieveTrainerScheduleByTrainerId() {
		this(null, null);
	}

	public void validate() {
		if (ChronoUnit.DAYS.between(lessonStartDt, lessonEndDt) > 31) {
			throw new CustomException(ErrorCode.SEARCH_LESS_THAN_31_DAYS);
		}
	}
}
