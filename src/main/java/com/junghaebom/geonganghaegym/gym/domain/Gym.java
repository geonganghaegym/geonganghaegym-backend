package com.junghaebom.geonganghaegym.gym.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@ToString(exclude = "member")
public class Gym extends BaseTimeEntity<Gym, Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "gym_id")
	private Long id;

	private String name;

	@Column(length = 6)
	private String joinCode;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "gym")
	private List<Member> member = new ArrayList<>();

	private Gym(String name, String joinCode) {
		this.name = name;
		this.joinCode = joinCode;
	}

	public static Gym registerGym(String name, String accessKey) {
		return new Gym(name, accessKey);
	}

	public void validateJoinCode(String joinCode) {
		if (!this.joinCode.equals(joinCode)) {
			throw new CustomException(ErrorCode.JOIN_CODE_NOT_VALID);
		}
	}
}
