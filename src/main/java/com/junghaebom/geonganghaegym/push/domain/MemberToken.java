package com.junghaebom.geonganghaegym.push.domain;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberToken extends BaseTimeEntity<MemberToken, Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_token_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	private DeviceType deviceType;

	@Column(unique = true)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	@ToString.Exclude
	private Member member;

	private MemberToken(Member member, String token, DeviceType deviceType) {
		this.member = member;
		this.token = token;
		this.deviceType = deviceType;
	}

	public static MemberToken register(Member member, String token, DeviceType deviceType) {
		return new MemberToken(member, token, deviceType);
	}

	public void changeOwner(Member member, DeviceType deviceType) {
		this.member = member;
		this.deviceType = deviceType;
	}
}
