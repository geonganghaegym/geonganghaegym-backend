package com.junghaebom.geonganghaegym.workout.presentation;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.workout.application.FileService;
import com.junghaebom.geonganghaegym.workout.application.WorkoutHistoryService;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.HistoryAddCommand;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.RegisterFile;
import com.junghaebom.geonganghaegym.workout.presentation.dto.out.WorkoutHistoryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workout-histories")
@Tag(name = "06-01. 운동기록 API", description = "운동기록 API")
@Slf4j
public class WorkoutHistoryController {

	private final WorkoutHistoryService workoutService;
	private final FileService fileService;

	@Operation(summary = "운동기록 첨부파일 등록")
	@PostMapping("/file")
	public ApiResult<List<RegisterFile>> addWorkoutHistoryFile(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Valid List<MultipartFile> uploadFiles) {
		return ApiResult.success("첨부파일이 등록되었습니다.", fileService.uploadFiles("workout-history", uploadFiles, customMemberDetails.getMember()));
	}

	@Operation(summary = "운동기록 등록")
	@PostMapping
	public ApiResult<WorkoutHistoryDto> addWorkoutHistory(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@RequestBody @Valid HistoryAddCommand request) {
		return ApiResult.success("운동기록이 등록되었습니다.", workoutService.addWorkoutHistory(customMemberDetails.getMember(), request));
	}

	@Operation(summary = "운동기록 상세 조회")
	@GetMapping("/{workoutHistoryId}")
	public ApiResult<WorkoutHistoryDto> getWorkoutHistoryDetail(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동기록 ID") @PathVariable Long workoutHistoryId) {
		return ApiResult.success("운동기록이 조회되었습니다.", workoutService.getWorkoutHistoryDetail(customMemberDetails.getMember(), workoutHistoryId));
	}

	@Operation(summary = "운동기록 삭제")
	@DeleteMapping("/{workoutHistoryId}")
	public ApiResult<Void> deleteWorkoutHistory(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동기록 ID") @PathVariable Long workoutHistoryId) {
		workoutService.deleteWorkoutHistory(customMemberDetails.getMember(), workoutHistoryId);
		return ApiResult.success("운동기록이 삭제되었습니다.");
	}

	@Operation(summary = "운동기록 수정")
	@PatchMapping("/{workoutHistoryId}")
	public ApiResult<WorkoutHistoryDto> updateWorkoutHistory(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동기록 ID") @PathVariable Long workoutHistoryId,
		@RequestBody @Valid HistoryAddCommand command) {
		return ApiResult.success("운동기록이 수정되었습니다.", workoutService.updateWorkoutHistory(customMemberDetails.getMember(), workoutHistoryId, command));
	}

	@Operation(summary = "운동기록 좋아요")
	@PostMapping("/{workoutHistoryId}/like")
	public ApiResult<Void> likeWorkoutHistory(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동기록 ID") @PathVariable Long workoutHistoryId) {
		workoutService.likeWorkoutHistory(customMemberDetails.getMember(), workoutHistoryId);
		return ApiResult.success("운동기록 좋아요에 성공하였습니다.");
	}

	@Operation(summary = "운동기록 좋아요 취소")
	@DeleteMapping("/{workoutHistoryId}/like")
	public ApiResult<Void> deleteLikeWorkoutHistory(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동기록 ID") @PathVariable Long workoutHistoryId) {
		workoutService.deleteLikeWorkoutHistory(customMemberDetails.getMember(), workoutHistoryId);
		return ApiResult.success("운동기록 좋아요가 취소되었습니다.");
	}

}
