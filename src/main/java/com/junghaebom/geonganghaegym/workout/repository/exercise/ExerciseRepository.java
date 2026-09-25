package com.junghaebom.geonganghaegym.workout.repository.exercise;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.workout.domain.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Long>, ExerciseRepositoryCustom {
	Optional<Exercise> findByExerciseIdAndMemberId(Long exerciseId, Long memberId);

	Optional<Exercise> findByMemberIdAndNames(Long memberId, String names);

}
