package com.junghaebom.geonganghaegym.home.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.home.application.HomeService;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.StudentHomeResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.TrainerHomeResult;
import com.junghaebom.geonganghaegym.point.application.PointService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/home")
@Tag(name = "10. 홈 API", description = "홈 API")
@Slf4j
public class HomeController {

	private final HomeService homeService;
	private final PointService pointService;

	@Operation(summary = "학생 홈 조회")
	@GetMapping("/student")
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	public ApiResult<StudentHomeResult> getStudentHome(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ApiResult.success("학생 홈이 조회되었습니다.", homeService.getStudentHome(customMemberDetails.getMemberId()));
	}

	@Operation(summary = "트레이너 홈 조회")
	@GetMapping("/trainer")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<TrainerHomeResult> getTrainerHome(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ApiResult.success("트레이너 홈이 조회되었습니다.", homeService.getTrainerHome(customMemberDetails.getMemberId()));
	}
}
