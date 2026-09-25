package com.junghaebom.geonganghaegym.lessonhistory.presentation;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.lessonhistory.application.LessonHistoryCommandService;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.CommandRegisterComment;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.CommandRegisterLessonHistory;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.CommandUpdateComment;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.CommandUpdateLessonHistory;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandRegisterCommentResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandRegisterLessonHistoryResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandRegisterReplyResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUpdateCommentResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUpdateLessonHistoryResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUploadFileResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lessonhistory")
@Tag(name = "07. 수업 일지")
@RequiredArgsConstructor
public class LessonHistoryCommandController {

	private final LessonHistoryCommandService lessonHistoryCommandService;

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	@Operation(summary = "수업 일지를 등록한다.")
	public ApiResult<CommandRegisterLessonHistoryResult> registerLessonHistory(
		@RequestBody @Valid CommandRegisterLessonHistory request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"수업 일지를 등록하였습니다.",
			lessonHistoryCommandService.registerLessonHistory(request, member.getMemberId())
		);
	}

	@Operation(summary = "게시글/댓글 작성 전에 파일을 첨부한다.")
	@PostMapping("/file")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<List<CommandUploadFileResult>> registerFilesOfLessonHistory(
		List<MultipartFile> uploadFiles,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"파일을 등록하였습니다.",
			lessonHistoryCommandService.registerFilesOfLessonHistory(uploadFiles, member.getMemberId())
		);
	}

	@Operation(summary = "수업일지를 수정한다.")
	@PatchMapping("/{lessonHistoryId}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<CommandUpdateLessonHistoryResult> updateLessonHistory(
		@PathVariable Long lessonHistoryId,
		@RequestBody @Valid CommandUpdateLessonHistory request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"수업 일지가 수정되었습니다.",
			lessonHistoryCommandService.updateLessonHistory(lessonHistoryId, request, member.getMemberId())
		);
	}

	@Operation(summary = "수업일지를 삭제한다.")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	@DeleteMapping("/{lessonHistoryId}")
	public ApiResult<Long> deleteLessonHistory(
		@PathVariable Long lessonHistoryId,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"수업 일지가 삭제되었습니다.",
			lessonHistoryCommandService.deleteLessonHistory(lessonHistoryId, member.getMemberId())
		);
	}

	@Operation(summary = "수업일지에 댓글을 등록한다.")
	@PostMapping("/{lessonHistoryId}/comment")
	public ApiResult<CommandRegisterCommentResult> registerLessonHistoryComment(
		@PathVariable Long lessonHistoryId,
		@RequestBody @Valid CommandRegisterComment request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"댓글이 등록되었습니다.",
			lessonHistoryCommandService.registerLessonHistoryComment(lessonHistoryId, request, member)
		);
	}

	@Operation(summary = "수업일지 댓글에 대댓글을 등록한다.")
	@PostMapping("/{lessonHistoryId}/comment/{lessonHistoryCommentId}")
	public ApiResult<CommandRegisterReplyResult> registerLessonHistoryReply(
		@PathVariable Long lessonHistoryId,
		@PathVariable Long lessonHistoryCommentId,
		@RequestBody @Valid CommandRegisterComment request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"대댓글이 등록되었습니다.",
			lessonHistoryCommandService.registerLessonHistoryReply(
				lessonHistoryId, lessonHistoryCommentId, request, member)
		);
	}

	@Operation(summary = "수업일지에 댓글/답글을 수정한다.")
	@PatchMapping("/comment/{lessonHistoryCommentId}")
	public ApiResult<CommandUpdateCommentResult> updateLessonHistoryComment(
		@PathVariable Long lessonHistoryCommentId,
		@RequestBody @Valid CommandUpdateComment request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"댓글이 수정되었습니다.",
			lessonHistoryCommandService.updateLessonHistoryComment(lessonHistoryCommentId, request, member)
		);
	}

	@Operation(summary = "수업일지에 댓글/답글을 삭제한다.")
	@DeleteMapping("/comment/{lessonHistoryCommentId}")
	public ApiResult<Long> deleteLessonHistoryComment(
		@PathVariable Long lessonHistoryCommentId,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"댓글 1개가 삭제되었습니다.",
			lessonHistoryCommandService.deleteLessonHistoryComment(lessonHistoryCommentId, member.getMemberId())
		);
	}
}
