package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.SocialType;

public record CommandFindMemberPasswordResult(
	@JsonIgnore String email,
	@JsonIgnore String message,
	SocialType socialType
) {

	public static CommandFindMemberPasswordResult from(Member member, String message) {
		return new CommandFindMemberPasswordResult(
			member.getEmail(),
			message,
			member.getSocialType()
		);
	}
}
