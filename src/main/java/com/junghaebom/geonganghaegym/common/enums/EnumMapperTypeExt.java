package com.junghaebom.geonganghaegym.common.enums;

public interface EnumMapperTypeExt extends EnumMapperType {
	<T extends Enum<T> & EnumMapperType> T getParentCode();
}
