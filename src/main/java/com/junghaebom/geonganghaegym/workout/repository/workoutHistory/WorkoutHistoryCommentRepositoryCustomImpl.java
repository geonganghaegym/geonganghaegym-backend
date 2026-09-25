package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;


import static com.junghaebom.geonganghaegym.member.domain.QMember.*;
import static com.junghaebom.geonganghaegym.workout.domain.QWorkoutHistoryComment.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.member.domain.QMemberProfile;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryComment;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WorkoutHistoryCommentRepositoryCustomImpl implements WorkoutHistoryCommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<WorkoutHistoryComment> getCommentsByWorkoutHistoryId(Long workoutHistoryId, Pageable pageable) {
		QMemberProfile profileId = new QMemberProfile("profileId");
		Long totalCnt = queryFactory
			.select(workoutHistoryComment.count())
			.from(workoutHistoryComment)
			.leftJoin(workoutHistoryComment.member, member)
			.leftJoin(member.memberProfile, profileId)
			.where(historyIdEq(workoutHistoryId))
			.fetchOne();
		List<WorkoutHistoryComment> comments = queryFactory
			.select(workoutHistoryComment)
			.from(workoutHistoryComment)
			.leftJoin(workoutHistoryComment.member, member).fetchJoin()
			.leftJoin(member.memberProfile, profileId).fetchJoin()
			.where(historyIdEq(workoutHistoryId))
			.orderBy(workoutHistoryComment.orderNum.asc(), workoutHistoryComment.createdAt.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
		return PageableExecutionUtils.getPage(comments, pageable, () -> totalCnt);
	}

	private BooleanExpression historyIdEq(Long workoutHistoryId) {
		if (!ObjectUtils.isEmpty(workoutHistoryId)) {
			return workoutHistoryComment.workoutHistory.workoutHistoryId.eq(workoutHistoryId);
		}
		return null;
	}
}
