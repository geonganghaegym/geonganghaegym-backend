package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.util.List;

import com.junghaebom.geonganghaegym.member.domain.AlarmStatus;

public record ScheduleCommandResponse(
	AlarmStatus scheduleNoticeStatus,
	List<ScheduleCommandResult> morning,
	List<ScheduleCommandResult> afternoon
) {
	public static ScheduleCommandResponse create(AlarmStatus scheduleNoticeStatus, List<ScheduleCommandResult> morning,
		List<ScheduleCommandResult> afternoon) {
		return new ScheduleCommandResponse(
			scheduleNoticeStatus,
			morning.isEmpty() ? null : morning,
			afternoon.isEmpty() ? null : afternoon
		);
	}
}
