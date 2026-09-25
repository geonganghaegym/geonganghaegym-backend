package com.junghaebom.geonganghaegym.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
	@Query("select m from Member m where m.email = :email and m.delYn = false")
	Optional<Member> findByEmail(String email);

	@Query("select m from Member m where m.userId = :userId and m.delYn = false")
	Optional<Member> findByUserId(String userId);

	@Query("select m from Member m where m.email = :email and m.name = :name and m.delYn = false")
	Optional<Member> findByEmailAndName(String email, String name);

	@Query("select m from Member m where m.id = :memberId and m.delYn = false")
	Optional<Member> findById(Long memberId);

	@EntityGraph(attributePaths = {"gym"})
	Optional<Member> findByIdAndMemberTypeAndDelYnFalse(Long memberId, MemberType memberType);

	@EntityGraph(attributePaths = {"gym", "memberProfile"})
	Optional<Member> findByIdAndDelYnFalse(Long memberId);
}
