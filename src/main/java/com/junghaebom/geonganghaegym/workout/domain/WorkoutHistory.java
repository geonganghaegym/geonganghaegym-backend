package com.junghaebom.geonganghaegym.workout.domain;

import static jakarta.persistence.FetchType.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.gym.domain.Gym;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "workout_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@DynamicUpdate
@ToString
public class WorkoutHistory extends BaseTimeEntity<WorkoutHistory, Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "workout_history_id")
	private Long workoutHistoryId;

	private String content;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	@ToString.Exclude
	private Member member;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "gym_id")
	@ToString.Exclude
	private Gym gym;

	@ColumnDefault("false")
	@Builder.Default
	private Boolean delYn = false;

	@ColumnDefault("0")
	@Builder.Default
	private Long likeCnt = 0L;

	@ColumnDefault("0")
	@Builder.Default
	private Long commentCnt = 0L;

	@ColumnDefault("false")
	@Builder.Default
	private Boolean viewMySelf = false;

	@Builder.Default
	@OneToMany(fetch = LAZY, mappedBy = "workoutHistory", cascade = CascadeType.ALL)
	@ToString.Exclude
	private List<WorkoutHistoryFiles> historyFiles = new ArrayList<>();

	@Builder.Default
	@OneToMany(fetch = LAZY, mappedBy = "workoutHistory", cascade = CascadeType.ALL)
	@ToString.Exclude
	private List<WorkoutHistoryComment> historyComments = new ArrayList<>();

	@Builder.Default
	@OneToMany(fetch = LAZY, mappedBy = "workoutHistory", cascade = CascadeType.ALL)
	@ToString.Exclude
	private List<CompletedExercise> completedExercises = new ArrayList<>();

	public static WorkoutHistory create(String content, boolean viewMySelf, Member member, Gym gym) {
		return WorkoutHistory.builder()
			.content(content)
			.viewMySelf(viewMySelf)
			.member(member)
			.gym(gym)
			.build();
	}

	public List<WorkoutHistoryFiles> getHistoryFiles() {
		return historyFiles.stream().filter(f -> !f.getDelYn()).toList();
	}

	public void changeContent(String content) {
		this.content = content;
	}

	public void changeViewMySelf(boolean viewMySelf) {
		this.viewMySelf = viewMySelf;
	}

	public void changeLikeCnt(Long likeCnt) {
		this.likeCnt = likeCnt;
	}

	public void changeCommentCnt(Long commentCnt) {
		this.commentCnt = commentCnt;
	}

	public void deleteWorkoutHistory() {
		this.delYn = true;
		this.deleteFiles();
		this.deleteComments();
	}

	public void deleteComments() {
		this.historyComments.forEach(WorkoutHistoryComment::deleteComment);
	}

	public void deleteFiles() {
		this.historyFiles.forEach(WorkoutHistoryFiles::deleteWorkoutHistoryFile);
	}

}
