package com.junghaebom.geonganghaegym.trainer.respository;

import static com.junghaebom.geonganghaegym.member.domain.QMember.*;
import static com.junghaebom.geonganghaegym.trainer.domain.QTrainerMemberMapping.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TrainerMemberMappingRepositoryCustomImpl implements TrainerMemberMappingRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Long> findAllTrainerIds() {
		return queryFactory
			.select(trainerMemberMapping.trainer.id)
			.from(trainerMemberMapping)
			.groupBy(trainerMemberMapping.trainer)
			.orderBy(trainerMemberMapping.trainer.id.asc())
			.fetch();
	}

	@Override
	public Optional<TrainerMemberMapping> findTrainerInfoByMemberId(Long memberId) {
		TrainerMemberMapping result = queryFactory
			.select(trainerMemberMapping)
			.from(trainerMemberMapping)
			.leftJoin(trainerMemberMapping.trainer, member).fetchJoin()
			.leftJoin(member.gym).fetchJoin()
			.leftJoin(member.memberProfile).fetchJoin()
			.where(
				trainerMemberMapping.member.id.eq(memberId)
			)
			.fetchOne();
		return Optional.ofNullable(result);
	}
}
