package com.junghaebom.geonganghaegym.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.member.repository.dto.MemberDetailResult;
import com.junghaebom.geonganghaegym.member.repository.dto.MemberInTeamResult;
import com.junghaebom.geonganghaegym.member.domain.Member;

public interface MemberRepositoryCustom {
	List<MemberInTeamResult> findAllMyMemberInTeam(Long trainerId, String searchValue, String sortValue,
		Pageable pageable);

	Page<Member> findAllUnattachedMembers(Long gymId, String searchValue, String sortValue, Pageable pageable);

	MemberDetailResult getMemberOfTrainer(Long memberId);

	List<MemberInTeamResult> getBestStudent(Long trainerId);

	Optional<Member> findMemberById(Long memberId);

	List<Member> findMemberTokenById(List<Long> memberId);
}
