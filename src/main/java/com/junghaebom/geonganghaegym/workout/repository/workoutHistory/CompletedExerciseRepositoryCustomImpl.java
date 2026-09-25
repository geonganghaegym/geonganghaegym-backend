package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import static com.junghaebom.geonganghaegym.workout.domain.QCompletedExercise.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.workout.domain.CompletedExercise;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CompletedExerciseRepositoryCustomImpl implements CompletedExerciseRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<CompletedExercise> getCompletedExercise(List<Long> ids) {
		return queryFactory.select(completedExercise)
			.from(completedExercise)
			.where(completedExercise.workoutHistory.workoutHistoryId.in(ids))
			.orderBy(completedExercise.exerciseId.asc())
			.fetch();
	}

}
