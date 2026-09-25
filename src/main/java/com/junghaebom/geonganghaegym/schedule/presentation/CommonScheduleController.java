package com.junghaebom.geonganghaegym.schedule.presentation;

import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.schedule.application.CommonScheduleService;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleIdInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedule")
@Slf4j
@Valid
@Tag(name = "03-03.수업 공통 API", description = "수업 공통 API")
public class CommonScheduleController {

	private final CommonScheduleService commonScheduleService;

	@Operation(summary = "트레이너 또는 학생이 수업을 신청한다.", description = "트레이너 또는 학생이 수업을 신청한다.")
	@PostMapping("/{scheduleId}")
	public ApiResult<ScheduleIdInfo> reserveSchedule(@PathVariable Long scheduleId,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		ScheduleIdInfo result = commonScheduleService.reserveSchedule(scheduleId, customMemberDetails.getMemberId());
		return ApiResult.success(result.scheduleTime() + " 수업이 예약되었습니다.", result);
	}

	@Operation(summary = "트레이너 또는 학생이 수업을 취소한다.", description = "트레이너 또는 학생이 등록한 수업을 취소한다.")
	@DeleteMapping("/{scheduleId}")
	public ApiResult<ScheduleIdInfo> cancelScheduleForMember(@PathVariable Long scheduleId,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		ScheduleIdInfo result;
		if (TRAINER.equals(customMemberDetails.getMember().getMemberType())) {
			result = commonScheduleService.cancelMemberScheduleForce(scheduleId, customMemberDetails.getMemberId());
		} else {
			result = commonScheduleService.cancelMemberSchedule(scheduleId, customMemberDetails.getMemberId());
		}
		return ApiResult.success(result.scheduleTime() + " 수업이 취소되었습니다.", result);
	}
}
