package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandRegisterMemberProfile;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.RegisterMemberProfileResult;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberCommandServiceV2 {

	private final MemberRepository memberRepository;
	private final LocalFileStorageService fileStorageService;

	public RegisterMemberProfileResult registerProfile(CommandRegisterMemberProfile request, Long memberId) {
		Member findMember = memberRepository.findMemberById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		findMember.validateModifiableAccount();

		if (ObjectUtils.isEmpty(request)) {
			throw new IllegalArgumentException("프로필 사진을 등록해 주세요.");
		}

		String tempFilePath = fileStorageService.extractFilePath(request.uploadFile().fileUrl());
		String fileName = tempFilePath.replaceFirst("temp/", "");
		String originPath = "origin/profile/" + fileName;

		fileStorageService.copy(tempFilePath, originPath);

		String fileUrl = fileStorageService.getFileUrl(originPath);
		String oldFileUrl = findMember.getMemberProfile() == null ? null : findMember.getMemberProfile().getFileUrl();

		log.info("등록한 fileUrl: {}", fileUrl);

		findMember.registerProfile(fileName, fileUrl);
		if (oldFileUrl != null) {
			fileStorageService.delete(fileStorageService.extractFilePath(oldFileUrl));
		}

		return RegisterMemberProfileResult.from(fileUrl, fileName);
	}
}
