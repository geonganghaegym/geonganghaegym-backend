package com.junghaebom.geonganghaegym.course.presentation.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CourseAddCommand(
	@Schema(description = "학생 ID", example = "1")
	@NotNull
	Long memberId,

	@Schema(description = "수업할 PT 횟수", example = "10")
	@Positive(message = "양수를 입력해주세요.")
	int lessonCnt
) {

	public static CourseAddCommand create(Long memberId, int lessonCnt) {
		return new CourseAddCommand(memberId, lessonCnt);
	}
}
