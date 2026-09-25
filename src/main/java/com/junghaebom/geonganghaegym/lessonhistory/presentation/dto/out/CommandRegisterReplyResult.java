package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out;

import java.util.List;
import java.util.stream.Collectors;

import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryComment;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryFiles;

public record CommandRegisterReplyResult(
	Long lessonHistoryId,
	Long commentId,
	String content,
	List<CommandUploadFileResult> files,
	int order,
	boolean delYn,
	Long parentId
) {
	public static CommandRegisterReplyResult from(LessonHistoryComment lessonHistoryComment,
		List<LessonHistoryFiles> files) {
		return new CommandRegisterReplyResult(
			lessonHistoryComment.getLessonHistory() != null ? lessonHistoryComment.getLessonHistory().getId() : null,
			lessonHistoryComment.getId(),
			lessonHistoryComment.getContent(),
			files.stream().map(CommandUploadFileResult::from).collect(Collectors.toList()),
			lessonHistoryComment.getOrder(),
			lessonHistoryComment.isDelYn(),
			lessonHistoryComment.getParent() != null ? lessonHistoryComment.getParent().getId() : null
		);
	}
}
