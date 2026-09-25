package com.junghaebom.geonganghaegym.schedule.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.schedule.application.ScheduleWaitingService;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.FindMyScheduleWaitingResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedule/waiting")
@Slf4j
@Valid
@Tag(name = "03-02. 수업 대기 API", description = "수업 대기 API")
public class ScheduleWaitingController {

	private final ScheduleWaitingService scheduleWaitingService;

	@Operation(summary = "학생이 수업 대기 신청을 한다.", description = "학생이 신청완료된 수업에 대기 신청을 한다.")
	@PostMapping("/{scheduleId}")
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	public ApiResult<Boolean> registerScheduleWaiting(@PathVariable Long scheduleId,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		String scheduleTime = scheduleWaitingService.registerScheduleWaiting(scheduleId, customMemberDetails.getMemberId());
		return ApiResult.success(scheduleTime + " 수업 대기가 예약되었습니다.", true);
	}

	@Operation(summary = "학생이 대기 신청을 취소한다.")
	@DeleteMapping("/{scheduleId}")
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	public ApiResult<Boolean> cancelScheduleWaiting(@PathVariable Long scheduleId,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		String scheduleTime = scheduleWaitingService.cancelScheduleWaiting(scheduleId, customMemberDetails.getMemberId());
		return ApiResult.success(scheduleTime + " 수업 대기가 취소되었습니다.", true);
	}

	@Operation(summary = "학생이 대기중인 예약을 조회한다.", description = "학생이 대기중인 예약을 조회한다.")
	@GetMapping("/my-waiting")
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	public ApiResult<FindMyScheduleWaitingResult> findAllMyScheduleWaiting(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ApiResult.success("학생이 대기중인 예약을 조회하였습니다.", scheduleWaitingService.findAllMyScheduleWaiting(customMemberDetails.getMemberId()));
	}
}
