package com.junghaebom.geonganghaegym.member.presentation.dto;

import org.springframework.util.ObjectUtils;

import com.junghaebom.geonganghaegym.member.domain.MemberProfile;

public record ProfileDto(
	Long id,
	String fileUrl
) {

	public static ProfileDto from(MemberProfile memberProfile) {
		if (ObjectUtils.isEmpty(memberProfile)) {
			return null;
		}
		return new ProfileDto(
			memberProfile.getId(),
			memberProfile.getFileUrl()
		);
	}
}
