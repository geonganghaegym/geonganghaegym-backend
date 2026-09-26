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
	String waitingByName,
	SoldOutReason soldOutReason
) {
	/** 회원 화면에서 SOLD_OUT(마감)으로 보여주는 이유. 화면이 사유별 문구를 고른다. */
	public enum SoldOutReason {
		PAST, MY_RESERVATION, RESERVATION_CLOSED, WAITING_FULL, WAITING_CLOSED
	}

	public static ScheduleCommandResult from(Schedule entity, Member member) {
		Long scheduleId = entity.getId();
		LocalDate lessonDt = entity.getLessonDt();
		LocalTime lessonStartTime = entity.getLessonStartTime();
		LocalTime lessonEndTime = entity.getLessonEndTime();
		ReservationStatus reservationStatus = null;
		String trainerName = null;
		String applicantName = null;
		String waitingByName = null;
		SoldOutReason soldOutReason = null;

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
				soldOutReason = SoldOutReason.MY_RESERVATION;
			} else {
				reservationStatus = entity.getReservationStatus();
			}
			applicantName = entity.getApplicant().getName();
		}

		if (!ObjectUtils.isEmpty(entity.getScheduleWaiting())) {
			waitingByName = entity.getScheduleWaiting().get(0).getMember().getName();
		}

		return new ScheduleCommandResult(scheduleId, lessonDt, lessonStartTime, lessonEndTime,
			reservationStatus, trainerName, applicantName, waitingByName, soldOutReason);
	}

	public ScheduleCommandResult soldOut(SoldOutReason reason) {
		return new ScheduleCommandResult(scheduleId, lessonDt, lessonStartTime, lessonEndTime,
			SOLD_OUT, trainerName, applicantName, waitingByName, reason);
	}
}
