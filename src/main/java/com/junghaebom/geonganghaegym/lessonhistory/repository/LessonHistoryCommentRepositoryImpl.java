package com.junghaebom.geonganghaegym.lessonhistory.repository;

import static com.junghaebom.geonganghaegym.lessonhistory.domain.QLessonHistoryComment.*;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryComment;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LessonHistoryCommentRepositoryImpl implements LessonHistoryCommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public int findTopComment(Long lessonHistoryId, Long lessonHistoryCommentId) {
		Integer result = queryFactory
			.select(lessonHistoryComment.order.max().add(1))
			.from(lessonHistoryComment)
			.where(
				lessonHistoryIdEq(lessonHistoryId),
				parentCommentIdEq(lessonHistoryCommentId)
			)
			.fetchOne();
		return result != null ? result : 1;
	}

	@Override
	public LessonHistoryComment findLessonHistoryCommentWithFiles(Long lessonHistoryCommentId, Long writerId) {
		return queryFactory
			.select(lessonHistoryComment)
			.from(lessonHistoryComment)
			.leftJoin(lessonHistoryComment.files).fetchJoin()
			.where(
				lessonHistoryComment.id.eq(lessonHistoryCommentId),
				lessonHistoryComment.delYn.eq(false),
				lessonHistoryComment.writer.id.eq(writerId)
			)
			.fetchOne();
	}

	@Override
	public LessonHistoryComment findCommentById(Long lessonHistoryCommentId) {
		return queryFactory
			.select(lessonHistoryComment)
			.from(lessonHistoryComment)
			.where(
				lessonHistoryComment.id.eq(lessonHistoryCommentId),
				lessonHistoryComment.delYn.eq(false)
			)
			.fetchOne();
	}

	@Override
	public LessonHistoryComment findById(Long lessonHistoryCommentId, Long writerId) {
		return queryFactory
			.select(lessonHistoryComment)
			.from(lessonHistoryComment)
			.where(
				lessonHistoryComment.id.eq(lessonHistoryCommentId),
				lessonHistoryComment.delYn.eq(false),
				lessonHistoryComment.writer.id.eq(writerId)
			)
			.fetchOne();
	}

	private BooleanExpression parentCommentIdEq(Long lessonHistoryCommentParentId) {
		if (ObjectUtils.isEmpty(lessonHistoryCommentParentId)) {
			return null;
		}
		return lessonHistoryComment.parent.id.eq(lessonHistoryCommentParentId);
	}

	private BooleanExpression parentCommentIdIsNull() {
		return lessonHistoryComment.parent.isNull();
	}

	private BooleanExpression lessonHistoryIdEq(Long lessonHistoryId) {
		return lessonHistoryComment.lessonHistory.id.eq(lessonHistoryId);
	}
}
