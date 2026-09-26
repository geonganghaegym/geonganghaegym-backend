package com.junghaebom.geonganghaegym.member.domain;

import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.junghaebom.geonganghaegym.config.JpaConfig;

// application.yml의 ddl-auto: validate를 덮어써 H2에 스키마를 만든다
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(JpaConfig.class)
class MemberProfileOrphanTest {

	@Autowired
	private TestEntityManager em;

	@Test
	@DisplayName("프로필을 다시 등록해도 member_profile 행은 하나로 유지된다")
	void registerTwice_keepsSingleRow() {
		Member member = Member.join("user01", "a@b.com", "회원", STUDENT, "pw");
		member.registerProfile("origin/profile/a.png", "https://x/files/origin/profile/a.png");
		em.persist(member);
		em.flush();

		member.registerProfile("origin/profile/b.png", "https://x/files/origin/profile/b.png");
		em.flush();
		em.clear();

		assertEquals(1L, profileCount());
		assertEquals("origin/profile/b.png", em.find(Member.class, member.getId()).getMemberProfile().getFileName());
	}

	@Test
	@DisplayName("프로필을 삭제하면 member_profile 행도 지워진다")
	void delete_removesRow() {
		Member member = Member.join("user02", "c@d.com", "회원", STUDENT, "pw");
		member.registerProfile("origin/profile/a.png", "https://x/files/origin/profile/a.png");
		em.persist(member);
		em.flush();

		member.deleteProfile();
		em.flush();
		em.clear();

		assertEquals(0L, profileCount());
		assertNull(em.find(Member.class, member.getId()).getMemberProfile());
	}

	private long profileCount() {
		return em.getEntityManager()
			.createQuery("select count(p) from MemberProfile p", Long.class)
			.getSingleResult();
	}
}
