package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymDto;
import com.junghaebom.geonganghaegym.member.presentation.dto.ProfileDto;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;

public record RetrieveTrainerInfo(
	Long mappingId,
	TrainerInfo trainer
) {

	public static RetrieveTrainerInfo from(TrainerMemberMapping trainerMemberMapping) {
		return new RetrieveTrainerInfo(
			trainerMemberMapping.getMappingId(),
			TrainerInfo.from(trainerMemberMapping.getTrainer())
		);
	}

	public record TrainerInfo(
		Long id,
		String email,
		String name,
		ProfileDto profile,
		GymDto gym
	) {

		public static TrainerInfo from(Member trainer) {
			return new TrainerInfo(
				trainer.getId(),
				trainer.getEmail(),
				trainer.getName(),
				ProfileDto.from(trainer.getMemberProfile()),
				GymDto.from(trainer.getGym())
			);
		}
	}
}
