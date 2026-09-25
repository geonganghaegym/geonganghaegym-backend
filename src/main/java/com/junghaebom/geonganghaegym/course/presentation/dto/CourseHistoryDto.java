package com.junghaebom.geonganghaegym.course.presentation.dto;

import java.time.LocalDateTime;

import com.junghaebom.geonganghaegym.course.domain.CourseHistory;
import com.junghaebom.geonganghaegym.course.domain.CourseHistoryType;
import com.junghaebom.geonganghaegym.point.domain.Calculation;

public record CourseHistoryDto(
	Long courseHistoryId,
	int cnt,
	Calculation calculation,
	CourseHistoryType type,
	LocalDateTime createdAt
) {

	public static CourseHistoryDto from(CourseHistory courseHistory) {
		return new CourseHistoryDto(
			courseHistory.getCourseHistoryId(),
			courseHistory.getCnt(),
			courseHistory.getCalculation(),
			courseHistory.getType(),
			courseHistory.getCreatedAt()
		);
	}
}
