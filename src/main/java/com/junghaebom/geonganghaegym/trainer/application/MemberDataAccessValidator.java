package com.junghaebom.geonganghaegym.trainer.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

import lombok.RequiredArgsConstructor;

/**
 * 회원 데이터(운동 기록·식단·수강권·포인트)는 본인이나 그 회원과 매핑된 트레이너만 조회할 수 있다.
 */
@Component
@RequiredArgsConstructor
public class MemberDataAccessValidator {

	private final TrainerMemberMappingRepository mappingRepository;

	@Transactional(readOnly = true)
	public void validateReadable(Long loginMemberId, Long memberId) {
		if (loginMemberId.equals(memberId)) {
			return;
		}
		mappingRepository.findByTrainerIdAndMemberId(loginMemberId, memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_MAPPED));
	}
}
