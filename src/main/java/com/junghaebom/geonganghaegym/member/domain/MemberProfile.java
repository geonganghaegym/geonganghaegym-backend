package com.junghaebom.geonganghaegym.member.domain;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import javax.annotation.Nullable;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "member_profile_id")
	private Long id;
	@Nullable
	private String fileUrl;

	private String fileName;

	public static MemberProfile create(String fileName, String fileUrl) {
		return MemberProfile.builder()
			.fileName(fileName)
			.fileUrl(fileUrl)
			.build();
	}

	public void change(String fileName, String fileUrl) {
		this.fileName = fileName;
		this.fileUrl = fileUrl;
	}
}
