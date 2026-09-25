package com.junghaebom.geonganghaegym.lessonhistory.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;

import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class LessonHistoryFiles extends BaseTimeEntity<LessonHistoryFiles, Long> {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "lesson_history_files_id")
	private Long id;

	private String fileUrl;

	private int fileOrder;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	@ToString.Exclude
	private Member member;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "lesson_history_id")
	@ToString.Exclude
	private LessonHistory lessonHistory;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "lesson_history_comment_id")
	@ToString.Exclude
	private LessonHistoryComment lessonHistoryComment;

	public LessonHistoryFiles(String fileUrl, int fileOrder, Member member, LessonHistory lessonHistory) {
		this.fileUrl = fileUrl;
		this.fileOrder = fileOrder;
		this.member = member;
		this.lessonHistory = lessonHistory;
	}

	public LessonHistoryFiles(String fileUrl, int fileOrder, Member member, LessonHistory lessonHistory,
		LessonHistoryComment lessonHistoryComment) {
		this.fileUrl = fileUrl;
		this.fileOrder = fileOrder;
		this.member = member;
		this.lessonHistory = lessonHistory;
		this.lessonHistoryComment = lessonHistoryComment;
	}

	public static LessonHistoryFiles create(Member member, String fileUrl, int fileOrder) {
		return new LessonHistoryFiles(fileUrl, fileOrder, member, null);
	}

	public void updateFileOrder(int fileOrder) {
		this.fileOrder = fileOrder;
	}
}
