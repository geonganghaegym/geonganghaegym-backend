package com.junghaebom.geonganghaegym.push.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.push.domain.MemberToken;

public interface MemberTokenRepository extends JpaRepository<MemberToken, Long> {

	Optional<MemberToken> findByMemberId(Long memberId);
}
