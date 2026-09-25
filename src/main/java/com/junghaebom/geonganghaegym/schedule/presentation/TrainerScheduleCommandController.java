package com.junghaebom.geonganghaegym.schedule.presentation;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.schedule.application.TrainerScheduleCommandService;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.CommandRegisterDefaultLessonTime;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.CommandRegisterSchedule;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.CommandUpdateScheduleStatus;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.CommandCancelStudentReservationResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.CommandRegisterDefaultLessonTimeResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.CommandRegisterScheduleByStudentResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.CommandRegisterScheduleResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.CommandScheduleStatusResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleIdInfo;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@Tag(name = "03-01.수업 API", description = "수업 일정 API")
public class TrainerScheduleCommandController {

	private final TrainerScheduleCommandService trainerScheduleCommandService;

	@Operation(summary = "트레이너가 기본 수업 시간을 설정한다.")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	@PostMapping("/default-lesson-time")
	public ApiResult<CommandRegisterDefaultLessonTimeResult> registerDefaultSchedule(
		@RequestBody @Valid CommandRegisterDefaultLessonTime request,
		@AuthenticationPrincipal CustomMemberDetails member
	) {
		return ApiResult.success(
			"기본 수업 시간이 설정되었습니다.",
			trainerScheduleCommandService.registerDefaultLessonTime(request, member.getMemberId())
		);
	}

	@Operation(summary = "트레이너가 일정을 등록한다.")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	@PostMapping
	public ApiResult<CommandRegisterScheduleResult> registerSchedule(
		@RequestBody @Valid CommandRegisterSchedule request,
		@AuthenticationPrincipal CustomMemberDetails member
	) {
		return ApiResult.success(
			"일정 등록에 성공하였습니다.",
			trainerScheduleCommandService.registerSchedule(request, member.getMemberId())
		);
	}

	@Operation(summary = "트레이너가 특정 스케줄을 DISABLED/AVAILABLE로 변경한다.")
	@PostMapping("/trainer/{status}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<List<CommandScheduleStatusResult>> changeScheduleForTrainer(
		@PathVariable ReservationStatus status,
		@RequestBody @Valid CommandUpdateScheduleStatus request,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails
	) {
		return ApiResult.success(
			"해당 스케줄을 " + status + "로 변경하였습니다.",
			trainerScheduleCommandService.updateScheduleStatus(request, status, customMemberDetails.getMemberId())
		);
	}

	@Operation(summary = "트레이너가 학생을 수업에 등록한다.")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	@PostMapping("/{scheduleId}/{studentId}")
	public ApiResult<CommandRegisterScheduleByStudentResult> registerStudentInTrainerSchedule(
		@PathVariable Long scheduleId,
		@PathVariable Long studentId,
		@AuthenticationPrincipal CustomMemberDetails member
	) {
		return ApiResult.success(
			"학생을 수업에 등록하였습니다.",
			trainerScheduleCommandService.registerStudentInTrainerSchedule(
				scheduleId, studentId, member.getMemberId()
			)
		);
	}

	@Operation(summary = "트레이너가 학생이 신청한 수업을 취소한다.")
	@DeleteMapping("/trainer/{scheduleId}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<CommandCancelStudentReservationResult> cancelScheduleForTrainer(
		@PathVariable Long scheduleId,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails
	) {
		CommandCancelStudentReservationResult scheduleResult =
			trainerScheduleCommandService.cancelStudentReservation(scheduleId, customMemberDetails.getMemberId());
		return ApiResult.success(
			scheduleResult.lessonStartTime().format(DateTimeFormatter.ofPattern("a HH시 mm분")) + " 수업이 취소되었습니다.",
			scheduleResult
		);
	}

	@Operation(summary = "트레이너가 학생 노쇼 처리를 한다.")
	@DeleteMapping("/no-show/{scheduleId}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<ScheduleIdInfo> updateReservationStatusToNoShow(
		@PathVariable Long scheduleId,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails
	) {
		return ApiResult.success(
			"노쇼 처리되었습니다.",
			trainerScheduleCommandService.updateReservationStatusToNoShow(
				scheduleId, customMemberDetails.getMemberId()
			)
		);
	}

	@Operation(summary = "트레이너가 학생 노쇼 처리를 취소한다.")
	@PostMapping("/no-show/{scheduleId}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<ScheduleIdInfo> revertReservationStatusToNoShow(
		@PathVariable Long scheduleId,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails
	) {
		return ApiResult.success(
			"노쇼 처리가 취소되었습니다.",
			trainerScheduleCommandService.cancelReservationStatusToNoShow(
				scheduleId, customMemberDetails.getMemberId()
			)
		);
	}
}
