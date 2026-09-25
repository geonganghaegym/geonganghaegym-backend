package com.junghaebom.geonganghaegym.member.domain;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComplimentaryLoginHistory {

	private Long memberId;
	private String userId;
	private MemberType memberType;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ComplimentaryLoginHistory create(Member member) {
		LocalDateTime now = LocalDateTime.now();

		return ComplimentaryLoginHistory.builder()
			.memberId(member.getId())
			.userId(member.getUserId())
			.memberType(member.getMemberType())
			.createdAt(now)
			.updatedAt(now)
			.build();
	}
}
