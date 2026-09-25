package com.junghaebom.geonganghaegym.workout.domain;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;

import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "exercise")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@ToString
public class Exercise {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "exercise_id")
	private Long exerciseId;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	@ToString.Exclude
	private Member member;

	private String names;

	@Enumerated(STRING)
	private ExerciseCategory category;

	private String primaryMuscle;
	private String secondaryMuscle;

	public static Exercise create(Member member, String names, ExerciseCategory category, String muscles) {
		return Exercise.builder()
			.member(member)
			.names(names)
			.category(category)
			.secondaryMuscle(muscles)
			.build();
	}
}
