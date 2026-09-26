package com.junghaebom.geonganghaegym.member.repository;

import static com.junghaebom.geonganghaegym.common.Utils.WEB_BASE_URL;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.member.domain.NonMember;

public interface NonMemberRepository extends JpaRepository<NonMember, Long> {

	/**
	 * 초대 링크의 uuid로 미가입 회원을 찾는다. 링크 전체가 조회 키라 도메인까지 정확히 일치해야 한다.
	 */
	default Optional<NonMember> findByInvitationUuid(String uuid) {
		return findByInvitationLink(WEB_BASE_URL + "/invite?type=student&uuid=" + uuid);
	}

	Optional<NonMember> findByInvitationLink(String invitationLink);
}
