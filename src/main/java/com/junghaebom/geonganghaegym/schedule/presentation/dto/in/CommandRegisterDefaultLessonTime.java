package com.junghaebom.geonganghaegym.schedule.presentation.dto.in;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CommandRegisterDefaultLessonTime(
	@Schema(description = "시작 수업 시간", example = "10:00:00", type = "string")
	LocalTime lessonStartTime,

	@Schema(description = "종료 수업 시간", example = "22:00:00", type = "string")
	LocalTime lessonEndTime,

	@Schema(description = "시작 점심시간", example = "12:00:00", type = "string")
	LocalTime lunchStartTime,

	@Schema(description = "종료 점심시간", example = "13:00:00", type = "string")
	LocalTime lunchEndTime,

	List<DayOfWeek> closedDays,

	@Schema(description = "세션당 수업 시간", example = "30|60|90|120")
	@NotNull(message = "수업 시간을 입력해 주세요.")
	Integer lessonTime
) {
	public CommandRegisterDefaultLessonTime {
		lessonStartTime = lessonStartTime != null ? lessonStartTime : LocalTime.of(9, 0, 0);
		lessonEndTime = lessonEndTime != null ? lessonEndTime : LocalTime.of(18, 0, 0);
		closedDays = closedDays != null ? closedDays : new ArrayList<>();
		if (lessonStartTime != null && lessonEndTime != null) {
			validate(lessonStartTime, lessonEndTime, lunchStartTime, lunchEndTime);
		}
	}

	public CommandRegisterDefaultLessonTime() {
		this(LocalTime.of(9, 0, 0), LocalTime.of(18, 0, 0), null, null, new ArrayList<>(), null);
	}

	public void validate() {
		if (lessonStartTime.isAfter(lessonEndTime) || lessonStartTime.equals(lessonEndTime)) {
			throw new CustomException(ErrorCode.START_TIME_AFTER_END_TIME);
		}
		if (lunchStartTime != null && lunchEndTime != null && lunchStartTime.isAfter(lunchEndTime)) {
			throw new CustomException(ErrorCode.LUNCH_TIME_INVALID);
		}
		if (!isWithinRange(lessonStartTime, lessonEndTime)) {
			throw new IllegalArgumentException("근무 시간은 오전 6시부터 밤 12시까지 설정이 가능해요.");
		}
	}

	private static void validate(LocalTime lessonStartTime, LocalTime lessonEndTime, LocalTime lunchStartTime, LocalTime lunchEndTime) {
		if (lessonStartTime.isAfter(lessonEndTime) || lessonStartTime.equals(lessonEndTime)) {
			throw new CustomException(ErrorCode.START_TIME_AFTER_END_TIME);
		}
		if (lunchStartTime != null && lunchEndTime != null && lunchStartTime.isAfter(lunchEndTime)) {
			throw new CustomException(ErrorCode.LUNCH_TIME_INVALID);
		}
		if (!isWithinRange(lessonStartTime, lessonEndTime)) {
			throw new IllegalArgumentException("근무 시간은 오전 6시부터 밤 12시까지 설정이 가능해요.");
		}
	}

	private static boolean isWithinRange(LocalTime lessonStartTime, LocalTime lessonEndTime) {
		LocalTime maxLessonStartTime = LocalTime.of(6, 0);
		LocalTime maxLessonEndTime = LocalTime.MIDNIGHT;

		return isWithinRange(lessonStartTime, maxLessonStartTime, maxLessonEndTime)
			&& isWithinRange(lessonEndTime, maxLessonStartTime, maxLessonEndTime);
	}

	private static boolean isWithinRange(LocalTime time, LocalTime start, LocalTime end) {
		if (start.isBefore(end)) {
			return !time.isBefore(start) && !time.isAfter(end);
		} else {
			return !time.isBefore(start) || !time.isAfter(end);
		}
	}
}
