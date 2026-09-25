package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.junghaebom.geonganghaegym.common.LessonTimeFormatter;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonAttendanceStatus;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistory;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryFiles;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryReadStatus;

public record RetrieveLessonHistoryByDateCondResult(
	Long id,
	String title,
	String content,
	Integer commentTotalCount,
	LocalDateTime createdAt,
	Long studentId,
	String student,
	String trainer,
	String trainerProfile,
	Long scheduleId,
	String lessonDt,
	String lessonTime,
	String attendanceStatus,
	LessonHistoryReadStatus feedbackChecked,
	List<LessonHistoryFileResults> files
) {
	public static RetrieveLessonHistoryByDateCondResult top1From(LessonHistory entity) {
		if (entity == null) {
			return null;
		}
		return new RetrieveLessonHistoryByDateCondResult(
			entity.getId(),
			entity.getTitle(),
			entity.getContent(),
			(int) entity.getLessonHistoryComment().stream().filter(c -> !c.isDelYn()).count(),
			entity.getCreatedAt(),
			entity.getStudent() != null ? entity.getStudent().getId() : null,
			entity.getStudent() != null ? entity.getStudent().getName() : null,
			entity.getTrainer() != null ? entity.getTrainer().getName() + " 트레이너" : null,
			entity.getTrainer() != null && entity.getTrainer().getMemberProfile() != null
				? entity.getTrainer().getMemberProfile().getFileUrl() : null,
			entity.getSchedule() != null ? entity.getSchedule().getId() : null,
			LessonTimeFormatter.formatLessonDt(
				entity.getSchedule() != null ? entity.getSchedule().getLessonDt() : null),
			LessonTimeFormatter.formatLessonTime(
				entity.getSchedule() != null ? entity.getSchedule().getLessonStartTime() : null,
				entity.getSchedule() != null ? entity.getSchedule().getLessonEndTime() : null),
			validateAttendanceStatus(
				entity.getSchedule() != null ? entity.getSchedule().getLessonDt() : null,
				entity.getSchedule() != null ? entity.getSchedule().getLessonEndTime() : null),
			entity.getFeedbackChecked(),
			entity.getFiles().stream()
				.map(LessonHistoryFileResults::from)
				.sorted(Comparator.comparing(LessonHistoryFileResults::createdAt))
				.collect(Collectors.toList())
		);
	}

	public static RetrieveLessonHistoryByDateCondResult from(LessonHistory entity) {
		return new RetrieveLessonHistoryByDateCondResult(
			entity.getId(),
			entity.getTitle(),
			entity.getContent(),
			(int) entity.getLessonHistoryComment().stream().filter(c -> !c.isDelYn()).count(),
			entity.getCreatedAt(),
			entity.getStudent() != null ? entity.getStudent().getId() : null,
			entity.getStudent() != null ? entity.getStudent().getName() : null,
			entity.getTrainer() != null ? entity.getTrainer().getName() + " 트레이너" : null,
			entity.getTrainer() != null && entity.getTrainer().getMemberProfile() != null
				? entity.getTrainer().getMemberProfile().getFileUrl() : null,
			entity.getSchedule() != null ? entity.getSchedule().getId() : null,
			LessonTimeFormatter.formatLessonDt(
				entity.getSchedule() != null ? entity.getSchedule().getLessonDt() : null),
			LessonTimeFormatter.formatLessonTime(
				entity.getSchedule() != null ? entity.getSchedule().getLessonStartTime() : null,
				entity.getSchedule() != null ? entity.getSchedule().getLessonEndTime() : null),
			validateAttendanceStatus(
				entity.getSchedule() != null ? entity.getSchedule().getLessonDt() : null,
				entity.getSchedule() != null ? entity.getSchedule().getLessonEndTime() : null),
			entity.getFeedbackChecked(),
			entity.getFiles().stream()
				.filter(f -> f.getLessonHistoryComment() == null)
				.map(LessonHistoryFileResults::from)
				.sorted(Comparator.comparing(LessonHistoryFileResults::createdAt))
				.collect(Collectors.toList())
		);
	}

	private static String validateAttendanceStatus(LocalDate lessonDt, LocalTime lessonEndTime) {
		LocalDateTime lesson = LocalDateTime.of(lessonDt, lessonEndTime);
		if (LocalDateTime.now().isAfter(lesson)) {
			return LessonAttendanceStatus.ATTENDED.getDescription();
		}
		return LessonAttendanceStatus.ABSENT.getDescription();
	}

	public record LessonHistoryFileResults(
		String fileUrl,
		int fileOrder,
		LocalDateTime createdAt
	) {
		public static LessonHistoryFileResults from(LessonHistoryFiles entity) {
			return new LessonHistoryFileResults(
				entity.getFileUrl(),
				entity.getFileOrder(),
				entity.getCreatedAt()
			);
		}
	}
}
