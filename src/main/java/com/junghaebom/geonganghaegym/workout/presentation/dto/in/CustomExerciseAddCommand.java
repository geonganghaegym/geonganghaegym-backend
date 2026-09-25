package com.junghaebom.geonganghaegym.workout.presentation.dto.in;

import com.junghaebom.geonganghaegym.workout.domain.ExerciseCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomExerciseAddCommand(
	@Schema(description = "카테고리", example = "CORE")
	@NotNull
	ExerciseCategory category,

	@Schema(description = "운동명", example = "버피테스트")
	@NotBlank
	String names,

	@Schema(description = "사용근육", example = "전신")
	String muscles
) {
}
