package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietDto;
import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymDto;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.RetrieveLessonHistoryByDateCondResult;
import com.junghaebom.geonganghaegym.point.presentation.dto.out.PointDto;
import com.junghaebom.geonganghaegym.point.presentation.dto.out.RankDto;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.MyReservation;

public record StudentHomeResult(
	CourseDto course,
	PointDto point,
	RankDto rank,
	MyReservation myReservation,
	RetrieveLessonHistoryByDateCondResult lessonHistory,
	DietDto diet,
	GymDto gym,
	Boolean redDotStatus
) {

	public static StudentHomeResult create(CourseDto course, PointDto point, RankDto rank, MyReservation myReservation,
		RetrieveLessonHistoryByDateCondResult lessonHistory, DietDto diet, GymDto gym, Boolean redDotStatus) {
		return new StudentHomeResult(course, point, rank, myReservation, lessonHistory, diet, gym, redDotStatus);
	}
}
