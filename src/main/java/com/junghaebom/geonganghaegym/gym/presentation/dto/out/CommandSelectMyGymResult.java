package com.junghaebom.geonganghaegym.gym.presentation.dto.out;

import com.junghaebom.geonganghaegym.gym.domain.Gym;

public record CommandSelectMyGymResult(
	Long id,
	String name
) {
	public static CommandSelectMyGymResult from(Gym gym) {
		return new CommandSelectMyGymResult(
			gym.getId(),
			gym.getName()
		);
	}
}
