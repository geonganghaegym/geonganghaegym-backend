package com.junghaebom.geonganghaegym.member.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.query.parser.PartTree;

import com.junghaebom.geonganghaegym.member.domain.NonMember;

class NonMemberRepositoryTest {

	private static final String UUID = "1727000000000-0b1c2d3e-aaaa-bbbb-cccc-1234567890ab";

	@Test
	@DisplayName("uuid 조회는 새 도메인과 옛 도메인 초대 링크를 모두 정확 일치로 찾는다")
	void findByInvitationUuidMatchesCurrentAndLegacyLinks() {
		NonMemberRepository repository = mock(NonMemberRepository.class, CALLS_REAL_METHODS);
		doReturn(Optional.empty()).when(repository).findByInvitationLinkIn(anyList());

		repository.findByInvitationUuid(UUID);

		verify(repository).findByInvitationLinkIn(List.of(
			"https://geonganghaejim.site/invite?type=student&uuid=" + UUID,
			"https://main.to-be-healthy.shop/invite?type=student&uuid=" + UUID
		));
	}

	@Test
	@DisplayName("파생 쿼리 이름이 NonMember 속성으로 해석된다 (컨텍스트 기동 시점 실패 방지)")
	void derivedQueryNameResolvesAgainstEntity() {
		assertDoesNotThrow(() -> new PartTree("findByInvitationLinkIn", NonMember.class));
	}
}
