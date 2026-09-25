package com.junghaebom.geonganghaegym.trainer.respository;

import java.util.List;
import java.util.Optional;

import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;

public interface TrainerMemberMappingRepositoryCustom {
	List<Long> findAllTrainerIds();

	Optional<TrainerMemberMapping> findTrainerInfoByMemberId(Long memberId);
}
