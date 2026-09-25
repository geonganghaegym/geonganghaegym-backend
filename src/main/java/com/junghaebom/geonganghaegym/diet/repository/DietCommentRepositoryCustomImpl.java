package com.junghaebom.geonganghaegym.diet.repository;

import static com.junghaebom.geonganghaegym.diet.domain.QDietComment.*;
import static com.junghaebom.geonganghaegym.member.domain.QMember.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.diet.domain.DietComment;
import com.junghaebom.geonganghaegym.member.domain.QMemberProfile;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DietCommentRepositoryCustomImpl implements DietCommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<DietComment> getCommentsByDietId(Long dietId, Pageable pageable) {
		QMemberProfile profileId = new QMemberProfile("profileId");
		Long totalCnt = queryFactory
			.select(dietComment.count())
			.from(dietComment)
			.leftJoin(dietComment.member, member)
			.leftJoin(member.memberProfile, profileId)
			.where(dietIdEq(dietId))
			.fetchOne();
		List<DietComment> comments = queryFactory
			.select(dietComment)
			.from(dietComment)
			.leftJoin(dietComment.member, member).fetchJoin()
			.leftJoin(member.memberProfile, profileId).fetchJoin()
			.where(dietIdEq(dietId))
			.orderBy(dietComment.orderNum.asc(), dietComment.createdAt.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
		return PageableExecutionUtils.getPage(comments, pageable, () -> totalCnt);
	}

	private BooleanExpression dietIdEq(Long dietId) {
		if (!ObjectUtils.isEmpty(dietId)) {
			return dietComment.diet.dietId.eq(dietId);
		}
		return null;
	}
}
