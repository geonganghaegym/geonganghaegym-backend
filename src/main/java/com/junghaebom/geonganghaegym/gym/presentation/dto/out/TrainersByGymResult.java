package com.junghaebom.geonganghaegym.gym.presentation.dto.out;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberProfile;

public record TrainersByGymResult(
	Long id,
	String userId,
	String email,
	String name,
	MemberProfileResult profile
) {
	public static TrainersByGymResult from(Member member) {
		return new TrainersByGymResult(
			member.getId(),
			member.getUserId(),
			member.getEmail(),
			member.getName(),
			MemberProfileResult.from(member.getMemberProfile())
		);
	}

	public record MemberProfileResult(
		Long id,
		String fileUrl
	) {
		public static MemberProfileResult from(MemberProfile memberProfile) {
			if (memberProfile == null) {
				return new MemberProfileResult(null, null);
			}
			return new MemberProfileResult(
				memberProfile.getId(),
				memberProfile.getFileUrl()
			);
		}
	}
}
