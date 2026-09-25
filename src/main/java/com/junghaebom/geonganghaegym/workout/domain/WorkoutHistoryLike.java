package com.junghaebom.geonganghaegym.workout.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workout_history_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class WorkoutHistoryLike {

	@EmbeddedId
	private WorkoutHistoryLikePK workoutHistoryLikePK;

	public static WorkoutHistoryLike from(WorkoutHistoryLikePK workoutHistoryLikePK) {
		return WorkoutHistoryLike.builder()
			.workoutHistoryLikePK(workoutHistoryLikePK)
			.build();
	}

}
