package com.junghaebom.geonganghaegym.trainer.presentation.dto;

import com.junghaebom.geonganghaegym.member.presentation.dto.MemberDto;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;

public record TrainerMemberMappingDto(
	Long mappingId,
	MemberDto trainer,
	MemberDto member
) {

	public static TrainerMemberMappingDto from(TrainerMemberMapping mapping) {
		return new TrainerMemberMappingDto(
			mapping.getMappingId(),
			MemberDto.from(mapping.getTrainer()),
			MemberDto.from(mapping.getMember())
		);
	}
}
