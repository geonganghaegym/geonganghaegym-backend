package com.junghaebom.geonganghaegym.gym.presentation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.gym.application.GymService;
import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/gyms")
@RequiredArgsConstructor
@Tag(name = "04-01.헬스장 API", description = "헬스장 조회 API")
public class GymController {

	private final GymService gymService;

	@Operation(summary = "모든 헬스장을 조회한다.", description = "등록된 모든 헬스장을 조회한다.")
	@GetMapping
	public ApiResult<List<GymResult>> findAllGym() {
		return ApiResult.success("모든 헬스장을 조회하였습니다.", gymService.findAllGym());
	}
}
