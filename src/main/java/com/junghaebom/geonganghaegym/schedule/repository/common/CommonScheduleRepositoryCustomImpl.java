package com.junghaebom.geonganghaegym.schedule.repository.common;


import static com.junghaebom.geonganghaegym.schedule.domain.QSchedule.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommonScheduleRepositoryCustomImpl implements CommonScheduleRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Long getCompletedLessonCnt(Long memberId, Long courseId) {
		return queryFactory
			.select(schedule.count())
			.from(schedule)
			.where(memberIdEq(memberId), courseIdEq(courseId), lessonDateTimeBeforeNow())
			.fetchOne();
	}

	private BooleanExpression courseIdEq(Long courseId) {
		if (!ObjectUtils.isEmpty(courseId)) {
			return schedule.course.courseId.eq(courseId);
		}
		return null;
	}

	private BooleanExpression memberIdEq(Long memberId) {
		if (!ObjectUtils.isEmpty(memberId)) {
			return schedule.applicant.id.eq(memberId);
		}
		return null;
	}

	private Predicate lessonDateTimeBeforeNow() {
		return schedule.lessonDt.before(LocalDate.now())
			.or(schedule.lessonDt.loe(LocalDate.now()).and(schedule.lessonStartTime.before(LocalTime.now())));
	}
}
