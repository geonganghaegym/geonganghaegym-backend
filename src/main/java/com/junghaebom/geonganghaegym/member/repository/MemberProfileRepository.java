package com.junghaebom.geonganghaegym.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.member.domain.MemberProfile;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
}
