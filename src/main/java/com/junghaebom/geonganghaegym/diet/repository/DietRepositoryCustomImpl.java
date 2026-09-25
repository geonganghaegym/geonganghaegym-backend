package com.junghaebom.geonganghaegym.diet.repository;


import static com.junghaebom.geonganghaegym.diet.domain.QDiet.*;
import static com.junghaebom.geonganghaegym.diet.domain.QDietFiles.*;
import static com.junghaebom.geonganghaegym.diet.domain.QDietLike.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietDto;
import com.junghaebom.geonganghaegym.diet.presentation.dto.QDietDto;
import com.junghaebom.geonganghaegym.diet.domain.Diet;
import com.junghaebom.geonganghaegym.diet.domain.DietFiles;
import com.junghaebom.geonganghaegym.member.domain.Member;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DietRepositoryCustomImpl implements DietRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Diet getTodayDiet(Long memberId) {
		return queryFactory.select(diet)
			.from(diet)
			.where(convertEatDate_YYYY_MM_DD(LocalDate.now().toString())
				, delYnEq(false)
				, memberIdEq(memberId))
			.orderBy(diet.createdAt.desc())
			.limit(1)
			.fetchOne();
	}

	@Override
	public List<String> getDietUploadDays(Long memberId, LocalDate startDate, LocalDate endDate) {
		return queryFactory
			.select(Expressions.stringTemplate(
				"DATE_FORMAT({0}, {1})"
				, diet.eatDate
				, ConstantImpl.create("%Y-%m-%d")))
			.from(diet)
			.where(memberIdEq(memberId), delYnEq(false), eatDateBetween(startDate, endDate))
			.fetch();
	}

	@Override
	public DietDto getDietById(Long loginMemberId, Long dietId) {
		return queryFactory.select(new QDietDto(diet.dietId, diet.member
				, isLiked()
				, diet.likeCnt, diet.commentCnt, diet.eatDate, diet.fastBreakfast, diet.fastLunch, diet.fastDinner))
			.from(diet)
			.leftJoin(dietLike)
			.on(diet.dietId.eq(dietLike.dietLikePK.diet.dietId)
				, dietLike.dietLikePK.member.id.eq(loginMemberId))
			.where(dietIdEq(dietId), delYnEq(false))
			.fetchOne();
	}

	@Override
	public Page<DietDto> getDietOfMonth(Long loginMemberId, Long memberId, Pageable pageable, String searchDate) {
		Long totalCnt = queryFactory
			.select(diet.count())
			.from(diet)
			.where(memberIdEq(memberId), delYnEq(false), convertEatDate_YYYY_MM(searchDate))
			.fetchOne();
		List<DietDto> diets = queryFactory
			.select(new QDietDto(diet.dietId, diet.member
				, isLiked()
				, diet.likeCnt, diet.commentCnt, diet.eatDate, diet.fastBreakfast, diet.fastLunch, diet.fastDinner))
			.from(diet)
			.leftJoin(dietLike)
			.on(diet.dietId.eq(dietLike.dietLikePK.diet.dietId)
				, dietLike.dietLikePK.member.id.eq(loginMemberId))
			.where(memberIdEq(memberId), delYnEq(false), convertEatDate_YYYY_MM(searchDate))
			.orderBy(diet.eatDate.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
		return PageableExecutionUtils.getPage(diets, pageable, () -> totalCnt);
	}

	@Override
	public List<DietFiles> getDietFile(List<Long> ids) {
		return queryFactory.select(dietFiles)
			.from(dietFiles)
			.where(dietFiles.diet.dietId.in(ids), dietFileDeYnEq(false))
			.orderBy(dietFiles.createdAt.desc())
			.fetch();
	}

	@Override
	public Page<Diet> getDietByTrainer(Member trainer, Pageable pageable, String searchDate) {
		Long totalCnt = queryFactory
			.select(diet.count())
			.from(diet)
			.where(dietTrainerIdEq(trainer), dietDeYnEq(false), convertEatDate_YYYY_MM_DD(searchDate))
			.fetchOne();
		List<Diet> diets = queryFactory
			.select(diet)
			.from(diet)
			.where(dietTrainerIdEq(trainer), dietDeYnEq(false), convertEatDate_YYYY_MM_DD(searchDate))
			.orderBy(diet.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
		return PageableExecutionUtils.getPage(diets, pageable, () -> totalCnt);
	}

	private BooleanExpression isLiked() {
		return new CaseBuilder()
			.when(dietLike.dietLikePK.member.id.isNotNull())
			.then(true)
			.otherwise(false).as("is_liked");
	}

	private BooleanExpression dietIdEq(Long dietId) {
		if (!ObjectUtils.isEmpty(dietId)) {
			return diet.dietId.eq(dietId);
		}
		return null;
	}

	private BooleanExpression dietTrainerIdEq(Member trainer) {
		if (!ObjectUtils.isEmpty(trainer)) {
			return diet.trainer.id.eq(trainer.getId());
		}
		return null;
	}

	private BooleanExpression dietDeYnEq(boolean bool) {
		if (!ObjectUtils.isEmpty(bool)) {
			return diet.delYn.eq(bool);
		}
		return null;
	}

	private BooleanExpression dietFileDeYnEq(boolean bool) {
		if (!ObjectUtils.isEmpty(bool)) {
			return dietFiles.delYn.eq(bool);
		}
		return null;
	}

	private BooleanExpression convertEatDate_YYYY_MM(String searchDate) {
		if (ObjectUtils.isEmpty(searchDate))
			return null;
		StringTemplate stringTemplate = Expressions.stringTemplate(
			"DATE_FORMAT({0}, {1})"
			, diet.eatDate
			, ConstantImpl.create("%Y-%m"));
		return stringTemplate.eq(searchDate);
	}

	private BooleanExpression eatDateBetween(LocalDate startDate, LocalDate endDate) {
		if (!ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate)) {
			return diet.eatDate.between(startDate, endDate);
		}
		return null;
	}

	private BooleanExpression convertEatDate_YYYY_MM_DD(String searchDate) {
		if (ObjectUtils.isEmpty(searchDate))
			return null;
		StringTemplate stringTemplate = Expressions.stringTemplate(
			"DATE_FORMAT({0}, {1})"
			, diet.eatDate
			, ConstantImpl.create("%Y-%m-%d"));
		return stringTemplate.eq(searchDate);
	}

	private BooleanExpression delYnEq(boolean bool) {
		if (!ObjectUtils.isEmpty(bool)) {
			return diet.delYn.eq(bool);
		}
		return null;
	}

	private BooleanExpression memberIdEq(Long memberId) {
		if (!ObjectUtils.isEmpty(memberId)) {
			return diet.member.id.eq(memberId);
		}
		return null;
	}

}
