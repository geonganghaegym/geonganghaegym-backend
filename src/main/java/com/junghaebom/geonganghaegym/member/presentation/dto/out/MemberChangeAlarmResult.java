package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.junghaebom.geonganghaegym.member.domain.AlarmStatus;
import com.junghaebom.geonganghaegym.member.domain.AlarmType;

public record MemberChangeAlarmResult(
	String type,
	AlarmStatus status
) {

	public static MemberChangeAlarmResult from(AlarmType type, AlarmStatus status) {
		return new MemberChangeAlarmResult(
			type.getDescription(),
			status
		);
	}
}
