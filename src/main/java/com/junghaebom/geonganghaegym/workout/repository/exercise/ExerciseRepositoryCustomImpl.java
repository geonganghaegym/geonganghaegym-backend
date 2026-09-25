package com.junghaebom.geonganghaegym.workout.repository.exercise;

import static com.junghaebom.geonganghaegym.workout.domain.QExercise.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.workout.domain.Exercise;
import com.junghaebom.geonganghaegym.workout.domain.ExerciseCategory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExerciseRepositoryCustomImpl implements ExerciseRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Exercise> getExercise(Long memberId, ExerciseCategory exerciseCategory, Pageable pageable,
		String searchValue) {
		Long totalCnt = queryFactory
			.select(exercise.count())
			.from(exercise)
			.where(exerciseCategoryEq(exerciseCategory), nameLike(searchValue))
			.fetchOne();
		List<Exercise> exercises = queryFactory
			.select(exercise)
			.from(exercise)
			.where(exerciseCategoryEq(exerciseCategory)
				, exercise.member.id.eq(memberId).or(exercise.member.id.isNull())
				, nameLike(searchValue))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(exercise.member.id.desc(), exercise.exerciseId.asc())
			.fetch();
		return PageableExecutionUtils.getPage(exercises, pageable, () -> totalCnt);
	}

	private BooleanExpression nameLike(String name) {
		if (!ObjectUtils.isEmpty(name)) {
			return exercise.names.containsIgnoreCase(name);
		}
		return null;
	}

	private BooleanExpression exerciseCategoryEq(ExerciseCategory category) {
		if (!ObjectUtils.isEmpty(category)) {
			return exercise.category.eq(category);
		}
		return null;
	}

}
