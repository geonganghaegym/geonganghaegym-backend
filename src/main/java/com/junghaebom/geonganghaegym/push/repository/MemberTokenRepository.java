package com.junghaebom.geonganghaegym.push.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.push.domain.MemberToken;

public interface MemberTokenRepository extends JpaRepository<MemberToken, Long> {

	// 옛 구조에서 같은 토큰이 중복 저장된 행이 있어 단건 조회 대신 첫 행을 쓴다
	Optional<MemberToken> findFirstByToken(String token);

	List<MemberToken> findAllByMemberId(Long memberId);
}
