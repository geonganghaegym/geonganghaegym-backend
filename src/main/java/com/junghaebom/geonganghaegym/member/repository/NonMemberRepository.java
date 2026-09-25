package com.junghaebom.geonganghaegym.member.repository;

import static com.junghaebom.geonganghaegym.common.Utils.WEB_BASE_URL;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.member.domain.NonMember;

public interface NonMemberRepository extends JpaRepository<NonMember, Long> {

	/**
	 * 초대 링크의 uuid로 미가입 회원을 찾는다. 링크 전체가 조회 키라 도메인까지 정확히 일치해야 한다.
	 */
	default Optional<NonMember> findByInvitationUuid(String uuid) {
		String path = "/invite?type=student&uuid=" + uuid;
		// ponytail: 도메인 이전(to-be-healthy.shop → geonganghaejim.site) 전에 저장된 행 호환용.
		// 옛 도메인 행이 모두 정리되면 두 번째 후보를 지운다.
		String legacyBaseUrl = "https://main.to-be-healthy.shop";
		return findByInvitationLinkIn(List.of(WEB_BASE_URL + path, legacyBaseUrl + path));
	}

	Optional<NonMember> findByInvitationLinkIn(List<String> invitationLinks);
}
