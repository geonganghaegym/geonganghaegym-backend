package com.junghaebom.geonganghaegym.workout.presentation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.workout.application.WorkoutHistoryService;
import com.junghaebom.geonganghaegym.workout.presentation.dto.out.WorkoutHistoryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community")
@Tag(name = "06-04. 커뮤니티 API", description = "커뮤니티 API")
@Slf4j
public class CommunityController {

	private final WorkoutHistoryService workoutService;

	@Operation(summary = "커뮤니티 운동기록 목록 조회하기")
	@GetMapping
	public ApiResult<CustomPaging<WorkoutHistoryDto>> getWorkoutHistoryOnCommunity(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "조회할 날짜", example = "2024-12") @Param("searchDate") String searchDate,
		@Parameter(description = "학생 ID") Long memberId,
		Pageable pageable) {
		return ApiResult.success("운동기록이 조회되었습니다.", workoutService.getWorkoutHistoryOnCommunity(memberId, customMemberDetails.getMember(), pageable, searchDate));
	}

}
