package com.junghaebom.geonganghaegym.diet.presentation.dto;

import com.junghaebom.geonganghaegym.diet.domain.DietFiles;
import com.junghaebom.geonganghaegym.diet.domain.DietType;

public record DietFileDto(Long id, Long dietId, String fileUrl, DietType type) {

	public static DietFileDto from(DietFiles dietFile) {
		return new DietFileDto(
			dietFile.getId(),
			dietFile.getDiet().getDietId(),
			dietFile.getFileUrl(),
			dietFile.getType()
		);
	}
}
