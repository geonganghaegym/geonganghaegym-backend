package com.junghaebom.geonganghaegym.common.enums;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

//부모 코드로 자식 EnumSet을 추출하는 EnumGroup 클래스
public class EnumGroup {
	public static <T extends Enum<T> & EnumMapperTypeExt, E extends Enum<E> & EnumMapperType> List<T> getEnumByGroup(
		Class<T> enumClass, E parentCode) {
		return EnumSet.allOf(enumClass)
			.stream()
			.filter(type -> type.getParentCode().equals(parentCode))
			.collect(Collectors.toList());
	}

	public static <E extends Enum<E> & EnumMapperType> List<E> getParentEnum(Class<E> enumClass) {
		return new ArrayList<>(EnumSet.allOf(enumClass));
	}
}
