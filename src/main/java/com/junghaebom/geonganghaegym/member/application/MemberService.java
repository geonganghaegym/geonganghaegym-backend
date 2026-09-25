package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.ValidateCurrentPassword;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.MemberInfoResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.RetrieveTrainerInfo;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.TrainerMappingResult;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

	private final MemberRepository memberRepository;
	private final TrainerMemberMappingRepository mappingRepository;
	private final PasswordEncoder passwordEncoder;

	public MemberInfoResult getMemberInfo(Long memberId) {
		Member member = memberRepository.findByIdAndDelYnFalse(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		return MemberInfoResult.create(member);
	}

	public TrainerMappingResult getTrainerMapping(Member member) {
		TrainerMemberMapping mapping = mappingRepository.findTop1ByMemberIdOrderByCreatedAtDesc(member.getId())
			.orElse(null);
		return new TrainerMappingResult(mapping != null);
	}

	public Boolean validateCurrentPassword(ValidateCurrentPassword request, Long memberId) {
		memberRepository.findById(memberId).ifPresentOrElse(
			m -> {
				if (!passwordEncoder.matches(request.password(), m.getPassword())) {
					throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
				}
			}, () -> {
				throw new CustomException(MEMBER_NOT_FOUND);
			});
		return true;
	}

	public RetrieveTrainerInfo findMyTrainerInfo(Long studentId) {
		return mappingRepository.findTrainerInfoByMemberId(studentId)
			.map(RetrieveTrainerInfo::from)
			.orElse(null);
	}
}
