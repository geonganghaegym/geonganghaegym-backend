package com.junghaebom.geonganghaegym.gym.presentation.dto.out;

import com.junghaebom.geonganghaegym.gym.domain.Gym;

public record CommandRegisterGymResult(
	Long id,
	String name,
	String joinCode
) {
	public static CommandRegisterGymResult from(Gym gym) {
		return new CommandRegisterGymResult(
			gym.getId(),
			gym.getName(),
			gym.getJoinCode()
		);
	}
}
