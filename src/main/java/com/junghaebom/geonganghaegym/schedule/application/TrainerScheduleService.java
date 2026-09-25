package com.junghaebom.geonganghaegym.schedule.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.KotlinCustomPaging;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.RetrieveTrainerScheduleByLessonDt;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.RetrieveTrainerScheduleByLessonInfo;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.RetrieveTrainerScheduleByTrainerId;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveApplicantSchedule;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerDefaultLessonTimeResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonDtResult;
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

	public RetrieveTrainerScheduleByLessonInfoResult findAllSchedule(
		Long trainerId,
		RetrieveTrainerScheduleByTrainerId request
	) {
		List<Schedule> schedules = trainerScheduleRepository.findAllSchedule(
			null,
			request.lessonStartDt(),
			request.lessonEndDt(),
			trainerId
		);
		return RetrieveTrainerScheduleByLessonInfoResult.from(schedules);
	}

	public RetrieveTrainerScheduleByLessonDtResult findOneTrainerTodaySchedule(
		RetrieveTrainerScheduleByLessonDt request,
		Long trainerId
	) {
		return trainerScheduleRepository.findOneTrainerTodaySchedule(request.lessonDt(), trainerId);
	}

	public KotlinCustomPaging<RetrieveApplicantSchedule> findAllScheduleByStudentId(
		Long studentId,
		Pageable pageable,
		Long trainerId
	) {
		Page<Schedule> schedules = trainerScheduleRepository.findAllScheduleByStudentId(studentId, pageable, trainerId);
		Page<RetrieveApplicantSchedule> contents = schedules.map(RetrieveApplicantSchedule::from);

		return new KotlinCustomPaging<>(
			contents.getContent(),
			contents.getPageable().getPageNumber(),
			contents.getPageable().getPageSize(),
			contents.getTotalPages(),
			contents.getTotalElements(),
			contents.isLast()
		);
	}
}
