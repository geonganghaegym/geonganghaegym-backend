package com.junghaebom.geonganghaegym.diet.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.junghaebom.geonganghaegym.diet.domain.DietComment;
import com.junghaebom.geonganghaegym.member.domain.MemberProfile;
import com.junghaebom.geonganghaegym.workout.presentation.dto.CommentMemberDto;

public record DietCommentDto(
	Long id,
	CommentMemberDto member,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	Long parentId,
	Long orderNum,
	boolean delYn,
	List<DietCommentDto> replies
) {

	public static DietCommentDto from(DietComment comment) {
		return new DietCommentDto(
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

	public static DietCommentDto create(DietComment comment, MemberProfile memberProfile) {
		return new DietCommentDto(
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

	public DietCommentDto withReplies(List<DietCommentDto> replies) {
		return new DietCommentDto(id, member, content, createdAt, updatedAt, parentId, orderNum, delYn, replies);
	}

}
