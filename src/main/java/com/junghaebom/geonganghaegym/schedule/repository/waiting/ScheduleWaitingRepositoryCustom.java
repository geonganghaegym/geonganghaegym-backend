package com.junghaebom.geonganghaegym.schedule.repository.waiting;

import java.util.List;

import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.MyScheduleWaiting;

public interface ScheduleWaitingRepositoryCustom {
	List<MyScheduleWaiting> findAllMyScheduleWaiting(Long memberId);
}
