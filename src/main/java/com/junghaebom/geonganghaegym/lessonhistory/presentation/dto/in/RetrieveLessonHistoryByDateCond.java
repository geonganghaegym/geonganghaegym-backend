package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "조회 조건 DTO")
public record RetrieveLessonHistoryByDateCond(

	@Schema(description = "조회 날짜", example = "YYYY-MM", required = false)
	String searchDate
) {
	public RetrieveLessonHistoryByDateCond {
		if (searchDate == null || searchDate.isBlank()) {
			searchDate = formatDate(LocalDate.now());
		}
	}

	private static String formatDate(LocalDate lessonDt) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM", Locale.KOREAN);
		return lessonDt.format(formatter);
	}
}
