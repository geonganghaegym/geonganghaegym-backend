package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryComment;

public interface WorkoutHistoryCommentRepositoryCustom {

	Page<WorkoutHistoryComment> getCommentsByWorkoutHistoryId(Long workoutHistoryId, Pageable pageable);
}
