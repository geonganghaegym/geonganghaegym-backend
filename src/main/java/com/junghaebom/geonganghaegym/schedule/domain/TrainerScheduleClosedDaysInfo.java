package com.junghaebom.geonganghaegym.schedule.domain;

import java.time.DayOfWeek;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerScheduleClosedDaysInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "trainer_schedule_closed_days_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_schedule_info_id")
	private TrainerScheduleInfo trainerScheduleInfo;

	@Enumerated(EnumType.STRING)
	private DayOfWeek closedDays;

	private TrainerScheduleClosedDaysInfo(TrainerScheduleInfo trainerScheduleInfo, DayOfWeek closedDays) {
		this.trainerScheduleInfo = trainerScheduleInfo;
		this.closedDays = closedDays;
	}

	public static TrainerScheduleClosedDaysInfo registerClosedDay(
		DayOfWeek dayOfWeek,
		TrainerScheduleInfo trainerScheduleInfo
	) {
		return new TrainerScheduleClosedDaysInfo(trainerScheduleInfo, dayOfWeek);
	}
}
