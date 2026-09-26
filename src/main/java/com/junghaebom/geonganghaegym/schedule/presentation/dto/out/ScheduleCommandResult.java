package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import static com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.util.ObjectUtils;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public record ScheduleCommandResult(
	Long scheduleId,
	LocalDate lessonDt,
	LocalTime lessonStartTime,
	LocalTime lessonEndTime,
	ReservationStatus reservationStatus,
	String trainerName,
	String applicantName,
	String waitingByName
) {
	public static ScheduleCommandResult from(Schedule entity, Member member) {
		Long scheduleId = entity.getId();
		LocalDate lessonDt = entity.getLessonDt();
		LocalTime lessonStartTime = entity.getLessonStartTime();
		LocalTime lessonEndTime = entity.getLessonEndTime();
		ReservationStatus reservationStatus = null;
		String trainerName = null;
		String applicantName = null;
		String waitingByName = null;

		if (!ObjectUtils.isEmpty(entity.getReservationStatus())) {
			reservationStatus = entity.getReservationStatus();
		}

		if (!ObjectUtils.isEmpty(entity.getTrainer())) {
			trainerName = entity.getTrainer().getName() + " 트레이너";
		}

		if (!ObjectUtils.isEmpty(entity.getApplicant())) {
			if (entity.getApplicant().getId().equals(member.getId()) && entity.getReservationStatus()
				.equals(COMPLETED)) {
				reservationStatus = SOLD_OUT;
			} else {
				reservationStatus = entity.getReservationStatus();
			}
			applicantName = entity.getApplicant().getName();
		}

		if (!ObjectUtils.isEmpty(entity.getScheduleWaiting())) {
			waitingByName = entity.getScheduleWaiting().get(0).getMember().getName();
		}

		return new ScheduleCommandResult(scheduleId, lessonDt, lessonStartTime, lessonEndTime,
			reservationStatus, trainerName, applicantName, waitingByName);
	}

	public ScheduleCommandResult withReservationStatus(ReservationStatus newStatus) {
		return new ScheduleCommandResult(scheduleId, lessonDt, lessonStartTime, lessonEndTime,
			newStatus, trainerName, applicantName, waitingByName);
	}
}
