package com.junghaebom.geonganghaegym.common.enums;

import lombok.Getter;

@Getter
public class EnumMapperValue {

	private final String code;
	private final String description;

	public EnumMapperValue(EnumMapperType enumMapperType) {
		this.code = enumMapperType.getCode();
		this.description = enumMapperType.getDescription();
	}
}
