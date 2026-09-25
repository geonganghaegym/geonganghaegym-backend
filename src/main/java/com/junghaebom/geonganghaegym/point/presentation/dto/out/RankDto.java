package com.junghaebom.geonganghaegym.point.presentation.dto.out;

public record RankDto(
	int ranking,
	int lastMonthRanking,
	int totalMemberCnt
) {
	public static RankDto create(int ranking, int lastMonthRanking, int totalMemberCnt) {
		return new RankDto(ranking, lastMonthRanking, totalMemberCnt);
	}
}
