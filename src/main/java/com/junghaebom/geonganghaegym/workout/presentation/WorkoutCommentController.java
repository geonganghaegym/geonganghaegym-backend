package com.junghaebom.geonganghaegym.workout.presentation;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.workout.application.WorkoutCommentService;
import com.junghaebom.geonganghaegym.workout.presentation.dto.WorkoutHistoryCommentDto;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.HistoryCommentAddCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workout-histories")
@Tag(name = "06-02. 운동기록 댓글 API", description = "운동기록 댓글 API")
@Slf4j
public class WorkoutCommentController {

	private final WorkoutCommentService commentService;

	@Operation(summary = "운동기록의 댓글을 조회한다.")
	@GetMapping("/{workoutHistoryId}/comments")
	public ApiResult<CustomPaging<WorkoutHistoryCommentDto>> getCommentsByHistoryId(
		@PathVariable @Parameter(description = "운동기록 ID") Long workoutHistoryId,
		Pageable pageable) {
		return ApiResult.success("댓글이 조회되었습니다.", commentService.getCommentsByWorkoutHistoryId(workoutHistoryId, pageable));
	}

	@Operation(summary = "운동기록에 댓글(답글)을 등록한다")
	@PostMapping("/{workoutHistoryId}/comments")
	public ApiResult<Void> addComment(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동기록 ID") @PathVariable Long workoutHistoryId,
		@Valid @RequestBody HistoryCommentAddCommand command) {
		commentService.addComment(workoutHistoryId, command, customMemberDetails.getMember());
		return ApiResult.success("댓글이 등록되었습니다.");
	}

	@Operation(summary = "운동기록의 댓글을 수정한다.")
	@PatchMapping("/{workoutHistoryId}/comments/{commentId}")
	public ApiResult<WorkoutHistoryCommentDto> updateComment(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동기록 ID") @PathVariable Long workoutHistoryId,
		@Parameter(description = "운동기록의 댓글 ID") @PathVariable Long commentId,
		@Valid @RequestBody HistoryCommentAddCommand command) {
		return ApiResult.success("댓글이 수정되었습니다.", commentService.updateComment(customMemberDetails.getMember(), workoutHistoryId, commentId, command));
	}

	@Operation(summary = "운동기록의 댓글을 삭제한다.")
	@DeleteMapping("/{workoutHistoryId}/comments/{commentId}")
	public ApiResult<Void> deleteComment(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동기록 ID") @PathVariable Long workoutHistoryId,
		@Parameter(description = "운동기록의 댓글 ID") @PathVariable Long commentId) {
		commentService.deleteComment(customMemberDetails.getMember(), workoutHistoryId, commentId);
		return ApiResult.success("댓글이 삭제되었습니다.");
	}

}
