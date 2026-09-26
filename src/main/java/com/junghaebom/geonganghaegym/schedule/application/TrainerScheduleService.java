package com.junghaebom.geonganghaegym.schedule.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.RetrieveTrainerScheduleByLessonInfo;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerDefaultLessonTimeResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonInfoResult;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.domain.TrainerScheduleInfo;
import com.junghaebom.geonganghaegym.schedule.repository.TrainerScheduleInfoRepository;
import com.junghaebom.geonganghaegym.schedule.repository.TrainerScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TrainerScheduleService {

	private final TrainerScheduleRepository trainerScheduleRepository;
	private final TrainerScheduleInfoRepository trainerScheduleInfoRepository;

	public RetrieveTrainerDefaultLessonTimeResult findOneDefaultLessonTime(Long trainerId) {
		TrainerScheduleInfo trainerScheduleInfo = trainerScheduleInfoRepository.findOneByTrainerId(trainerId);
		return RetrieveTrainerDefaultLessonTimeResult.from(trainerScheduleInfo);
	}

	public RetrieveTrainerScheduleByLessonInfoResult findAllSchedule(
		RetrieveTrainerScheduleByLessonInfo request,
		Long trainerId
	) {
		List<Schedule> schedules = trainerScheduleRepository.findAllSchedule(
			request.lessonDt(),
			request.lessonStartDt(),
			request.lessonEndDt(),
			trainerId
		);
		return RetrieveTrainerScheduleByLessonInfoResult.from(schedules);
	}
}
