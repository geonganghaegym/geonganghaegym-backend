package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistory;

public interface WorkoutHistoryRepository extends JpaRepository<WorkoutHistory, Long>, WorkoutHistoryRepositoryCustom {

	Optional<WorkoutHistory> findByWorkoutHistoryIdAndMemberIdAndDelYnFalse(Long workoutHistoryId, Long memberId);

	@EntityGraph(attributePaths = {"member"})
	Optional<WorkoutHistory> findByWorkoutHistoryIdAndDelYnFalse(Long workoutHistoryId);

}
