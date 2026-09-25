package com.junghaebom.geonganghaegym.gym.presentation.dto.out;

import com.junghaebom.geonganghaegym.gym.domain.Gym;

public record GymResult(
	Long gymId,
	String name
) {
	public static GymResult from(Gym gym) {
		return new GymResult(
			gym.getId(),
			gym.getName()
		);
	}
}
