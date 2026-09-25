package com.junghaebom.geonganghaegym.point.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.point.domain.Point;

public interface PointRepositoryCustom {

	Page<Point> getPoint(Long memberId, String searchDate, Pageable pageable);
}
