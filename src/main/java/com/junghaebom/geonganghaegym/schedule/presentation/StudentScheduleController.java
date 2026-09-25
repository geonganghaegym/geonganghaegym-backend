package com.junghaebom.geonganghaegym.schedule.presentation;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.schedule.application.StudentScheduleService;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.StudentScheduleCond;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.MyReservationResponse;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ReservationDaysResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleCommandResponse;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleCommandResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedule/student")
@Slf4j
@Valid
@Tag(name = "03-02.수업 API", description = "수업 일정 API")
public class StudentScheduleController {

	private final StudentScheduleService studentScheduleService;

	@Operation(summary = "학생이 트레이너의 전체 일정을 조회한다.", description = "전체 일정을 조회한다. 특정 일자나 기간으로 조회하고 싶으면 DTO를 활용한다.")
	@GetMapping("/all")
	public ApiResult<ScheduleCommandResponse> findAllScheduleOfTrainer(
		@ParameterObject StudentScheduleCond searchCond,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ApiResult.success("전체 일정을 조회했습니다.", studentScheduleService.findAllScheduleOfTrainer(searchCond, customMemberDetails.getMember()));
	}

	@Operation(summary = "학생이 내 수업을 조회한다.", description = "회원이 등록된 수업 전체를 조회한다.")
	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	public ApiResult<List<ScheduleCommandResult>> findMySchedule(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ApiResult.success("내 수업을 조회하였습니다.", studentScheduleService.findAllByApplicantId(customMemberDetails.getMemberId()));
	}

	@Operation(summary = "학생이 다가오는 예약을 조회한다.", description = "학생이 다가오는 예약을 조회한다.")
	@GetMapping("/my-reservation/new")
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	public ApiResult<MyReservationResponse> findNewReservation(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@ParameterObject StudentScheduleCond searchCond) {
		return ApiResult.success("학생이 내 예약을 조회하였습니다.", studentScheduleService.findNewReservation(customMemberDetails.getMemberId(), searchCond));
	}

	@Operation(summary = "학생이 지난 예약을 조회한다.", description = "학생이 지난 예약을 조회한다.")
	@GetMapping("/my-reservation/old")
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	public ApiResult<MyReservationResponse> findOldReservation(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "조회할 날짜", example = "2024-12") @Param("searchDate") String searchDate) {
		return ApiResult.success("학생이 내 예약을 조회하였습니다.", studentScheduleService.findOldReservation(customMemberDetails.getMemberId(), searchDate));
	}

	@Operation(summary = "학생 예약한 날짜 블루닷 표시", description = "학생 예약한 날짜 블루닷 표시를 조회한다.")
	@GetMapping("/my-reservation")
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	public ApiResult<ReservationDaysResult> findMyReservationBlueDot(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@ParameterObject StudentScheduleCond searchCond) {
		return ApiResult.success("학생이 내 예약을 조회하였습니다.", studentScheduleService.findMyReservationBlueDot(customMemberDetails.getMemberId(), searchCond));
	}

}
