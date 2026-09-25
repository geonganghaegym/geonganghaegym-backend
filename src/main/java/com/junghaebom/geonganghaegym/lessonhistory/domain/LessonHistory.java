package com.junghaebom.geonganghaegym.lessonhistory.domain;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class LessonHistory extends BaseTimeEntity<LessonHistory, Long> {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "lesson_history_id")
	private Long id;

	private String title;

	private String content;

	@OneToMany(fetch = LAZY, mappedBy = "lessonHistory", cascade = ALL)
	@ToString.Exclude
	private List<LessonHistoryComment> lessonHistoryComment = new ArrayList<>();

	@OneToMany(fetch = LAZY, mappedBy = "lessonHistory", cascade = ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<LessonHistoryFiles> files = new ArrayList<>();

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "trainer_id")
	@ToString.Exclude
	private Member trainer;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "student_id")
	@ToString.Exclude
	private Member student;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "schedule_id")
	@ToString.Exclude
	private Schedule schedule;

	@Enumerated(STRING)
	private LessonHistoryReadStatus feedbackChecked = LessonHistoryReadStatus.UNREAD;

	private LessonHistory(String title, String content, Member trainer, Member student, Schedule schedule) {
		this.title = title;
		this.content = content;
		this.trainer = trainer;
		this.student = student;
		this.schedule = schedule;
	}

	public static LessonHistory register(String title, String content, Member student, Member trainer,
		Schedule schedule) {
		return new LessonHistory(title, content, trainer, student, schedule);
	}

	public void updateLessonHistory(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public void updateFeedbackChecked() {
		this.feedbackChecked = LessonHistoryReadStatus.READ;
	}
}
