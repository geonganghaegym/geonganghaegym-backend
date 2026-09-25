package com.junghaebom.geonganghaegym.course.presentation.dto.in;

import com.junghaebom.geonganghaegym.course.domain.CourseHistoryType;
import com.junghaebom.geonganghaegym.point.domain.Calculation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CourseUpdateCommand(
	@Schema(description = "학생 ID", example = "1")
	@NotNull
	Long memberId,

	@Schema(description = "추가 및 증감", example = "PLUS, MINUS")
	@NotNull
	Calculation calculation,

	@Schema(description = "증감 타입", example = "COURSE_CREATE(수강권 생성), PLUS_CNT(횟수 추가), MINUS_CNT(횟수 차감), ONE_LESSON(1회 수강권 지급)")
	@NotNull
	CourseHistoryType type,

	@Schema(description = "추가 및 차감 할 횟수", example = "10")
	int updateCnt
) {

	public static CourseUpdateCommand create(Long memberId, Calculation calculation, CourseHistoryType type,
		int updateCnt) {
		return new CourseUpdateCommand(memberId, calculation, type, updateCnt);
	}
}
