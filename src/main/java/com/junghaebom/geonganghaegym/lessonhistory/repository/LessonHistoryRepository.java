package com.junghaebom.geonganghaegym.lessonhistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistory;

public interface LessonHistoryRepository extends JpaRepository<LessonHistory, Long>, LessonHistoryRepositoryCustom {
	LessonHistory findByIdAndTrainerId(Long lessonHistoryId, Long trainerId);
}
