package com.junghaebom.geonganghaegym.push.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.query.parser.PartTree;

import com.junghaebom.geonganghaegym.push.domain.MemberToken;

class MemberTokenRepositoryTest {

	@Test
	@DisplayName("파생 쿼리 이름이 MemberToken 속성으로 해석된다 (컨텍스트 기동 시점 실패 방지)")
	void derivedQueryNamesResolveAgainstEntity() {
		assertDoesNotThrow(() -> new PartTree("findFirstByToken", MemberToken.class));
		assertDoesNotThrow(() -> new PartTree("findAllByMemberId", MemberToken.class));
	}
}
