package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out;

import java.util.List;

import com.junghaebom.geonganghaegym.common.LessonTimeFormatter;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistory;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public record RetrieveUnwrittenLessonHistory(
	Long scheduleId,
	Long studentId,
	String studentName,
	String lessonDt,
	String lessonTime,
	String reservationStatus,
	Long lessonHistoryId,
	String reviewStatus
) {
	public static RetrieveUnwrittenLessonHistory from(Schedule schedule) {
		return new RetrieveUnwrittenLessonHistory(
			schedule.getId(),
			schedule.getApplicant() != null ? schedule.getApplicant().getId() : null,
			schedule.getApplicant() != null ? schedule.getApplicant().getName() : null,
			LessonTimeFormatter.formatLessonDt(schedule.getLessonDt()),
			LessonTimeFormatter.formatLessonTimeWithAMPM(schedule.getLessonStartTime(), schedule.getLessonEndTime()),
			formatReservationStatus(schedule.getReservationStatus()),
			schedule.getLessonHistories().isEmpty() ? null : schedule.getLessonHistories().get(0).getId(),
			validateReviewStatus(schedule.getLessonHistories())
		);
	}

	private static String validateReviewStatus(List<LessonHistory> lessonHistories) {
		return lessonHistories.isEmpty() ? "미작성" : "작성";
	}

	private static String formatReservationStatus(ReservationStatus reservationStatus) {
		if (reservationStatus == ReservationStatus.COMPLETED) {
			return "출석";
		} else if (reservationStatus == ReservationStatus.NO_SHOW) {
			return "미출석";
		}
		return reservationStatus.getDescription();
	}
}
