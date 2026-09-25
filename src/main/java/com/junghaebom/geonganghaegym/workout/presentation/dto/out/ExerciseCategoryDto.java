package com.junghaebom.geonganghaegym.workout.presentation.dto.out;

import com.junghaebom.geonganghaegym.workout.domain.ExerciseCategory;

public record ExerciseCategoryDto(
	String category,
	String name
) {

	public static ExerciseCategoryDto from(ExerciseCategory exerciseCategory) {
		return new ExerciseCategoryDto(
			exerciseCategory.getCode(),
			exerciseCategory.getDescription()
		);
	}
}
