package com.junghaebom.geonganghaegym.member.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.member.application.MemberAuthService;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.FindMemberUserId;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.FindMemberUserId.FindMemberUserIdResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.InvitationMappingResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
@Valid
@Tag(name = "01. 회원 인증 API", description = "인증/권한 없이 접근할 수 있는 회원 API")
public class MemberAuthController {

	private final MemberAuthService memberAuthService;

	@Operation(summary = "아이디 중복 확인하기")
	@GetMapping("/validation/user-id")
	public ApiResult<Boolean> validateUsernameDuplication(@RequestParam String userId) {
		return ApiResult.success("사용할 수 있는 아이디입니다.", memberAuthService.validateUserIdDuplication(userId));
	}

	@Operation(summary = "아이디를 찾는다.", description = "이메일과 이름을 기준으로 일치하는 아이디를 찾는다.(소셜은 찾을 수 없음)")
	@PostMapping("/find/user-id")
	public ApiResult<FindMemberUserIdResult> findUserId(@RequestBody @Valid FindMemberUserId request) {
		FindMemberUserIdResult userIdResult = memberAuthService.findUserId(request);
		return ApiResult.success(userIdResult.message(), userIdResult);
	}

	@Operation(summary = "초대링크 uuid 데이터 조회")
	@GetMapping("/invitation/uuid")
	public ApiResult<InvitationMappingResult> getInvitationMapping(@RequestParam String uuid) {
		return ApiResult.success("조회가 완료되었습니다.", memberAuthService.getInvitationMapping(uuid));
	}
}
