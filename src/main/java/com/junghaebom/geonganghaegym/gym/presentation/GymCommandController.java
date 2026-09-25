package com.junghaebom.geonganghaegym.gym.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.gym.application.GymCommandService;
import com.junghaebom.geonganghaegym.gym.presentation.dto.in.CommandRegisterGym;
import com.junghaebom.geonganghaegym.gym.presentation.dto.in.CommandSelectMyGym;
import com.junghaebom.geonganghaegym.gym.presentation.dto.out.CommandRegisterGymResult;
import com.junghaebom.geonganghaegym.gym.presentation.dto.out.CommandSelectMyGymResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/gyms")
@RequiredArgsConstructor
@Tag(name = "04-01.헬스장 API", description = "헬스장 조회 API")
public class GymCommandController {

	private final GymCommandService gymCommandService;

	@Operation(summary = "관리자 또는 트레이너가 헬스장을 등록한다.")
	@PostMapping
	public ApiResult<CommandRegisterGymResult> registerGym(
		@RequestBody CommandRegisterGym request) {
		return ApiResult.success("헬스장을 등록하였습니다.", gymCommandService.registerGym(request));
	}

	@Operation(summary = "학생 또는 트레이너가 내가 다니는 헬스장으로 등록한다.")
	@PostMapping("/{gymId}")
	public ApiResult<CommandSelectMyGymResult> selectMyGym(
		@PathVariable Long gymId,
		@RequestBody(required = false) CommandSelectMyGym request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("내 헬스장으로 등록되었습니다.", gymCommandService.selectMyGym(gymId, request, member.getMemberId()));
	}
}
