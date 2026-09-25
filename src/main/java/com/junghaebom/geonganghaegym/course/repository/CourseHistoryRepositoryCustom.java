package com.junghaebom.geonganghaegym.course.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.course.domain.CourseHistory;

public interface CourseHistoryRepositoryCustom {
	Page<CourseHistory> getCourseHistory(Long memberId, Long trainerId, Pageable pageable, String searchDate);

	Long checkPaidOneLesson(Long trainerId, String searchDate);

}
