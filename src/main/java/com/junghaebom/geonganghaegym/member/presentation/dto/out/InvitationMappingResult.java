package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.junghaebom.geonganghaegym.member.presentation.dto.MemberDto;
import com.junghaebom.geonganghaegym.member.domain.Member;

public record InvitationMappingResult(
	MemberDto trainer,
	String name,
	int lessonCnt
) {

	public static InvitationMappingResult create(Member member, String name, int lessonCnt) {
		return new InvitationMappingResult(
			MemberDto.from(member),
			name,
			lessonCnt
		);
	}
}
