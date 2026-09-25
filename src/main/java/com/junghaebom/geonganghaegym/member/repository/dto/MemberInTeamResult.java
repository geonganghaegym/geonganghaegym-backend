package com.junghaebom.geonganghaegym.member.repository.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MemberInTeamResult(
	Long memberId,
	String name,
	String userId,
	String email,
	int ranking,
	int lessonCnt,
	int remainLessonCnt,
	String nickName,
	String fileUrl,
	Long courseId,
	boolean isNonmember
) {

	@QueryProjection
	public MemberInTeamResult(Long memberId, String name, String userId, String email, int ranking, int lessonCnt,
		int remainLessonCnt, String nickName, String fileUrl, Long nonMemberId) {
		this(memberId, name, userId, email, ranking, lessonCnt, remainLessonCnt, nickName, fileUrl, null,
			nonMemberId != null);
	}

	public MemberInTeamResult withCourseId(Long courseId) {
		return new MemberInTeamResult(memberId, name, userId, email, ranking, lessonCnt, remainLessonCnt, nickName,
			fileUrl, courseId, isNonmember);
	}
}
