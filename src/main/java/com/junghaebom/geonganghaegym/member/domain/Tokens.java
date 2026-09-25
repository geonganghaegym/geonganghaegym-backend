package com.junghaebom.geonganghaegym.member.domain;

import com.junghaebom.geonganghaegym.gym.domain.Gym;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Tokens {
	private Long memberId;
	private String name;
	private String accessToken;
	private String refreshToken;
	private String userId;
	private MemberType memberType;
	private Long gymId;

	public Tokens(Long memberId, String name, String accessToken, String refreshToken, String userId,
		MemberType memberType, Gym gym) {
		this.memberId = memberId;
		this.name = name;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.userId = userId;
		this.memberType = memberType;
		this.gymId = gym != null ? gym.getId() : null;
	}
}