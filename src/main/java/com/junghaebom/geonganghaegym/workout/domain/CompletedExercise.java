package com.junghaebom.geonganghaegym.workout.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "completed_exercise")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@ToString
public class CompletedExercise {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "completed_id")
	private Long completedId;

	private Long exerciseId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_history_id")
	@ToString.Exclude
	private WorkoutHistory workoutHistory;

	private String name;
	private int setNum;
	private int weight;
	private int numberOfCycles;

	public static CompletedExercise create(Long exerciseId, WorkoutHistory history, String name, int setNum, int weight,
		int numberOfCycles) {
		return CompletedExercise.builder()
			.exerciseId(exerciseId)
			.name(name)
			.setNum(setNum)
			.weight(weight)
			.numberOfCycles(numberOfCycles)
			.workoutHistory(history)
			.build();
	}

}
