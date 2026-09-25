package com.junghaebom.geonganghaegym.workout.presentation.dto;

import com.junghaebom.geonganghaegym.workout.domain.CompletedExercise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record CompletedExerciseDto(
	@Schema(description = "운동 종류 ID", example = "1")
	Long exerciseId,

	@Schema(description = "운동 종류 이름", example = "90/90 Hamstring")
	String name,

	String names,

	@Schema(description = "세트", example = "3")
	@Positive(message = "숫자를 입력해주세요.")
	int setNum,

	@Schema(description = "무게", example = "20")
	@Positive(message = "숫자를 입력해주세요.")
	int weight,

	@Schema(description = "반복횟수", example = "10")
	@Positive(message = "숫자를 입력해주세요.")
	int numberOfCycles,

	Long workoutHistoryId
) {

	public static CompletedExerciseDto from(CompletedExercise exercise) {
		return new CompletedExerciseDto(
			exercise.getExerciseId(),
			exercise.getName(),
			null,
			exercise.getSetNum(),
			exercise.getWeight(),
			exercise.getNumberOfCycles(),
			exercise.getWorkoutHistory().getWorkoutHistoryId()
		);
	}
}
