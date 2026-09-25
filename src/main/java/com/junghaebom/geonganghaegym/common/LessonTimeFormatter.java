package com.junghaebom.geonganghaegym.common;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LessonTimeFormatter {

	private LessonTimeFormatter() {
	}

	public static String formatLessonTime(LocalTime lessonStartTime, LocalTime lessonEndTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		String startTime = lessonStartTime != null ? lessonStartTime.format(formatter) : null;
		String endTime = lessonEndTime != null ? lessonEndTime.format(formatter) : null;
		return startTime + " - " + endTime;
	}

	public static String formatLessonTimeWithAMPM(LocalTime lessonStartTime, LocalTime lessonEndTime) {
		DateTimeFormatter startTimeFormatter = DateTimeFormatter.ofPattern("a hh:mm");
		DateTimeFormatter endTimeFormatter = DateTimeFormatter.ofPattern("hh:mm");
		String startTime = lessonStartTime.format(startTimeFormatter);
		String endTime = lessonEndTime.format(endTimeFormatter);
		return startTime + " - " + endTime;
	}

	public static String formatLessonTime(LocalTime localTime) {
		if (localTime == null) {
			return null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		return localTime.format(formatter);
	}

	public static String formatLessonDt(LocalDate lessonDt) {
		if (lessonDt == null) {
			return null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 E요일", Locale.KOREAN);
		return lessonDt.format(formatter);
	}

	public static DateTimeFormatter lessonStartDateTimeFormatter() {
		return DateTimeFormatter.ofPattern("M월 d일(E) h시");
	}
}
