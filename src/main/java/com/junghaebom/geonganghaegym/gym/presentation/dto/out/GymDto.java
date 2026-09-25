package com.junghaebom.geonganghaegym.gym.presentation.dto.out;

import com.junghaebom.geonganghaegym.gym.domain.Gym;

public record GymDto(
	Long id,
	String name
) {
	public static GymDto from(Gym gym) {
		return new GymDto(
			gym.getId(),
			gym.getName()
		);
	}
}
