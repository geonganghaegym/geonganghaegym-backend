package com.junghaebom.geonganghaegym.schedule.presentation;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.schedule.application.TrainerScheduleService;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.RetrieveTrainerScheduleByLessonInfo;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerDefaultLessonTimeResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonInfoResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Tag(name = "03-01.수업 API", description = "수업 일정 API")
public class TrainerScheduleController {

	private final TrainerScheduleService trainerScheduleService;

	@Operation(summary = "트레이너가 기본 수업 시간을 조회한다.")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	@GetMapping("/default-lesson-time")
	public ApiResult<RetrieveTrainerDefaultLessonTimeResult> findOneDefaultLessonTime(
		@AuthenticationPrincipal CustomMemberDetails member
	) {
		return ApiResult.success(
			"기본 수업 시간 조회에 성공하였습니다.",
			trainerScheduleService.findOneDefaultLessonTime(member.getMemberId())
		);
	}

	@Operation(summary = "트레이너가 전체 일정을 조회한다.", description = "트레이너가 전체 일정을 조회한다. 특정 일자나 기간으로 조회하고 싶으면 DTO를 활용한다.")
	@GetMapping("/all")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<RetrieveTrainerScheduleByLessonInfoResult> findAllSchedule(
		@ParameterObject RetrieveTrainerScheduleByLessonInfo retrieveTrainerScheduleByLessonInfo,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails
	) {
		return ApiResult.success(
			"전체 일정을 조회했습니다.",
			trainerScheduleService.findAllSchedule(
				retrieveTrainerScheduleByLessonInfo,
				customMemberDetails.getMemberId()
			)
		);
	}

}
