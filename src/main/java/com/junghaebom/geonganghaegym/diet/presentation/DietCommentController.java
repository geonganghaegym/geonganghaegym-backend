package com.junghaebom.geonganghaegym.diet.presentation;

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
import com.junghaebom.geonganghaegym.diet.application.DietCommentService;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietCommentDto;
import com.junghaebom.geonganghaegym.diet.presentation.dto.in.DietCommentAddCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diets")
@Tag(name = "09-00. 식단기록 댓글 API", description = "식단기록 댓글 API")
@Slf4j
public class DietCommentController {

	private final DietCommentService commentService;

	@Operation(summary = "식단기록의 댓글을 조회한다.")
	@GetMapping("/{dietId}/comments")
	public ApiResult<CustomPaging<DietCommentDto>> getCommentsByDietId(
		@Parameter(description = "식단기록 ID") @PathVariable Long dietId,
		Pageable pageable) {
		return ApiResult.success("댓글이 조회되었습니다.", commentService.getCommentsByDietId(dietId, pageable));
	}

	@Operation(summary = "식단기록에 댓글(답글)을 등록한다")
	@PostMapping("/{dietId}/comments")
	public ApiResult<Void> addComment(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "식단기록 ID") @PathVariable Long dietId,
		@Valid @RequestBody DietCommentAddCommand command) {
		commentService.addComment(dietId, command, customMemberDetails.getMember());
		return ApiResult.success("댓글이 등록되었습니다.");
	}

	@Operation(summary = "식단기록의 댓글을 수정한다.")
	@PatchMapping("/{dietId}/comments/{commentId}")
	public ApiResult<DietCommentDto> updateComment(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "식단기록 ID") @PathVariable Long dietId,
		@Parameter(description = "식단기록의 댓글 ID") @PathVariable Long commentId,
		@Valid @RequestBody DietCommentAddCommand command) {
		return ApiResult.success("댓글이 수정되었습니다.", commentService.updateComment(customMemberDetails.getMember(), dietId, commentId, command));
	}

	@Operation(summary = "식단기록의 댓글을 삭제한다.")
	@DeleteMapping("/{dietId}/comments/{commentId}")
	public ApiResult<Void> deleteComment(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "식단기록 ID") @PathVariable Long dietId,
		@Parameter(description = "식단기록의 댓글 ID") @PathVariable Long commentId) {
		commentService.deleteComment(customMemberDetails.getMember(), dietId, commentId);
		return ApiResult.success("댓글이 삭제되었습니다.");
	}

}
