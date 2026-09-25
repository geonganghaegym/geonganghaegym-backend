package com.junghaebom.geonganghaegym.course.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseStatus {

	NONE("수강권 없음"),
	USING("사용중"), //잔여 횟수 1이상인 경우
	EXPIRED("만료"); //잔여 횟수 0인 경우

	private final String description;

}
