package com.junghaebom.geonganghaegym.lessonhistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryComment;

@Repository
public interface LessonHistoryCommentRepository extends JpaRepository<LessonHistoryComment, Long>,
	LessonHistoryCommentRepositoryCustom {
}
