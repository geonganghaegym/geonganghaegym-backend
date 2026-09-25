package com.junghaebom.geonganghaegym.schedule.presentation.dto.in;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommandRegisterSchedule(
	@Schema(description = "시작 수업 일자", example = "2024-04-01") LocalDate lessonStartDt,
	@Schema(description = "종료 수업 일자", example = "2024-04-30") LocalDate lessonEndDt
) {
	public CommandRegisterSchedule {
		if (lessonStartDt != null && lessonEndDt != null) {
			validate(lessonStartDt, lessonEndDt);
		}
	}

	public void validate() {
		if (lessonStartDt.isAfter(lessonEndDt)) {
			throw new CustomException(ErrorCode.START_DATE_AFTER_END_DATE);
		}
		if (ChronoUnit.DAYS.between(lessonStartDt, lessonEndDt) > 31) {
			throw new CustomException(ErrorCode.SCHEDULE_LESS_THAN_31_DAYS);
		}
	}

	private static void validate(LocalDate start, LocalDate end) {
		if (start.isAfter(end)) throw new CustomException(ErrorCode.START_DATE_AFTER_END_DATE);
		if (ChronoUnit.DAYS.between(start, end) > 31) throw new CustomException(ErrorCode.SCHEDULE_LESS_THAN_31_DAYS);
	}
}
