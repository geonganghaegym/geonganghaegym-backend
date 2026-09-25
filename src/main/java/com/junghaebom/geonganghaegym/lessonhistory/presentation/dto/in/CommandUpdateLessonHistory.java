package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in;

import java.util.List;

import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUploadFileResult;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommandUpdateLessonHistory(

	@Schema(description = "수정할 수업일지 제목")
	String title,

	@Schema(description = "수정할 수업일지 내용")
	String content,

	@Schema(description = "등록할 파일", required = false)
	List<CommandUploadFileResult> uploadFiles
) {
}
