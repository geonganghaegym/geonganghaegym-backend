package com.junghaebom.geonganghaegym.member.repository;

import static com.junghaebom.geonganghaegym.course.domain.QCourse.*;
import static com.junghaebom.geonganghaegym.member.domain.AlarmStatus.*;
import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;
import static com.junghaebom.geonganghaegym.member.domain.QMember.*;
import static com.junghaebom.geonganghaegym.member.domain.QMemberProfile.*;
import static com.junghaebom.geonganghaegym.member.domain.QNonMember.*;
import static com.junghaebom.geonganghaegym.schedule.domain.QSchedule.*;
import static com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus.*;
import static com.junghaebom.geonganghaegym.trainer.domain.QTrainerMemberMapping.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.member.repository.dto.MemberDetailResult;
import com.junghaebom.geonganghaegym.member.repository.dto.MemberInTeamResult;
import com.junghaebom.geonganghaegym.member.repository.dto.QMemberDetailResult;
import com.junghaebom.geonganghaegym.member.repository.dto.QMemberInTeamResult;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public List<MemberInTeamResult> findAllMyMemberInTeam(Long trainerId, String searchValue, String sortValue,
		Pageable pageable) {
		return queryFactory
			.select(new QMemberInTeamResult(member.id, member.name, member.userId, member.email,
				trainerMemberMapping.ranking, course.totalLessonCnt, course.remainLessonCnt,
				member.nickname, memberProfile.fileUrl, nonMember.id))
			.from(trainerMemberMapping)
			.innerJoin(trainerMemberMapping.member, member).on(trainerMemberMapping.member.id.eq(member.id))
			.leftJoin(member.memberProfile, memberProfile)
			.leftJoin(course).on(course.member.id.eq(member.id), course.remainLessonCnt.gt(0))
			.leftJoin(nonMember).on(nonMember.member.id.eq(member.id))
			.where(
				mappingTrainerIdEq(trainerId),
				memberTypeEq(STUDENT),
				memberDelYnEq(false),
				nameLike(searchValue)
			)
			.orderBy(sortBy(sortValue))
			.fetch();
	}

	public List<MemberInTeamResult> getBestStudent(Long trainerId) {
		return queryFactory
			.select(new QMemberInTeamResult(
				member.id,
				member.name,
				member.userId,
				member.email,
				trainerMemberMapping.ranking,
				course.totalLessonCnt,
				course.remainLessonCnt,
				member.nickname,
				memberProfile.fileUrl,
				nonMember.id)
			)
			.from(trainerMemberMapping)
			.innerJoin(trainerMemberMapping.member, member)
			.on(trainerMemberMapping.member.id.eq(member.id))
			.leftJoin(member.memberProfile, memberProfile)
			.leftJoin(course)
			.on(course.member.id.eq(member.id), course.remainLessonCnt.gt(0))
			.leftJoin(nonMember)
			.on(nonMember.member.id.eq(member.id))
			.where(
				mappingTrainerIdEq(trainerId),
				memberTypeEq(STUDENT),
				memberDelYnEq(false),
				rankingTop1()
			)
			.orderBy(sortBy("ranking"))
			.fetch();
	}

	@Override
	public Optional<Member> findMemberById(Long memberId) {
		Member findMember = queryFactory.select(member)
			.from(member).leftJoin(member.memberProfile).fetchJoin()
			.where(memberIdEq(memberId), memberDelYnEq(false))
			.fetchOne();
		return Optional.ofNullable(findMember);
	}

	private BooleanExpression rankingTop1() {
		return trainerMemberMapping.ranking.eq(1);
	}

	@Override
	public Page<Member> findAllUnattachedMembers(Long gymId, String searchValue, String sortValue, Pageable pageable) {
		Long totalCnt = queryFactory
			.select(member.count())
			.from(member)
			.where(memberTypeEq(STUDENT)
				, memberDelYnEq(false)
				, nameLike(searchValue)
				, memberGymIdEq(gymId)
				, JPAExpressions.selectFrom(trainerMemberMapping)
					.where(trainerMemberMapping.member.eq(member))
					.notExists())
			.fetchOne();
		List<Member> members = queryFactory
			.select(member)
			.from(member)
			.where(memberTypeEq(STUDENT)
				, memberDelYnEq(false)
				, nameLike(searchValue)
				, memberGymIdEq(gymId)
				, JPAExpressions.selectFrom(trainerMemberMapping)
					.where(trainerMemberMapping.member.eq(member))
					.notExists())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
		return PageableExecutionUtils.getPage(members, pageable, () -> totalCnt);
	}

	@Override
	public MemberDetailResult getMemberOfTrainer(Long memberId) {
		return queryFactory
			.select(new QMemberDetailResult(member.id, member.name, member.nickname, memberProfile.fileUrl
				, trainerMemberMapping.memo, trainerMemberMapping.ranking
				, schedule.lessonDt, schedule.lessonStartTime, nonMember.id))
			.from(member)
			.leftJoin(memberProfile)
			.on(member.memberProfile.id.eq(memberProfile.id))
			.leftJoin(trainerMemberMapping)
			.on(member.id.eq(trainerMemberMapping.member.id))
			.leftJoin(schedule)
			.on(member.id.eq(schedule.applicant.id)
				, scheduleReservationStatusEq(COMPLETED)
				, lessonDateTimeAfterNow())
			.leftJoin(nonMember).on(nonMember.member.id.eq(member.id))
			.where(memberIdEq(memberId), memberDelYnEq(false))
			.orderBy(schedule.lessonDt.asc(), schedule.lessonStartTime.asc())
			.limit(1)
			.fetchOne();
	}

	private BooleanExpression scheduleReservationStatusEq(ReservationStatus status) {
		if (!ObjectUtils.isEmpty(status)) {
			return schedule.reservationStatus.eq(status);
		}
		return null;
	}

	@Override
	public List<Member> findAllTrainerByGym(Long gymId) {
		return queryFactory
			.select(member)
			.from(member)
			.leftJoin(member.memberProfile).fetchJoin()
			.where(
				member.gym.id.eq(gymId),
				member.memberType.eq(TRAINER),
				member.delYn.eq(false)
			)
			.orderBy(member.id.desc())
			.fetch();
	}

	@Override
	public List<Member> findMemberTokenById(List<Long> memberId) {
		return queryFactory
			.select(member)
			.from(member)
			.leftJoin(member.memberToken).fetchJoin()
			.where(
				member.id.in(memberId),
				member.pushAlarmStatus.eq(ENABLED)
			)
			.fetch();
	}

	private Predicate lessonDateTimeAfterNow() {
		return schedule.lessonDt.after(LocalDate.now())
			.or(schedule.lessonDt.goe(LocalDate.now()).and(schedule.lessonStartTime.after(LocalTime.now())));
	}

	private BooleanExpression memberIdEq(Long memberId) {
		if (!ObjectUtils.isEmpty(memberId)) {
			return member.id.eq(memberId);
		}
		return null;
	}

	private BooleanExpression nameLike(String name) {
		if (!ObjectUtils.isEmpty(name)) {
			return member.name.containsIgnoreCase(name);
		}
		return null;
	}

	private OrderSpecifier sortBy(String sortValue) {
		if (!ObjectUtils.isEmpty(sortValue)) {
			if ("ranking".equals(sortValue))
				return trainerMemberMapping.ranking.asc();
		}
		return member.id.asc();
	}

	private BooleanExpression memberDelYnEq(boolean bool) {
		if (!ObjectUtils.isEmpty(bool)) {
			return member.delYn.eq(bool);
		}
		return null;
	}

	private BooleanExpression mappingTrainerIdEq(Long trainerId) {
		if (!ObjectUtils.isEmpty(trainerId)) {
			return trainerMemberMapping.trainer.id.eq(trainerId);
		}
		return null;
	}

	private BooleanExpression memberTypeEq(MemberType memberType) {
		if (!ObjectUtils.isEmpty(memberType)) {
			return member.memberType.eq(memberType);
		}
		return null;
	}

	private BooleanExpression memberGymIdEq(Long gymId) {
		if (!ObjectUtils.isEmpty(gymId)) {
			return member.gym.id.eq(gymId);
		}
		return null;
	}

}
