package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import com.junghaebom.geonganghaegym.common.LessonTimeFormatter;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public record RetrieveApplicantSchedule(
	Long studentId,
	String studentName,
	Long scheduleId,
	String lessonDt,
	String lessonTime,
	String attendanceStatus
) {
	public static RetrieveApplicantSchedule from(Schedule schedule) {
		return new RetrieveApplicantSchedule(
			schedule.getApplicant() != null ? schedule.getApplicant().getId() : null,
			schedule.getApplicant() != null ? schedule.getApplicant().getName() : null,
			schedule.getId(),
			LessonTimeFormatter.formatLessonDt(schedule.getLessonDt()),
			LessonTimeFormatter.formatLessonTime(schedule.getLessonStartTime(), schedule.getLessonEndTime()),
			formatReservationStatus(schedule.getReservationStatus())
		);
	}

	private static String formatReservationStatus(ReservationStatus reservationStatus) {
		if (reservationStatus == ReservationStatus.COMPLETED) {
			return "출석";
		} else if (reservationStatus == ReservationStatus.NO_SHOW) {
			return "미출석";
		} else {
			return reservationStatus.getDescription();
		}
	}
}
