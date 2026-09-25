package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.time.LocalDate;
import java.time.LocalTime;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

public record CommandRegisterScheduleByStudentResult(
	Long scheduleId,
	LocalDate lessonDt,
	LocalTime lessonStartTime,
	LocalTime lessonEndTime,
	String studentName,
	Long studentId,
	Long trainerId
) {
	public static CommandRegisterScheduleByStudentResult from(Schedule schedule, Member student) {
		return new CommandRegisterScheduleByStudentResult(
			schedule.getId(),
			schedule.getLessonDt(),
			schedule.getLessonStartTime(),
			schedule.getLessonEndTime(),
			student.getName(),
			student.getId(),
			schedule.getTrainer().getId()
		);
	}
}
