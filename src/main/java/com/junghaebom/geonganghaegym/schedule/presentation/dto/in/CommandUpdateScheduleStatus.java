package com.junghaebom.geonganghaegym.schedule.presentation.dto.in;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record CommandUpdateScheduleStatus(
	@NotNull(message = "수업 일정 ID를 입력해 주세요.")
	List<Long> scheduleIds
) {
}
