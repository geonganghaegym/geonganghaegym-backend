package com.junghaebom.geonganghaegym.workout.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.junghaebom.geonganghaegym.member.domain.MemberProfile;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryComment;

public record WorkoutHistoryCommentDto(
	Long id,
	CommentMemberDto member,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	Long parentId,
	Long orderNum,
	boolean delYn,
	List<WorkoutHistoryCommentDto> replies
) {

	public WorkoutHistoryCommentDto withReplies(List<WorkoutHistoryCommentDto> replies) {
		return new WorkoutHistoryCommentDto(id, member, content, createdAt, updatedAt, parentId, orderNum, delYn, replies);
	}

	public static WorkoutHistoryCommentDto from(WorkoutHistoryComment comment) {
		return new WorkoutHistoryCommentDto(
			comment.getCommentId(),
			CommentMemberDto.from(comment.getMember()),
			comment.getDelYn() ? "삭제된 댓글입니다." : comment.getContent(),
			comment.getCreatedAt(),
			comment.getUpdatedAt(),
			comment.getParentCommentId(),
			comment.getOrderNum(),
			comment.getDelYn(),
			null
		);
	}

	public static WorkoutHistoryCommentDto create(WorkoutHistoryComment comment, MemberProfile memberProfile) {
		return new WorkoutHistoryCommentDto(
			comment.getCommentId(),
			CommentMemberDto.create(comment.getMember(), memberProfile),
			comment.getDelYn() ? "삭제된 댓글입니다." : comment.getContent(),
			comment.getCreatedAt(),
			comment.getUpdatedAt(),
			comment.getParentCommentId(),
			comment.getOrderNum(),
			comment.getDelYn(),
			null
		);
	}
}
