package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 성공 응답")
public record CommandJoinMemberResult(
	@Schema(description = "회원 ID") Long id,
	@Schema(description = "이메일") String email,
	@Schema(description = "아이디") String userId,
	@Schema(description = "이름") String name,
	@Schema(description = "회원구분") MemberType memberType
) {

	public static CommandJoinMemberResult from(Member member) {
		return new CommandJoinMemberResult(
			member.getId(),
			member.getEmail(),
			member.getUserId(),
			member.getName(),
			member.getMemberType()
		);
	}
}
