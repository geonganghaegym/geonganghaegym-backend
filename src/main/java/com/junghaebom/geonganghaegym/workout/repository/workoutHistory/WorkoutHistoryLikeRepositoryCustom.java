package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

public interface WorkoutHistoryLikeRepositoryCustom {

	Long getLikeCnt(Long workoutHistoryId);

	void deleteLikeByWorkoutHistoryId(Long workoutHistoryId);

}
