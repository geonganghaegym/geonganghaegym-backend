package com.junghaebom.geonganghaegym.diet.domain;

import static jakarta.persistence.FetchType.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
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
@Table(name = "diet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@DynamicUpdate
@ToString
public class Diet extends BaseTimeEntity<Diet, Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "diet_id")
	private Long dietId;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	@ToString.Exclude
	private Member member;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "trainer_id")
	@ToString.Exclude
	private Member trainer;

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
	private Boolean fastBreakfast = false;

	@ColumnDefault("false")
	@Builder.Default
	private Boolean fastLunch = false;

	@ColumnDefault("false")
	@Builder.Default
	private Boolean fastDinner = false;

	private LocalDate eatDate;

	@OneToMany(fetch = LAZY, mappedBy = "diet", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	private List<DietFiles> dietFiles = new ArrayList<>();

	@Builder.Default
	@OneToMany(fetch = LAZY, mappedBy = "diet", cascade = CascadeType.ALL)
	@ToString.Exclude
	private List<DietComment> dietComments = new ArrayList<>();

	public static Diet create(Member member, Member trainer) {
		return Diet.builder()
			.member(member)
			.trainer(trainer)
			.build();
	}

	public static Diet create(Member member, Member trainer, boolean breakfastFast, boolean lunchFast,
		boolean dinnerFast, LocalDate eatDate) {
		return Diet.builder()
			.member(member)
			.trainer(trainer)
			.fastBreakfast(breakfastFast)
			.fastLunch(lunchFast)
			.fastDinner(dinnerFast)
			.eatDate(eatDate)
			.build();
	}

	public List<DietFiles> getDietFiles() {
		return dietFiles.stream().filter(f -> !f.getDelYn()).toList();
	}

	public void updateLikeCnt(Long likeCnt) {
		this.likeCnt = likeCnt;
	}

	public void changeFast(DietType type, boolean isFast) {
		switch (type) {
			case BREAKFAST -> this.changeFastBreakfast(isFast);
			case LUNCH -> this.changeFastLunch(isFast);
			case DINNER -> this.changeFastDinner(isFast);
		}
	}

	public void changeFast(boolean breakfastFast, boolean lunchFast, boolean dinnerFast) {
		this.changeFastBreakfast(breakfastFast);
		this.changeFastLunch(lunchFast);
		this.changeFastDinner(dinnerFast);
	}

	public void changeFastBreakfast(boolean isFast) {
		this.fastBreakfast = isFast;
	}

	public void changeFastLunch(boolean isFast) {
		this.fastLunch = isFast;
	}

	public void changeFastDinner(boolean isFast) {
		this.fastDinner = isFast;
	}

	public void deleteFile(DietType type) {
		if (dietFiles != null) {
			this.dietFiles.stream().filter(f -> type.equals(f.getType()))
				.forEach(DietFiles::deleteDietFile);
		}
	}

	public void deleteDiet() {
		this.delYn = true;
		this.deleteFiles();
		this.deleteComments();
	}

	public void deleteComments() {
		this.dietComments.forEach(DietComment::deleteComment);
	}

	public void deleteFiles() {
		this.dietFiles.forEach(DietFiles::deleteDietFile);
	}

	public void updateCommentCnt(Long commentCnt) {
		this.commentCnt = commentCnt;
	}

	public void changeEatDate(LocalDate eatDate) {
		this.eatDate = eatDate;
	}

	public void changeTrainer(Member trainer) {
		this.trainer = trainer;
	}
}
