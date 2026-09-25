package com.junghaebom.geonganghaegym.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.course.domain.CourseHistory;

public interface CourseHistoryRepository extends JpaRepository<CourseHistory, Long>, CourseHistoryRepositoryCustom {

}
