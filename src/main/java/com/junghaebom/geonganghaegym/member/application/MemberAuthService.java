package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.Utils.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.member.domain.SocialType.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.FindMemberUserId;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.FindMemberUserId.FindMemberUserIdResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.InvitationMappingResult;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.NonMember;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.member.repository.NonMemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberAuthService {

	private final MemberRepository memberRepository;
	private final RedisService redisService;
	private final ObjectMapper objectMapper;
	private final NonMemberRepository nonMemberRepository;

	public boolean validateUserIdDuplication(String userId) {
		if (validateUserId(userId)) {
			throw new CustomException(MEMBER_ID_NOT_VALID);
		}

		memberRepository.findByUserId(userId).ifPresent(m -> {
			throw new CustomException(MEMBER_ID_DUPLICATION);
		});

		return true;
	}

	public FindMemberUserIdResult findUserId(FindMemberUserId request) {

		Member member = memberRepository.findByEmailAndName(request.email(), request.name())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		if (member.getSocialType() != NONE) {
			return FindMemberUserIdResult.from(
				member,
				String.format("%s은 %s 계정으로 가입되어 있습니다.", member.getEmail(), member.getSocialType().getDescription())
			);
		}

		return FindMemberUserIdResult.from(member, "아이디 찾기에 성공하였습니다.");
	}

	public InvitationMappingResult getInvitationMapping(String uuid) {
		NonMember nonMember = getNonmemberData(uuid);
		Member member = memberRepository.findByIdAndDelYnFalse(nonMember.getTrainerId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		return InvitationMappingResult.create(member, nonMember.getName(), nonMember.getLessonCnt());
	}

	private NonMember getNonmemberData(String uuid) {
		NonMember nonMember = nonMemberRepository.findByInvitationUuid(uuid)
			.orElseThrow(() -> new CustomException(INVITE_LINK_NOT_FOUND));
		return nonMember;
	}
}
