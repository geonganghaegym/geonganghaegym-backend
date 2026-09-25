package com.junghaebom.geonganghaegym.schedule.repository.waiting;

import static com.junghaebom.geonganghaegym.schedule.domain.QSchedule.*;
import static com.junghaebom.geonganghaegym.schedule.domain.QScheduleWaiting.*;
import static java.util.stream.Collectors.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.member.domain.QMember;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.MyScheduleWaiting;
import com.junghaebom.geonganghaegym.schedule.domain.ScheduleWaiting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ScheduleWaitingRepositoryImpl implements ScheduleWaitingRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<MyScheduleWaiting> findAllMyScheduleWaiting(Long memberId) {
		List<ScheduleWaiting> results = queryFactory.select(scheduleWaiting)
			.from(scheduleWaiting)
			.innerJoin(scheduleWaiting.schedule, schedule).fetchJoin()
			.innerJoin(scheduleWaiting.member, new QMember("member")).fetchJoin()
			.innerJoin(schedule.trainer, new QMember("trainer")).fetchJoin()
			.where(scheduleWaitingMemberIdEq(memberId),
				lessonDateTimeAfterYesterday())
			.orderBy(scheduleWaiting.schedule.lessonDt.asc(), scheduleWaiting.schedule.lessonStartTime.asc())
			.fetch();
		return results.stream().map(MyScheduleWaiting::from).collect(toList());
	}

	private BooleanExpression scheduleWaitingMemberIdEq(Long memberId) {
		if (!ObjectUtils.isEmpty(memberId)) {
			return scheduleWaiting.member.id.eq(memberId);
		}
		return null;
	}

	private Predicate lessonDateTimeAfterYesterday() {
		return schedule.lessonDt.after(LocalDate.now().plusDays(1))
			.or(schedule.lessonDt.goe(LocalDate.now().plusDays(1))
				.and(schedule.lessonStartTime.after(LocalTime.now())));
	}
}
