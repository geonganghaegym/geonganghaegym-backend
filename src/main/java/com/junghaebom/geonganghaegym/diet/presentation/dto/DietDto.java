package com.junghaebom.geonganghaegym.diet.presentation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;
import com.junghaebom.geonganghaegym.diet.domain.Diet;
import com.junghaebom.geonganghaegym.member.presentation.dto.MemberDto;
import com.junghaebom.geonganghaegym.member.domain.Member;

public record DietDto(
	Long dietId,
	MemberDto member,
	Long likeCnt,
	Long commentCnt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	LocalDate eatDate,
	boolean liked,
	boolean feedbackChecked,
	DietDetailDto breakfast,
	DietDetailDto lunch,
	DietDetailDto dinner
) {

	public DietDto() {
		this(null, null, null, null, null, null, null, false, false,
			new DietDetailDto(), new DietDetailDto(), new DietDetailDto());
	}

	@QueryProjection
	public DietDto(Long dietId, Member member, boolean liked, Long likeCnt, Long commentCnt, LocalDate eatDate,
		boolean fastBreakfast, boolean fastLunch, boolean fastDinner) {
		this(
			dietId,
			MemberDto.from(member),
			likeCnt,
			commentCnt,
			null,
			null,
			eatDate,
			liked,
			false,
			new DietDetailDto(fastBreakfast),
			new DietDetailDto(fastLunch),
			new DietDetailDto(fastDinner)
		);
	}

	public static DietDto from(Diet diet) {
		return new DietDto(
			diet.getDietId(),
			MemberDto.from(diet.getMember()),
			diet.getLikeCnt(),
			diet.getCommentCnt(),
			diet.getCreatedAt(),
			diet.getUpdatedAt(),
			diet.getEatDate(),
			false,
			false,
			new DietDetailDto(diet.getFastBreakfast()),
			new DietDetailDto(diet.getFastLunch()),
			new DietDetailDto(diet.getFastDinner())
		);
	}

	public DietDto withLiked(boolean liked) {
		return new DietDto(dietId, member, likeCnt, commentCnt, createdAt, updatedAt, eatDate,
			liked, feedbackChecked, breakfast, lunch, dinner);
	}

	public DietDto withFeedbackChecked(boolean feedbackChecked) {
		return new DietDto(dietId, member, likeCnt, commentCnt, createdAt, updatedAt, eatDate,
			liked, feedbackChecked, breakfast, lunch, dinner);
	}

	public DietDto withDietFiles(List<DietFileDto> filesDto) {
		DietDetailDto newBreakfast = breakfast;
		DietDetailDto newLunch = lunch;
		DietDetailDto newDinner = dinner;
		for (DietFileDto file : filesDto) {
			switch (file.type()) {
				case BREAKFAST -> newBreakfast = newBreakfast.withDietFile(file);
				case LUNCH -> newLunch = newLunch.withDietFile(file);
				case DINNER -> newDinner = newDinner.withDietFile(file);
			}
		}
		return new DietDto(dietId, member, likeCnt, commentCnt, createdAt, updatedAt, eatDate,
			liked, feedbackChecked, newBreakfast, newLunch, newDinner);
	}

}
