package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.junghaebom.geonganghaegym.member.domain.Member;

public record CommandAssignNicknameResult(
	Long memberId,
	String nickname
) {

	public static CommandAssignNicknameResult from(Member member) {
		return new CommandAssignNicknameResult(
			member.getId(),
			member.getNickname()
		);
	}
}
