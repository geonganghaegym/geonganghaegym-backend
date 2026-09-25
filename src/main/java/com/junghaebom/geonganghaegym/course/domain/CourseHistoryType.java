package com.junghaebom.geonganghaegym.course.domain;

import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;

import java.util.List;

import com.junghaebom.geonganghaegym.common.enums.EnumGroup;
import com.junghaebom.geonganghaegym.common.enums.EnumMapperTypeExt;
import com.junghaebom.geonganghaegym.member.domain.MemberType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CourseHistoryType implements EnumMapperTypeExt {

	COURSE_CREATE("수강권 생성", TRAINER),
	PLUS_CNT("횟수 추가", TRAINER),
	MINUS_CNT("횟수 차감", TRAINER),
	ONE_LESSON("1회 수강권 지급", TRAINER),

	RESERVATION("수업 예약", STUDENT),
	RESERVATION_CANCEL("수업 예약 취소", STUDENT);

	private final String description;
	private final MemberType requester;

	public static List<CourseHistoryType> getEnumByGroup(MemberType parentCode) {
		return EnumGroup.getEnumByGroup(CourseHistoryType.class, parentCode);
	}

	public static List<MemberType> getParentEnum() {
		return EnumGroup.getParentEnum(MemberType.class);
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public MemberType getParentCode() {
		return requester;
	}
}
