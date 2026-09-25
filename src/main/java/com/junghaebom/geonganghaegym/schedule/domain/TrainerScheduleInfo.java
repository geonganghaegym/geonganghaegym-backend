package com.junghaebom.geonganghaegym.schedule.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerScheduleInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "trainer_schedule_info_id")
	private Long id;

	private LocalTime lessonStartTime;

	private LocalTime lessonEndTime;

	private LocalTime lunchStartTime;

	private LocalTime lunchEndTime;

	@Enumerated(EnumType.STRING)
	private LessonTime lessonTime;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_id")
	private Member trainer;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "trainerScheduleInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TrainerScheduleClosedDaysInfo> trainerScheduleClosedDays = new ArrayList<>();

	private TrainerScheduleInfo(LocalTime lessonStartTime, LocalTime lessonEndTime,
		LocalTime lunchStartTime, LocalTime lunchEndTime,
		LessonTime lessonTime, Member trainer) {
		this.lessonStartTime = lessonStartTime;
		this.lessonEndTime = lessonEndTime;
		this.lunchStartTime = lunchStartTime;
		this.lunchEndTime = lunchEndTime;
		this.lessonTime = lessonTime;
		this.trainer = trainer;
	}

	public static TrainerScheduleInfo registerDefaultLessonTime(
		LocalTime lessonStartTime,
		LocalTime lessonEndTime,
		LocalTime lunchStartTime,
		LocalTime lunchEndTime,
		Member trainer
	) {
		return new TrainerScheduleInfo(
			lessonStartTime,
			lessonEndTime,
			lunchStartTime,
			lunchEndTime,
			LessonTime.ONE_HOUR,
			trainer
		);
	}

	public static LessonTime fromDescription(int description) {
		for (LessonTime lt : LessonTime.values()) {
			if (lt.getDescription() == description) {
				return lt;
			}
		}
		throw new CustomException(ErrorCode.INVALID_LESSON_TIME_DESCRIPTION);
	}

	public void changeDefaultLessonTime(
		LocalTime lessonStartTime,
		LocalTime lessonEndTime,
		LocalTime lunchStartTime,
		LocalTime lunchEndTime,
		Integer lessonTime,
		List<TrainerScheduleClosedDaysInfo> closedDays
	) {
		this.lessonStartTime = lessonStartTime;
		this.lessonEndTime = lessonEndTime;
		this.lunchStartTime = lunchStartTime;
		this.lunchEndTime = lunchEndTime;
		this.lessonTime = fromDescription(lessonTime);
		this.trainerScheduleClosedDays.clear();
		this.trainerScheduleClosedDays.addAll(closedDays);
	}
}
