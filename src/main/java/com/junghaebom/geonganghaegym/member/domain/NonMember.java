package com.junghaebom.geonganghaegym.member.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Builder
@ToString
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class NonMember {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "nonmember_id")
	private Long id;

	@OneToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	@ToString.Exclude
	private Member member;

	private String invitationLink;
	private String name;
	private Long trainerId;
	private int lessonCnt;

	public static NonMember create(Member member, String invitationLink, String name, Long trainerId, int lessonCnt) {
		return NonMember.builder()
			.member(member)
			.invitationLink(invitationLink)
			.name(name)
			.trainerId(trainerId)
			.lessonCnt(lessonCnt)
			.build();
	}
}
