package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import java.util.List;

import com.junghaebom.geonganghaegym.workout.domain.CompletedExercise;

public interface CompletedExerciseRepositoryCustom {

	List<CompletedExercise> getCompletedExercise(List<Long> ids);
}
