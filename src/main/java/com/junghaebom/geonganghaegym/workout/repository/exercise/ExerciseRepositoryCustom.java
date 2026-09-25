package com.junghaebom.geonganghaegym.workout.repository.exercise;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.workout.domain.Exercise;
import com.junghaebom.geonganghaegym.workout.domain.ExerciseCategory;

public interface ExerciseRepositoryCustom {

	Page<Exercise> getExercise(Long memberId, ExerciseCategory exerciseCategory, Pageable pageable, String searchValue);

}
