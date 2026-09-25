package com.junghaebom.geonganghaegym.schedule.repository.waiting;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.schedule.domain.ScheduleWaiting;

public interface ScheduleWaitingRepository
	extends JpaRepository<ScheduleWaiting, Long>, ScheduleWaitingRepositoryCustom {
	Optional<ScheduleWaiting> findByScheduleIdAndMemberId(Long scheduleId, Long memberId);

	@EntityGraph(attributePaths = {"member"})
	Optional<ScheduleWaiting> findByScheduleId(Long scheduleId);

	void deleteByMemberId(Long memberId);

}
