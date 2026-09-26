package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import com.junghaebom.geonganghaegym.member.domain.MemberType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(title = "MemberJoinCommand", description = "회원가입 DTO")
public record CommandJoinMember(
	@Schema(description = "아이디", example = "geonganghaegym")
	@NotEmpty(message = "아이디를 입력해 주세요.")
	String userId,

	@Schema(description = "이메일", example = "geonganghaegym@example.com")
	@NotEmpty(message = "이메일을 입력해 주세요.")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "올바른 이메일 형식을 입력해 주세요.")
	String email,

	@Schema(description = "비밀번호", example = "zxcvbnm11")
	@NotEmpty(message = "비밀번호를 입력해 주세요.")
	String password,

	@Schema(description = "비밀번호 확인", example = "zxcvbnm11")
	@NotEmpty(message = "비밀번호를 재입력해 주세요.")
	String passwordConfirm,

	@Schema(description = "실명", example = "홍길동")
	@NotEmpty(message = "실명을 입력해 주세요.")
	String name,

	@Schema(description = "회원 구분", example = "STUDENT")
	@NotNull(message = "회원 구분이 필요합니다.")
	MemberType memberType,

	@Schema(description = "초대링크로 가입하는 경우 uuid")
	String uuid
) {
}
