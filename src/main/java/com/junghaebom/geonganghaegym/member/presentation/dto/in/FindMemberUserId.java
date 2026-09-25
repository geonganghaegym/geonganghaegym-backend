package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.SocialType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Schema(description = "아이디 찾기 DTO")
public record FindMemberUserId(
	@Schema(description = "이메일", example = "to-be-healthy@gmail.com")
	@NotEmpty(message = "이메일을 입력해 주세요.")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "올바른 이메일 형식을 입력해 주세요.")
	String email,

	@Schema(description = "실명", example = "홍길동")
	@NotEmpty(message = "실명을 입력해 주세요.")
	String name
) {

	public record FindMemberUserIdResult(
		String userId,
		LocalDateTime createdAt,
		SocialType socialType,
		@JsonIgnore String message
	) {
		public static FindMemberUserIdResult from(Member member, String message) {
			return new FindMemberUserIdResult(
				member.getUserId().substring(0, member.getUserId().length() - 2) + "**",
				member.getCreatedAt(),
				member.getSocialType(),
				message
			);
		}
	}
}
