package com.junghaebom.geonganghaegym.member.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.member.application.MemberAuthCommandService;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandFindMemberPassword;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandJoinMember;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandLoginMember;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandRefreshToken;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandSocialLogin;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandValidateEmail;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandVerification;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.CommandFindMemberPasswordResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.CommandJoinMemberResult;
import com.junghaebom.geonganghaegym.member.domain.Tokens;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "01. 회원 인증 API", description = "인증/권한 없이 접근할 수 있는 회원 API")
public class MemberAuthCommandController {

	private final MemberAuthCommandService memberAuthCommandService;

	@Operation(summary = "이메일로 인증번호를 전송한다.", description = "이메일로 6자리의 난수를 만들어 3분간 유효한 인증번호를 전송한다.")
	@PostMapping("/validation/send-email")
	public ApiResult<String> sendEmailVerification(@RequestBody @Valid CommandValidateEmail request) {
		return ApiResult.success("이메일로 인증번호를 발송중이에요!", memberAuthCommandService.sendEmailVerification(request));
	}

	@Operation(summary = "이메일 인증번호를 검증한다.", description = "이메일로 전송된 인증번호를 3분안에 입력해야 검증에 성공한다.")
	@PostMapping("/validation/confirm-email")
	public ApiResult<Boolean> verifyAuthMail(@RequestBody @Valid CommandVerification reuqest) {
		return ApiResult.success("인증번호가 확인되었습니다.", memberAuthCommandService.verifyEmailAuthNumber(reuqest));
	}

	@Operation(summary = "회원가입", description = "이름, 비밀번호 규칙, 아이디, 이메일 중복을 검증하고 비밀번호는 암호화해서 가입시킨다.")
	@PostMapping("/join")
	public ApiResult<CommandJoinMemberResult> join(@RequestBody @Valid CommandJoinMember request) {
		return ApiResult.success("회원가입이 완료되었습니다.", memberAuthCommandService.joinMember(request));
	}

	@Operation(summary = "로그인", description = "로그인에 성공하면, Access/Refresh token, userId, memberType, gymId를 반환한다.")
	@PostMapping("/login")
	public ApiResult<Tokens> login(@RequestBody @Valid CommandLoginMember request) {
		return ApiResult.success("로그인 되었습니다.", memberAuthCommandService.login(request));
	}

	@Operation(summary = "토큰을 갱신한다.", description = "refresh token이 유효하면 AccessToken을 생성하여 반환한다.")
	@PostMapping("/refresh-token")
	public ApiResult<Tokens> refreshToken(@RequestBody @Valid CommandRefreshToken request) {
		return ApiResult.success("토큰이 갱신되었습니다.", memberAuthCommandService.refreshToken(request));
	}

	@Operation(summary = "비밀번호 찾기", description = "등록된 이메일로 초기화 비밀번호를 전송한다.")
	@PostMapping("/find/password")
	public ApiResult<CommandFindMemberPasswordResult> findMemberPW(
		@RequestBody @Valid CommandFindMemberPassword request) {
		CommandFindMemberPasswordResult findMemberPasswordResult = memberAuthCommandService.findMemberPW(request);
		return ApiResult.success(findMemberPasswordResult.message(), findMemberPasswordResult);
	}

	@Operation(summary = "네이버 소셜 로그인", description = "인가코드로 네이버에서 정보를 받아온 뒤에, 로그인 프로세스를 거친다. 비회원인 경우 회원가입 프로세스를 추가로 거친다.")
	@PostMapping("/access-token/naver")
	public ApiResult<Tokens> getNaverAccessToken(@RequestBody CommandSocialLogin request) {
		return ApiResult.success("요청이 처리되었습니다.", memberAuthCommandService.getNaverAccessToken(request));
	}

	@Operation(summary = "카카오 소셜 로그인", description = "인가코드로 카카오에서 정보를 받아온 뒤에, 로그인 프로세스를 거친다. 비회원인 경우 회원가입 프로세스를 추가로 거친다.")
	@PostMapping("/access-token/kakao")
	public ApiResult<Tokens> getKakaoAccessToken(@RequestBody CommandSocialLogin request) {
		return ApiResult.success("요청이 처리되었습니다.", memberAuthCommandService.getKakaoAccessToken(request));
	}

	@Operation(summary = "구글 소셜 로그인", description = "인가코드로 구글에서 정보를 받아온 뒤에, 로그인 프로세스를 거친다. 비회원인 경우 회원가입 프로세스를 추가로 거친다.")
	@PostMapping("/access-token/google")
	public ApiResult<Tokens> getGoogleOAuth(@RequestBody CommandSocialLogin command) {
		return ApiResult.success("요청이 처리되었습니다.", memberAuthCommandService.getGoogleOAuth(command));
	}

	@Operation(summary = "애플 소셜 로그인", description = "인가코드로 애플에서 정보를 받아온 뒤에, 로그인 프로세스를 거친다. 비회원인 경우 회원가입 프로세스를 추가로 거친다.")
	@PostMapping("/access-token/apple")
	public ApiResult<Tokens> getAppleOAuth(@RequestBody CommandSocialLogin request) {
		return ApiResult.success("요청이 처리되었습니다.", memberAuthCommandService.getAppleOAuth(request));
	}
}
