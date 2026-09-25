package com.junghaebom.geonganghaegym.member.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class MemberProfile extends BaseTimeEntity<MemberProfile, Long> {

	@OneToMany(mappedBy = "memberProfile", fetch = LAZY)
	private final List<Member> member = new ArrayList<>();
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "member_profile_id")
	private Long id;
	@Nullable
	private String fileUrl;

	private String fileName;

	public static MemberProfile create(String fileName, String fileUrl, Member member) {
		MemberProfile memberProfile = MemberProfile.builder()
			.fileName(fileName)
			.fileUrl(fileUrl)
			.build();

		memberProfile.getMember().add(member);

		return memberProfile;
	}
}
