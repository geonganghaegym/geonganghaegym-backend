package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistory;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryFiles;

public record CommandRegisterLessonHistoryResult(
	Long lessonHistoryId,
	String title,
	String content,
	Long scheduleId,
	LocalDate lessonDt,
	LocalTime lessonStartTime,
	LocalTime lessonEndTime,
	Long trainerId,
	String trainerName,
	Long studentId,
	String studentName,
	List<CommandUploadFileResult> files
) {
	public static CommandRegisterLessonHistoryResult from(LessonHistory lessonHistory, List<LessonHistoryFiles> files) {
		return new CommandRegisterLessonHistoryResult(
			lessonHistory.getId(),
			lessonHistory.getTitle(),
			lessonHistory.getContent(),
			lessonHistory.getSchedule() != null ? lessonHistory.getSchedule().getId() : null,
			lessonHistory.getSchedule() != null ? lessonHistory.getSchedule().getLessonDt() : null,
			lessonHistory.getSchedule() != null ? lessonHistory.getSchedule().getLessonStartTime() : null,
			lessonHistory.getSchedule() != null ? lessonHistory.getSchedule().getLessonEndTime() : null,
			lessonHistory.getTrainer() != null ? lessonHistory.getTrainer().getId() : null,
			lessonHistory.getTrainer() != null ? lessonHistory.getTrainer().getName() : null,
			lessonHistory.getStudent() != null ? lessonHistory.getStudent().getId() : null,
			lessonHistory.getStudent() != null ? lessonHistory.getStudent().getName() : null,
			files.stream().map(CommandUploadFileResult::from).collect(Collectors.toList())
		);
	}
}
