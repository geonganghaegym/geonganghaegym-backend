package com.junghaebom.geonganghaegym.diet.presentation.dto.out;

import java.util.List;

import org.springframework.util.ObjectUtils;

import com.junghaebom.geonganghaegym.member.domain.AlarmStatus;

public record DietUploadDaysResult(AlarmStatus dietNoticeStatus, List<String> uploadDays) {

	public static DietUploadDaysResult create(AlarmStatus dietNoticeStatus, List<String> days) {
		return new DietUploadDaysResult(
			dietNoticeStatus,
			ObjectUtils.isEmpty(days) ? null : days
		);
	}
}
