package com.junghaebom.geonganghaegym.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.schedule.domain.TrainerScheduleInfo;

public interface TrainerScheduleInfoRepository extends JpaRepository<TrainerScheduleInfo, Long> {
	TrainerScheduleInfo findOneByTrainerId(Long trainerId);
}
