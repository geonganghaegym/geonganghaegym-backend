package com.junghaebom.geonganghaegym.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public interface TrainerScheduleRepository extends JpaRepository<Schedule, Long>, TrainerScheduleRepositoryCustom {
}
