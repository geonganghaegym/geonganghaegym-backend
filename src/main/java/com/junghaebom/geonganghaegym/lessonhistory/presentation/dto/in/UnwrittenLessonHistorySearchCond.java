package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in;

import com.junghaebom.geonganghaegym.lessonhistory.domain.WritingStatus;

public record UnwrittenLessonHistorySearchCond(
	String lessonDate,
	Long studentId,
	WritingStatus writingStatus
) {
}
