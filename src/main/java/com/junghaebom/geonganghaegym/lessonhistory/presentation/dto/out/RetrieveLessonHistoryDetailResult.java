package com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.junghaebom.geonganghaegym.common.LessonTimeFormatter;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonAttendanceStatus;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistory;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryComment;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryFiles;
import com.junghaebom.geonganghaegym.member.domain.Member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수업 일지 상세 조회 응답 DTO")
public record RetrieveLessonHistoryDetailResult(
	Long id,
	String title,
	String content,
	List<LessonHistoryCommentCommandResult> comments,
	Integer commentTotalCount,
	LocalDateTime createdAt,
	String student,
	String trainer,
	Long scheduleId,
	String lessonDt,
	String lessonTime,
	String attendanceStatus,
	List<LessonHistoryFileResults> files
) {
	public static RetrieveLessonHistoryDetailResult detailFrom(LessonHistory entity) {
		if (entity == null) {
			return null;
		}
		String trainerName = entity.getTrainer() != null && entity.getTrainer().getName() != null
			? entity.getTrainer().getName() + " 트레이너" : null;

		return new RetrieveLessonHistoryDetailResult(
			entity.getId(),
			entity.getTitle(),
			entity.getContent(),
			sortLessonHistoryComment(entity.getLessonHistoryComment()),
			(int) entity.getLessonHistoryComment().stream().filter(c -> !c.isDelYn()).count(),
			entity.getCreatedAt(),
			entity.getStudent() != null ? entity.getStudent().getName() : null,
			trainerName,
			entity.getSchedule() != null ? entity.getSchedule().getId() : null,
			LessonTimeFormatter.formatLessonDt(
				entity.getSchedule() != null ? entity.getSchedule().getLessonDt() : null),
			LessonTimeFormatter.formatLessonTime(
				entity.getSchedule() != null ? entity.getSchedule().getLessonStartTime() : null,
				entity.getSchedule() != null ? entity.getSchedule().getLessonEndTime() : null),
			validateAttendanceStatus(
				entity.getSchedule() != null ? entity.getSchedule().getLessonDt() : null,
				entity.getSchedule() != null ? entity.getSchedule().getLessonEndTime() : null),
			entity.getFiles().stream()
				.filter(f -> f.getLessonHistoryComment() == null)
				.map(LessonHistoryFileResults::from)
				.sorted(Comparator.comparing(LessonHistoryFileResults::createdAt))
				.collect(Collectors.toList())
		);
	}

	private static List<LessonHistoryCommentCommandResult> sortLessonHistoryComment(
		List<LessonHistoryComment> comments) {
		if (comments == null) {
			return new ArrayList<>();
		}

		List<LessonHistoryComment> sorted = comments.stream()
			.sorted(Comparator.comparingInt(LessonHistoryComment::getOrder))
			.collect(Collectors.toList());

		List<LessonHistoryComment> parentComments = sorted.stream()
			.filter(c -> c.getParent() == null)
			.collect(Collectors.toList());

		List<LessonHistoryComment> childComments = sorted.stream()
			.filter(c -> c.getParent() != null)
			.collect(Collectors.toList());

		for (LessonHistoryComment parentComment : parentComments) {
			List<LessonHistoryComment> replies = childComments.stream()
				.filter(child -> child.getParent().getId().equals(parentComment.getId()))
				.sorted(Comparator.comparingInt(LessonHistoryComment::getOrder))
				.collect(Collectors.toList());
			parentComment.setReplies(replies);
		}

		return parentComments.stream()
			.map(LessonHistoryCommentCommandResult::from)
			.collect(Collectors.toList());
	}

	private static String validateAttendanceStatus(LocalDate lessonDt, LocalTime lessonEndTime) {
		LocalDateTime lesson = LocalDateTime.of(lessonDt, lessonEndTime);
		if (LocalDateTime.now().isAfter(lesson)) {
			return LessonAttendanceStatus.ATTENDED.getDescription();
		}
		return LessonAttendanceStatus.ABSENT.getDescription();
	}

	public record LessonHistoryCommentCommandResult(
		Long id,
		String content,
		LessonHistoryCommentMemberResult member,
		Integer orderNum,
		Long parentId,
		List<LessonHistoryCommentCommandResult> replies,
		List<LessonHistoryFileResults> files,
		Boolean delYn,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
		public static LessonHistoryCommentCommandResult from(LessonHistoryComment entity) {
			if (entity == null) {
				return null;
			}
			List<LessonHistoryCommentCommandResult> repliesList = entity.getReplies() != null
				? entity.getReplies().stream().map(LessonHistoryCommentCommandResult::from).collect(Collectors.toList())
				: new ArrayList<>();

			List<LessonHistoryFileResults> filesList = entity.getFiles() != null
				? entity.getFiles().stream().map(LessonHistoryFileResults::from).collect(Collectors.toList())
				: new ArrayList<>();

			return new LessonHistoryCommentCommandResult(
				entity.getId(),
				entity.isDelYn() ? "삭제된 댓글입니다." : entity.getContent(),
				LessonHistoryCommentMemberResult.from(entity.getWriter()),
				entity.getOrder(),
				entity.getParent() != null ? entity.getParent().getId() : null,
				repliesList,
				filesList,
				entity.isDelYn(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
			);
		}
	}

	public record LessonHistoryCommentMemberResult(
		Long memberId,
		String name,
		String fileUrl
	) {
		public static LessonHistoryCommentMemberResult from(Member entity) {
			if (entity == null) {
				return null;
			}
			return new LessonHistoryCommentMemberResult(
				entity.getId(),
				entity.getName(),
				entity.getMemberProfile() != null ? entity.getMemberProfile().getFileUrl() : null
			);
		}
	}

	public record LessonHistoryFileResults(
		String fileUrl,
		Integer fileOrder,
		LocalDateTime createdAt
	) {
		public static LessonHistoryFileResults from(LessonHistoryFiles entity) {
			if (entity == null) {
				return null;
			}
			return new LessonHistoryFileResults(
				entity.getFileUrl(),
				entity.getFileOrder(),
				entity.getCreatedAt()
			);
		}
	}
}
