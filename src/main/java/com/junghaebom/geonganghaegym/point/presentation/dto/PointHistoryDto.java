package com.junghaebom.geonganghaegym.point.presentation.dto;

import java.time.LocalDateTime;

import com.junghaebom.geonganghaegym.point.domain.Calculation;
import com.junghaebom.geonganghaegym.point.domain.Point;
import com.junghaebom.geonganghaegym.point.domain.PointType;

public record PointHistoryDto(
	Long pointId,
	PointType type,
	Calculation calculation,
	int point,
	LocalDateTime createdAt
) {
	public static PointHistoryDto from(Point point) {
		return new PointHistoryDto(
			point.getPointId(),
			point.getType(),
			point.getCalculation(),
			point.getPoint(),
			point.getCreatedAt()
		);
	}
}
