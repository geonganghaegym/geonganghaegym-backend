package com.junghaebom.geonganghaegym.member.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.querydsl.core.annotations.QueryProjection;
import com.junghaebom.geonganghaegym.course.presentation.dto.CourseDto;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietDto;
import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymDto;
import com.junghaebom.geonganghaegym.point.presentation.dto.out.PointDto;
import com.junghaebom.geonganghaegym.point.presentation.dto.out.RankDto;

public record MemberDetailResult(
	Long memberId,
	String name,
	String nickName,
	String fileUrl,
	String memo,
	int ranking,
	LocalDate lessonDt,
	LocalTime lessonStartTime,
	DietDto diet,
	CourseDto course,
	PointDto point,
	RankDto rank,
	GymDto gym,
	boolean isNonmember
) {

	@QueryProjection
	public MemberDetailResult(Long memberId, String name, String nickName, String fileUrl, String memo, int ranking,
		LocalDate lessonDt, LocalTime lessonStartTime, Long nonMemberId) {
		this(memberId, name, nickName, fileUrl, memo, ranking, lessonDt, lessonStartTime, null, null, null, null, null,
			nonMemberId != null);
	}

	public MemberDetailResult withDetails(DietDto diet, CourseDto course, PointDto point, RankDto rank, GymDto gym) {
		return new MemberDetailResult(memberId, name, nickName, fileUrl, memo, ranking, lessonDt, lessonStartTime,
			diet, course, point, rank, gym, isNonmember);
	}
}
