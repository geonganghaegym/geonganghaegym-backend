package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryLike;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryLikePK;

public interface WorkoutHistoryLikeRepository
	extends JpaRepository<WorkoutHistoryLike, WorkoutHistoryLikePK>, WorkoutHistoryLikeRepositoryCustom {

}
