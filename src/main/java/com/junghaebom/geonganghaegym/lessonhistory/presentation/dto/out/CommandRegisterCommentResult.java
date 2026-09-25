package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out;

import java.util.List;
import java.util.stream.Collectors;

import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryComment;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryFiles;

public record CommandRegisterCommentResult(
	Long lessonHistoryId,
	Long lessonHistoryCommentId,
	Long writerId,
	String writerName,
	String content,
	List<CommandUploadFileResult> files
) {
	public static CommandRegisterCommentResult from(LessonHistoryComment lessonHistoryComment,
		List<LessonHistoryFiles> files) {
		return new CommandRegisterCommentResult(
			lessonHistoryComment.getLessonHistory() != null ? lessonHistoryComment.getLessonHistory().getId() : null,
			lessonHistoryComment.getId(),
			lessonHistoryComment.getWriter() != null ? lessonHistoryComment.getWriter().getId() : null,
			lessonHistoryComment.getWriter() != null ? lessonHistoryComment.getWriter().getName() : null,
			lessonHistoryComment.getContent(),
			files.stream().map(CommandUploadFileResult::from).collect(Collectors.toList())
		);
	}
}
