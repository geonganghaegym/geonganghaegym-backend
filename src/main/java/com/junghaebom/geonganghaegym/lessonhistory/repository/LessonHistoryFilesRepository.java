package com.junghaebom.geonganghaegym.lessonhistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryFiles;

@Repository
public interface LessonHistoryFilesRepository extends JpaRepository<LessonHistoryFiles, Long> {
}
