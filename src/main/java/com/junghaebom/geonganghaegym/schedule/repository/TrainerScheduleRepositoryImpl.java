package com.junghaebom.geonganghaegym.schedule.repository;


import static com.junghaebom.geonganghaegym.lessonhistory.domain.QLessonHistory.*;
import static com.junghaebom.geonganghaegym.member.domain.QMember.*;
import static com.junghaebom.geonganghaegym.schedule.domain.QSchedule.*;
import static com.junghaebom.geonganghaegym.schedule.domain.QScheduleWaiting.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.UnwrittenLessonHistorySearchCond;
import com.junghaebom.geonganghaegym.lessonhistory.domain.WritingStatus;
import com.junghaebom.geonganghaegym.member.domain.AlarmStatus;
import com.junghaebom.geonganghaegym.member.domain.QMember;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.CommandRegisterSchedule;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.FeedbackNotificationToTrainer;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonDtResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonInfoResult;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.domain.TrainerScheduleInfo;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TrainerScheduleRepositoryImpl implements TrainerScheduleRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Schedule> findAllSchedule(
		String lessonDt,
		LocalDate lessonStartDt,
		LocalDate lessonEndDt,
		Long trainerId
	) {
		return queryFactory
			.select(schedule)
			.from(schedule)
			.leftJoin(schedule.trainer, new QMember("trainer")).fetchJoin()
			.leftJoin(schedule.applicant, new QMember("applicant")).fetchJoin()
			.leftJoin(schedule.scheduleWaiting, scheduleWaiting).fetchJoin()
			.where(
				lessonDtMonthEq(lessonDt),
				lessonDtBetween(lessonStartDt, lessonEndDt),
				trainerIdEq(trainerId)
			)
			.orderBy(schedule.lessonDt.asc(), schedule.lessonStartTime.asc())
			.fetch();
	}

	@Override
	public RetrieveTrainerScheduleByLessonDtResult findOneTrainerTodaySchedule(
		String lessonDt,
		Long trainerId
	) {
		List<Schedule> results = queryFactory
			.select(schedule)
			.from(schedule)
			.leftJoin(schedule.trainer, new QMember("trainer")).fetchJoin()
			.leftJoin(schedule.applicant, new QMember("applicant")).fetchJoin()
			.where(
				lessonDtEq(lessonDt),
				trainerIdEq(trainerId),
				reservationStatusEq(ReservationStatus.COMPLETED)
			)
			.orderBy(schedule.lessonDt.asc(), schedule.lessonStartTime.asc())
			.fetch();

		Long scheduleCount = queryFactory
			.select(schedule.count())
			.from(schedule)
			.where(
				lessonDtEq(lessonDt),
				trainerIdEq(trainerId),
				reservationStatusEq(ReservationStatus.COMPLETED)
			)
			.fetchOne();

		RetrieveTrainerScheduleByLessonInfoResult response = RetrieveTrainerScheduleByLessonInfoResult.from(results);

		RetrieveTrainerScheduleByLessonDtResult trainerTodaySchedule = new RetrieveTrainerScheduleByLessonDtResult(
			response.trainerName(),
			scheduleCount != null ? scheduleCount : 0L
		);

		if (response.schedule() != null) {
			response.schedule().forEach((key, value) -> {
				value.stream()
					.filter(it -> it.lessonStartTime() != null && it.lessonStartTime().isAfter(LocalTime.now()))
					.forEach(it -> trainerTodaySchedule.schedule().add(it));
			});
		}

		return trainerTodaySchedule;
	}

	@Override
	public boolean validateDuplicateSchedule(
		TrainerScheduleInfo trainerScheduleInfo,
		CommandRegisterSchedule request,
		Long trainerId
	) {
		List<Integer> closedDayValues = trainerScheduleInfo.getTrainerScheduleClosedDays().stream()
			.map(day -> day.getClosedDays().getValue())
			.collect(Collectors.toList());

		Long count = queryFactory
			.select(schedule.count())
			.from(schedule)
			.where(
				lessonDtBetween(request.lessonStartDt(), request.lessonEndDt()),
				trainerIdEq(trainerId),
				notDayOfWeek(schedule.lessonDt, closedDayValues),
				schedule.lessonStartTime.between(
					trainerScheduleInfo.getLessonStartTime(),
					trainerScheduleInfo.getLessonEndTime()
				),
				schedule.lessonEndTime.between(
					trainerScheduleInfo.getLessonStartTime(),
					trainerScheduleInfo.getLessonEndTime()
				)
			)
			.fetchOne();
		return count != null && count > 0;
	}

	private BooleanExpression notDayOfWeek(DatePath<LocalDate> dateTimePath, List<Integer> dayOfWeekValues) {
		return dateTimePath.dayOfWeek().notIn(dayOfWeekValues);
	}

	@Override
	public Optional<Schedule> findAvailableWaitingId(Long scheduleId) {
		Schedule result = queryFactory
			.select(schedule)
			.from(schedule)
			.leftJoin(schedule.scheduleWaiting, scheduleWaiting)
			.where(
				scheduleIdEq(scheduleId),
				reservationStatusEq(ReservationStatus.COMPLETED),
				applicantIsNotNull()
			)
			.fetchOne();
		return Optional.ofNullable(result);
	}

	@Override
	public List<Schedule> findAllSchedule(
		List<Long> scheduleIds,
		List<ReservationStatus> reservationStatus,
		Long trainerId
	) {
		return queryFactory
			.select(schedule)
			.from(schedule)
			.where(
				scheduleIdIn(scheduleIds),
				trainerIdEq(trainerId),
				reservationStatusIn(reservationStatus)
			)
			.fetch();
	}

	@Override
	public Schedule findAllSchedule(Long scheduleId, ReservationStatus reservationStatus, Long trainerId) {
		return queryFactory
			.select(schedule)
			.from(schedule)
			.where(
				scheduleIdEq(scheduleId),
				trainerIdEq(trainerId),
				reservationStatusEq(reservationStatus)
			)
			.fetchOne();
	}

	@Override
	public List<Schedule> findAllDisabledSchedule(LocalDate lessonStartDt, LocalDate lessonEndDt) {
		return queryFactory
			.select(schedule)
			.from(schedule)
			.leftJoin(schedule.scheduleWaiting).fetchJoin()
			.where(
				lessonDtBetween(lessonStartDt, lessonEndDt),
				reservationStatusEq(ReservationStatus.DISABLED)
			)
			.fetch();
	}

	@Override
	public List<Schedule> findAllUnwrittenLessonHistory(
		UnwrittenLessonHistorySearchCond request,
		Long memberId
	) {
		return queryFactory
			.select(schedule)
			.from(schedule)
			.leftJoin(schedule.lessonHistories, lessonHistory).fetchJoin()
			.where(
				trainerIdEq(memberId),
				schedule.applicant.isNotNull(),
				reservationStatusEq(ReservationStatus.COMPLETED),
				lessonDateTimeEq(request.lessonDate()),
				applicantIdEq(request.studentId()),
				writtenStatusEq(request.writingStatus())
			)
			.orderBy(schedule.lessonDt.asc(), schedule.lessonStartTime.asc())
			.fetch();
	}

	private BooleanExpression writtenStatusEq(WritingStatus writingStatus) {
		if (writingStatus == null) {
			return null;
		}
		switch (writingStatus) {
			case WRITTEN:
				return lessonHistory.isNotNull();
			case UNWRITTEN:
				return lessonHistory.isNull();
			default:
				return null;
		}
	}

	private BooleanExpression applicantIdEq(Long studentId) {
		if (studentId == null) {
			return null;
		}
		return schedule.applicant.id.eq(studentId);
	}

	@Override
	public List<Schedule> findAllSimpleLessonHistoryByMemberId(Long studentId, Long trainerId) {
		return queryFactory
			.select(schedule)
			.from(schedule)
			.leftJoin(schedule.lessonHistories, lessonHistory).fetchJoin()
			.where(
				schedule.applicant.id.eq(studentId),
				trainerIdEq(trainerId),
				schedule.applicant.isNotNull(),
				reservationStatusEq(ReservationStatus.COMPLETED)
			)
			.orderBy(schedule.lessonDt.asc(), schedule.lessonStartTime.asc())
			.fetch();
	}

	@Override
	public Page<Schedule> findAllScheduleByStudentId(
		Long studentId,
		Pageable pageable,
		Long trainerId
	) {
		List<Schedule> results = queryFactory
			.select(schedule)
			.from(schedule)
			.leftJoin(schedule.applicant, new QMember("applicant")).fetchJoin()
			.where(
				schedule.applicant.id.eq(studentId),
				trainerIdEq(trainerId)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(schedule.lessonDt.desc(), schedule.lessonStartTime.desc())
			.fetch();

		var totalCount = queryFactory
			.select(schedule.count())
			.from(schedule)
			.leftJoin(schedule.applicant, new QMember("applicant"))
			.where(
				schedule.applicant.id.eq(studentId),
				trainerIdEq(trainerId)
			);

		return PageableExecutionUtils.getPage(results, pageable, () -> {
			Long count = totalCount.fetchOne();
			return count != null ? count : 0L;
		});
	}

	@Override
	public List<FeedbackNotificationToTrainer> findAllFeedbackNotificationToTrainer() {
		return queryFactory
			.select(
				Projections.constructor(
					FeedbackNotificationToTrainer.class,
					schedule.trainer.id,
					schedule.count()
				)
			)
			.from(schedule)
			.innerJoin(schedule.trainer, member).on(member.feedbackAlarmStatus.eq(AlarmStatus.ENABLED))
			.leftJoin(schedule.lessonHistories, lessonHistory)
			.where(
				lessonHistory.isNull(),
				schedule.lessonDt.eq(LocalDate.now()),
				schedule.reservationStatus.eq(ReservationStatus.COMPLETED)
			)
			.groupBy(schedule.trainer.id)
			.fetch();
	}

	private BooleanExpression scheduleIdIn(List<Long> scheduleIds) {
		return schedule.id.in(scheduleIds);
	}

	private BooleanExpression applicantIsNotNull() {
		return schedule.applicant.isNotNull();
	}

	private BooleanExpression reservationStatusEq(ReservationStatus reservationStatus) {
		return schedule.reservationStatus.eq(reservationStatus);
	}

	private BooleanExpression reservationStatusIn(List<ReservationStatus> reservationStatus) {
		return schedule.reservationStatus.in(reservationStatus);
	}

	private BooleanExpression scheduleIdEq(Long scheduleId) {
		return schedule.id.eq(scheduleId);
	}

	private BooleanExpression trainerIdEq(Long trainerId) {
		return schedule.trainer.id.eq(trainerId);
	}

	private BooleanExpression lessonDtBetween(LocalDate lessonStartDt, LocalDate lessonEndDt) {
		if (!ObjectUtils.isEmpty(lessonStartDt) && !ObjectUtils.isEmpty(lessonEndDt)) {
			return schedule.lessonDt.between(lessonStartDt, lessonEndDt);
		}
		return null;
	}

	private BooleanExpression lessonDtMonthEq(String lessonDt) {
		if (!ObjectUtils.isEmpty(lessonDt)) {
			var formattedDate = Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m')", schedule.lessonDt);
			return formattedDate.eq(lessonDt);
		}
		return null;
	}

	private BooleanExpression lessonDtEq(String lessonDt) {
		if (!StringUtils.hasText(lessonDt)) {
			return null;
		}
		var formattedDate = Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", schedule.lessonDt);
		return formattedDate.eq(lessonDt);
	}

	private BooleanExpression lessonDateTimeEq(String lessonDate) {
		if (lessonDate == null) {
			return null;
		}
		var formattedDate = Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", schedule.lessonDt);
		return formattedDate.eq(lessonDate);
	}
}
