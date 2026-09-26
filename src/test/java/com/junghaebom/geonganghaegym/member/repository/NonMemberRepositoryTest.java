package com.junghaebom.geonganghaegym.member.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.query.parser.PartTree;

import com.junghaebom.geonganghaegym.member.domain.NonMember;

class NonMemberRepositoryTest {

	private static final String UUID = "1727000000000-0b1c2d3e-aaaa-bbbb-cccc-1234567890ab";

	@Test
	@DisplayName("uuid 조회는 현재 웹 주소 기준 초대 링크를 정확 일치로 찾는다")
	void findByInvitationUuidMatchesCurrentLink() {
		NonMemberRepository repository = mock(NonMemberRepository.class, CALLS_REAL_METHODS);
		doReturn(Optional.empty()).when(repository).findByInvitationLink(anyString());

		repository.findByInvitationUuid(UUID);

		verify(repository).findByInvitationLink("https://geonganghaejim.site/invite?type=student&uuid=" + UUID);
	}

	@Test
	@DisplayName("파생 쿼리 이름이 NonMember 속성으로 해석된다 (컨텍스트 기동 시점 실패 방지)")
	void derivedQueryNameResolvesAgainstEntity() {
		assertDoesNotThrow(() -> new PartTree("findByInvitationLink", NonMember.class));
	}
}
