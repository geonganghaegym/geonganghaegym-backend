package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.junghaebom.geonganghaegym.member.domain.Member;

public record CommandChangeNameResult(
	Long memberId,
	String name
) {

	public static CommandChangeNameResult from(Member member) {
		return new CommandChangeNameResult(
			member.getId(),
			member.getName()
		);
	}
}
