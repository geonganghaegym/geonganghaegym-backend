package com.junghaebom.geonganghaegym.lessonhistory.repository;

import static com.junghaebom.geonganghaegym.lessonhistory.domain.QLessonHistory.*;
import static com.junghaebom.geonganghaegym.lessonhistory.domain.QLessonHistoryFiles.*;
import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;
import static com.junghaebom.geonganghaegym.schedule.domain.QSchedule.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.RetrieveLessonHistoryByDateCond;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.RetrieveLessonHistoryByDateCondResult;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistory;
import com.junghaebom.geonganghaegym.member.domain.MemberType;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LessonHistoryRepositoryImpl implements LessonHistoryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public LessonHistory findById(Long lessonHistoryId, Long memberId, MemberType memberType) {
		return queryFactory
			.select(lessonHistory)
			.from(lessonHistory)
			.where(
				lessonHistory.id.eq(lessonHistoryId),
				validateMemberTypeAndMemberIdEq(memberId, memberType)
			)
			.fetchOne();
	}

	@Override
	public List<LessonHistory> findAllLessonHistory(RetrieveLessonHistoryByDateCond request, Long memberId,
		MemberType memberType) {
		return queryFactory
			.select(lessonHistory)
			.from(lessonHistory)
			.innerJoin(lessonHistory.trainer).fetchJoin()
			.innerJoin(lessonHistory.student).fetchJoin()
			.innerJoin(lessonHistory.schedule, schedule).fetchJoin()
			.where(
				convertDateFormat(request.searchDate()),
				validateMemberTypeAndMemberIdEq(memberId, memberType)
			)
			.orderBy(
				schedule.lessonDt.desc(),
				schedule.lessonStartTime.desc(),
				lessonHistory.id.desc()
			)
			.limit(50)
			.fetch();
	}

	@Override
	public LessonHistory findOneLessonHistory(Long lessonHistoryId, Long memberId, MemberType memberType) {
		return queryFactory
			.selectDistinct(lessonHistory)
			.from(lessonHistory)
			.leftJoin(lessonHistory.lessonHistoryComment).fetchJoin()
			.innerJoin(lessonHistory.trainer).fetchJoin()
			.innerJoin(lessonHistory.student).fetchJoin()
			.innerJoin(lessonHistory.schedule).fetchJoin()
			.where(
				validateMemberTypeAndMemberIdEq(memberId, memberType),
				lessonHistoryIdEq(lessonHistoryId)
			)
			.fetchOne();
	}

	private BooleanExpression lessonHistoryIdEq(Long lessonHistoryId) {
		return lessonHistory.id.eq(lessonHistoryId);
	}

	@Override
	public List<LessonHistory> findAllLessonHistoryByMemberId(Long studentId, RetrieveLessonHistoryByDateCond request,
		Long trainerId) {
		return queryFactory
			.select(lessonHistory)
			.from(lessonHistory)
			.leftJoin(lessonHistory.trainer).fetchJoin()
			.leftJoin(lessonHistory.student).fetchJoin()
			.leftJoin(lessonHistory.schedule).fetchJoin()
			.where(
				convertDateFormat(request.searchDate()),
				lessonHistory.student.id.eq(studentId),
				lessonHistory.trainer.id.eq(trainerId)
			)
			.orderBy(
				schedule.lessonDt.desc(),
				schedule.lessonStartTime.desc(),
				lessonHistory.id.desc()
			)
			.limit(50)
			.fetch();
	}

	@Override
	public RetrieveLessonHistoryByDateCondResult findTop1LessonHistoryByMemberId(Long studentId) {
		LessonHistory entity = queryFactory
			.select(lessonHistory)
			.from(lessonHistory)
			.innerJoin(lessonHistory.trainer).fetchJoin()
			.innerJoin(lessonHistory.student).fetchJoin()
			.innerJoin(lessonHistory.schedule).fetchJoin()
			.where(lessonHistory.student.id.eq(studentId))
			.orderBy(lessonHistory.schedule.lessonDt.desc())
			.limit(1)
			.fetchOne();

		return RetrieveLessonHistoryByDateCondResult.top1From(entity);
	}

	@Override
	public Page<LessonHistory> findAllMyLessonHistory(RetrieveLessonHistoryByDateCond request, Pageable pageable,
		CustomMemberDetails member) {
		List<LessonHistory> results = queryFactory
			.selectDistinct(lessonHistory)
			.from(lessonHistory)
			.innerJoin(lessonHistory.trainer).fetchJoin()
			.innerJoin(lessonHistory.student).fetchJoin()
			.innerJoin(lessonHistory.schedule).fetchJoin()
			.leftJoin(lessonHistory.files, lessonHistoryFiles).fetchJoin()
			.where(
				convertDateFormat(request.searchDate()),
				validateMemberTypeAndMemberIdEq(member.getMemberId(), member.getMemberType()),
				lessonHistoryFiles.lessonHistoryComment.id.isNull()
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(
				schedule.lessonDt.desc(),
				schedule.lessonStartTime.desc(),
				lessonHistory.id.desc()
			)
			.fetch();

		var totalCount = queryFactory
			.select(lessonHistory.count())
			.from(lessonHistory)
			.innerJoin(lessonHistory.trainer)
			.innerJoin(lessonHistory.student)
			.innerJoin(lessonHistory.schedule)
			.leftJoin(lessonHistory.files, lessonHistoryFiles)
			.where(
				convertDateFormat(request.searchDate()),
				validateMemberTypeAndMemberIdEq(member.getMemberId(), member.getMemberType()),
				lessonHistoryFiles.lessonHistoryComment.id.isNull()
			);

		return PageableExecutionUtils.getPage(results, pageable, () -> {
			Long count = totalCount.fetchOne();
			return count != null ? count : 0L;
		});
	}

	@Override
	public LessonHistory findOneLessonHistoryWithFiles(Long lessonHistoryId, Long trainerId) {
		return queryFactory
			.selectDistinct(lessonHistory)
			.from(lessonHistory)
			.leftJoin(lessonHistory.files).fetchJoin()
			.where(
				lessonHistory.id.eq(lessonHistoryId),
				lessonHistory.trainer.id.eq(trainerId)
			)
			.fetchOne();
	}

	@Override
	public boolean validateDuplicateLessonHistory(Long trainerId, Long studentId, Long scheduleId) {
		Long count = queryFactory
			.select(lessonHistory.count())
			.from(lessonHistory)
			.where(
				lessonHistory.trainer.id.eq(trainerId),
				lessonHistory.student.id.eq(studentId),
				lessonHistory.schedule.id.eq(scheduleId)
			)
			.fetchOne();
		return count != null && count > 0;
	}

	private BooleanExpression validateMemberTypeAndMemberIdEq(Long memberId, MemberType memberType) {
		if (memberType == TRAINER) {
			return lessonHistory.trainer.id.eq(memberId);
		}
		return lessonHistory.student.id.eq(memberId);
	}

	private BooleanExpression convertDateFormat(String searchDate) {
		if (StringUtils.isEmpty(searchDate)) {
			return null;
		}
		LocalDate firstDt = LocalDate.parse(searchDate + "-01");
		LocalDate lastDt = firstDt.withDayOfMonth(firstDt.lengthOfMonth());

		return lessonHistory.schedule.lessonDt.between(firstDt, lastDt);
	}
}
