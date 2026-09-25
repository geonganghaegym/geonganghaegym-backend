package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out;

import java.util.List;

public record CustomRetrieveLessonHistoryByDateCondResult(
	String studentName,
	List<RetrieveLessonHistoryByDateCondResult> content
) {
	public static CustomRetrieveLessonHistoryByDateCondResult from(List<RetrieveLessonHistoryByDateCondResult> entity) {
		String studentName = entity.isEmpty() ? null : entity.get(0).student();
		return new CustomRetrieveLessonHistoryByDateCondResult(studentName, entity);
	}
}
