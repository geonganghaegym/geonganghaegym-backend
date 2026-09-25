package com.junghaebom.geonganghaegym.member.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.member.application.MemberCommandServiceV2;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandRegisterMemberProfile;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.RegisterMemberProfileResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/members")
@Slf4j
@Valid
@Tag(name = "02. 회원 API", description = "인증이 있어야만 접근 가능한 회원 API")
public class MemberCommandControllerV2 {

	private final MemberCommandServiceV2 memberCommandServiceV2;

	@Operation(summary = "프로필 사진을 등록한다.", responses = {
		@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
		@ApiResponse(responseCode = "500", description = "파일 업로드중 에러가 발생하였습니다."),
		@ApiResponse(responseCode = "200", description = "프로필 사진이 등록되었습니다.")
	})
	@PostMapping("/profile")
	public ApiResult<RegisterMemberProfileResult> changeProfile(@RequestBody CommandRegisterMemberProfile request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("프로필 사진이 등록되었습니다.", memberCommandServiceV2.registerProfile(request, member.getMemberId()));
	}
}