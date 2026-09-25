package com.junghaebom.geonganghaegym.lessonhistory.domain;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class LessonHistoryComment extends BaseTimeEntity<LessonHistoryComment, Long> {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "lesson_history_comment_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "parent_id")
	@ToString.Exclude
	private LessonHistoryComment parent;

	@Column(name = "\"order\"")
	private int order;

	private String content;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "writer_id")
	@ToString.Exclude
	private Member writer;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "lesson_history_id")
	@ToString.Exclude
	private LessonHistory lessonHistory;

	@OneToMany(fetch = LAZY, mappedBy = "lessonHistoryComment", cascade = ALL)
	@ToString.Exclude
	private List<LessonHistoryFiles> files = new ArrayList<>();

	@ColumnDefault("false")
	private boolean delYn = false;

	@Transient
	private List<LessonHistoryComment> replies = new ArrayList<>();

	public LessonHistoryComment(int order, String content, Member writer, LessonHistory lessonHistory) {
		this.order = order;
		this.content = content;
		this.writer = writer;
		this.lessonHistory = lessonHistory;
	}

	public LessonHistoryComment(int order, String content, Member writer, LessonHistory lessonHistory,
		LessonHistoryComment parent) {
		this.order = order;
		this.content = content;
		this.writer = writer;
		this.lessonHistory = lessonHistory;
		this.parent = parent;
	}

	public void updateLessonHistoryComment(String content) {
		this.content = content;
	}

	public void deleteComment() {
		this.delYn = true;
	}

	public void setReplies(List<LessonHistoryComment> replies) {
		this.replies = replies;
	}
}
