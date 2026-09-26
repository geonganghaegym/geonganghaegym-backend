package com.junghaebom.geonganghaegym.common.aop;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandLoginMember;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandRefreshToken;

class LogAspectTest {

	@Test
	@DisplayName("로그인 요청 인자를 로그로 남길 때 비밀번호 값을 가리고 나머지 필드는 남긴다")
	void masksPasswordInLoginArgs() {
		Object[] args = {new CommandLoginMember("healthy-trainer0", "12345678a", MemberType.TRAINER, true)};

		String logged = LogAspect.mask(Arrays.toString(args));

		assertThat(logged)
			.doesNotContain("12345678a")
			.contains("password=****", "userId=healthy-trainer0", "memberType=TRAINER", "complimentaryLogin=true");
	}

	@Test
	@DisplayName("갱신 토큰 요청은 토큰 값을 가린다")
	void masksRefreshToken() {
		String logged = LogAspect.mask(new CommandRefreshToken("user", "eyJ.abc.def").toString());

		assertThat(logged).doesNotContain("eyJ.abc.def").contains("refreshToken=****", "userId=user");
	}

	@Test
	@DisplayName("엔티티 toString처럼 괄호 안에 있는 password 필드도 가린다")
	void masksNestedPassword() {
		String logged = LogAspect.mask("CustomMemberDetails(member=Member(id=5, userId=u, password=$2a$10$hash, name=김))");

		assertThat(logged).doesNotContain("$2a$10$hash").contains("password=****, name=김");
	}
}
