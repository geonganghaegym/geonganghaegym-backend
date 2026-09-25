package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.junghaebom.geonganghaegym.common.LessonDetailResultSerializer;
import com.junghaebom.geonganghaegym.common.LessonTimeFormatter;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public record RetrieveTrainerScheduleByLessonInfoResult(
	String trainerName,
	String earliestLessonStartTime,
	String latestLessonEndTime,
	Map<LocalDate, List<LessonDetailResult>> schedule
) {
	private static final double DEFAULT_DURATION = 60.0;

	public static RetrieveTrainerScheduleByLessonInfoResult from(List<Schedule> schedules) {
		LocalTime firstLessonStartTime = schedules.stream()
			.filter(s -> s.getReservationStatus() != ReservationStatus.DISABLED)
			.map(Schedule::getLessonStartTime)
			.min(LocalTime::compareTo)
			.orElse(null);

		LocalTime lastLessonEndTime = schedules.stream()
			.filter(s -> s.getReservationStatus() != ReservationStatus.DISABLED)
			.map(Schedule::getLessonEndTime)
			.max(LocalTime::compareTo)
			.orElse(null);

		Map<LocalDate, List<LessonDetailResult>> groupingSchedules = schedules.stream()
			.collect(Collectors.groupingBy(
				Schedule::getLessonDt,
				LinkedHashMap::new,
				Collectors.mapping(s -> new LessonDetailResult(
					s.getId(),
					calculateDuration(s),
					s.getLessonStartTime(),
					s.getLessonEndTime(),
					s.getReservationStatus(),
					s.getApplicant() != null ? s.getApplicant().getId() : null,
					s.getApplicant() != null ? s.getApplicant().getName() : null,
					s.getScheduleWaiting() != null && !s.getScheduleWaiting().isEmpty()
						? s.getScheduleWaiting().get(0).getMember().getId() : null,
					s.getScheduleWaiting() != null && !s.getScheduleWaiting().isEmpty()
						? s.getScheduleWaiting().get(0).getMember().getName() : null
				), Collectors.toList())
			));

		String trainerName = schedules.isEmpty() ? null
			: schedules.get(0).getTrainer() != null
			  ? schedules.get(0).getTrainer().getName() + " 트레이너"
			  : null;

		return new RetrieveTrainerScheduleByLessonInfoResult(
			trainerName,
			LessonTimeFormatter.formatLessonTime(firstLessonStartTime),
			LessonTimeFormatter.formatLessonTime(lastLessonEndTime),
			groupingSchedules.isEmpty() ? null : groupingSchedules
		);
	}

	private static double calculateDuration(Schedule schedule) {
		double duration = Duration.between(schedule.getLessonStartTime(), schedule.getLessonEndTime())
			.toMinutes() / DEFAULT_DURATION;
		if (duration < 0) {
			return duration + 24;
		}
		return duration;
	}

	@JsonSerialize(using = LessonDetailResultSerializer.class)
	public record LessonDetailResult(
		Long scheduleId,
		Double duration,
		LocalTime lessonStartTime,
		LocalTime lessonEndTime,
		ReservationStatus reservationStatus,
		Long applicantId,
		String applicantName,
		Long waitingStudentId,
		String waitingStudentName
	) {
	}
}
