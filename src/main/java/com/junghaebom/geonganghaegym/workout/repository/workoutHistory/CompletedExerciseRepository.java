package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.workout.domain.CompletedExercise;

public interface CompletedExerciseRepository
	extends JpaRepository<CompletedExercise, Long>, CompletedExerciseRepositoryCustom {

}
