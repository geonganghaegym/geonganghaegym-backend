package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistory;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryComment;

public interface WorkoutHistoryCommentRepository
	extends JpaRepository<WorkoutHistoryComment, Long>, WorkoutHistoryCommentRepositoryCustom {

	Optional<WorkoutHistoryComment> findByCommentIdAndMemberIdAndDelYnFalse(Long commentId, Long memberId);

	Long countByWorkoutHistoryAndParentCommentIdAndDelYnFalse(WorkoutHistory history, Long parentCommentId);

	Long countByWorkoutHistory(WorkoutHistory history);

	Optional<WorkoutHistoryComment> findByCommentIdAndDelYnFalse(@Param("commentId") Long parentCommentId);

}
