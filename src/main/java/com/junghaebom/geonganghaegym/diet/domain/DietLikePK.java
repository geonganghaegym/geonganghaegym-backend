package com.junghaebom.geonganghaegym.diet.domain;

import java.io.Serializable;

import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class DietLikePK implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "diet_id", nullable = false)
	private Diet diet;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	public static DietLikePK create(Diet diet, Member member) {
		return DietLikePK.builder()
			.diet(diet)
			.member(member)
			.build();
	}

}
