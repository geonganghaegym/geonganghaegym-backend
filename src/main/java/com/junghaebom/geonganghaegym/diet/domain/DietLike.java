package com.junghaebom.geonganghaegym.diet.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diet_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class DietLike {

	@EmbeddedId
	private DietLikePK dietLikePK;

	public static DietLike from(DietLikePK dietLikePK) {
		return DietLike.builder()
			.dietLikePK(dietLikePK)
			.build();
	}

}
