package com.junghaebom.geonganghaegym.diet.presentation.dto;

public record DietDetailDto(Boolean fast, DietFileDto dietFile) {

	public DietDetailDto() {
		this(false, null);
	}

	public DietDetailDto(Boolean fast) {
		this(fast, null);
	}

	public DietDetailDto withDietFile(DietFileDto dietFile) {
		return new DietDetailDto(this.fast, dietFile);
	}

}
