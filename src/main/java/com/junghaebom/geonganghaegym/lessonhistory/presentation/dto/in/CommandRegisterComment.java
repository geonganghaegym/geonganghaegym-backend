package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in;

import java.util.List;

import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUploadFileResult;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 등록 DTO")
public record CommandRegisterComment(

	@Schema(description = "등록할 댓글 내용")
	@NotBlank(message = "내용을 입력해 주세요.")
	String content,

	@Schema(description = "등록할 파일", required = false)
	List<CommandUploadFileResult> uploadFiles
) {
}
