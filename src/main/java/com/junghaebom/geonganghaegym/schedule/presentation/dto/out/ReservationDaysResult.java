package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.util.List;

import org.springframework.util.ObjectUtils;

public record ReservationDaysResult(
	List<String> reservationDays
) {
	public static ReservationDaysResult from(List<String> days) {
		return new ReservationDaysResult(ObjectUtils.isEmpty(days) ? null : days);
	}
}
