package com.junghaebom.geonganghaegym.workout.presentation.dto.in;

import java.util.List;

import com.junghaebom.geonganghaegym.workout.presentation.dto.CompletedExerciseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record HistoryAddCommand(
	@Schema(description = "게시글 내용", example = "오운완~!!")
	String content,

	@Schema(description = "완료한 운동 목록")
	@NotEmpty(message = "운동을 추가해 주세요.")
	List<CompletedExerciseDto> completedExercises,

	List<RegisterFile> files,

	boolean viewMySelf
) {
}
