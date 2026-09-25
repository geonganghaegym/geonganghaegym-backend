package com.junghaebom.geonganghaegym.workout.domain;

import java.io.Serializable;

import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class WorkoutHistoryLikePK implements Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_history_id", nullable = false)
	private WorkoutHistory workoutHistory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	public static WorkoutHistoryLikePK create(WorkoutHistory history, Member member) {
		return WorkoutHistoryLikePK.builder()
			.workoutHistory(history)
			.member(member)
			.build();
	}

}
