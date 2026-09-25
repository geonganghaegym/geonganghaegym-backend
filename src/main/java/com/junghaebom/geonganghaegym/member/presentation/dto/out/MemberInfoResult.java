package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymDto;
import com.junghaebom.geonganghaegym.member.presentation.dto.ProfileDto;
import com.junghaebom.geonganghaegym.member.domain.AlarmStatus;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.member.domain.SocialType;

public record MemberInfoResult(
	Long id,
	String userId,
	String email,
	String name,
	ProfileDto profile,
	GymDto gym,
	MemberType memberType,
	AlarmStatus pushAlarmStatus,
	AlarmStatus communityAlarmStatus,
	AlarmStatus feedbackAlarmStatus,
	AlarmStatus scheduleNoticeStatus,
	SocialType socialType
) {

	public static MemberInfoResult create(Member member) {
		ProfileDto profileDto = member.getMemberProfile() != null
			? ProfileDto.from(member.getMemberProfile()) : null;
		GymDto gymDto = member.getGym() != null
			? GymDto.from(member.getGym()) : null;
		return new MemberInfoResult(
			member.getId(),
			member.getUserId(),
			member.getEmail(),
			member.getName(),
			profileDto,
			gymDto,
			member.getMemberType(),
			member.getPushAlarmStatus(),
			member.getCommunityAlarmStatus(),
			member.getFeedbackAlarmStatus(),
			member.getScheduleNoticeStatus(),
			member.getSocialType()
		);
	}
}
