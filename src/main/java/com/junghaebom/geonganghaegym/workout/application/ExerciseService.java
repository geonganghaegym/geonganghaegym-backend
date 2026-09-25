package com.junghaebom.geonganghaegym.workout.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.workout.presentation.dto.ExerciseDto;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.CustomExerciseAddCommand;
import com.junghaebom.geonganghaegym.workout.domain.Exercise;
import com.junghaebom.geonganghaegym.workout.domain.ExerciseCategory;
import com.junghaebom.geonganghaegym.workout.repository.exercise.ExerciseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExerciseService {

	private final ExerciseRepository exerciseRepository;

	public CustomPaging<ExerciseDto> getExercise(Member member, ExerciseCategory exerciseCategory, Pageable pageable,
		String searchValue) {
		Page<Exercise> exercises = exerciseRepository.getExercise(member.getId(), exerciseCategory, pageable,
			searchValue);
		List<ExerciseDto> exerciseDtos = exercises.map(ExerciseDto::from).stream().toList();

		return new CustomPaging(exerciseDtos, exercises.getPageable().getPageNumber(),
			exercises.getPageable().getPageSize(), exercises.getTotalPages(), exercises.getTotalElements(),
			exercises.isLast());
	}

	public void addExerciseCustom(Member member, CustomExerciseAddCommand command) {
		exerciseRepository.findByMemberIdAndNames(member.getId(), command.names())
			.ifPresent(i -> {
				throw new CustomException(EXERCISE_ALREADY_EXISTS);
			});
		exerciseRepository.save(Exercise.create(member, command.names(), command.category(), command.muscles()));
	}

	public void deleteExerciseCustom(Member member, Long exerciseId) {
		Exercise exercise = exerciseRepository.findByExerciseIdAndMemberId(exerciseId, member.getId())
			.orElseThrow(() -> new CustomException(EXERCISE_NOT_FOUND));
		exerciseRepository.delete(exercise);
	}
}
