package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in;

import java.util.List;

import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUploadFileResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수업 일지 댓글 수정 DTO")
public record CommandUpdateComment(

	@Schema(description = "수정할 댓글 내용", example = "트레이너님 휴대폰 그만봐", required = true)
	String content,

	@Schema(description = "등록할 파일", required = false)
	List<CommandUploadFileResult> uploadFiles
) {
}
