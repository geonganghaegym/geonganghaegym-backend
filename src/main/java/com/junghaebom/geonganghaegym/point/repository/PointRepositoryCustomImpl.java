package com.junghaebom.geonganghaegym.point.repository;

import static com.junghaebom.geonganghaegym.point.domain.QPoint.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.point.domain.Point;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointRepositoryCustomImpl implements PointRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Point> getPoint(Long memberId, String searchDate, Pageable pageable) {
		Long totalCnt = queryFactory
			.select(point1.count())
			.from(point1)
			.where(pointMemberIdEq(memberId), convertDateFormat(searchDate))
			.fetchOne();
		List<Point> points = queryFactory
			.select(point1)
			.from(point1)
			.where(pointMemberIdEq(memberId), convertDateFormat(searchDate))
			.orderBy(point1.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
		return PageableExecutionUtils.getPage(points, pageable, () -> totalCnt);
	}

	private BooleanExpression pointMemberIdEq(Long memberId) {
		if (!ObjectUtils.isEmpty(memberId)) {
			return point1.member.id.eq(memberId);
		}
		return null;
	}

	private BooleanExpression convertDateFormat(String searchDate) {
		if (!ObjectUtils.isEmpty(searchDate)) {
			StringTemplate stringTemplate = Expressions.stringTemplate(
				"DATE_FORMAT({0}, {1})"
				, point1.createdAt
				, ConstantImpl.create("%Y-%m"));
			return stringTemplate.eq(searchDate);
		}
		return null;
	}

}
