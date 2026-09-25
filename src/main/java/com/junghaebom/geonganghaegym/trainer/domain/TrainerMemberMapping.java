package com.junghaebom.geonganghaegym.trainer.domain;

import static jakarta.persistence.FetchType.*;

import org.hibernate.annotations.ColumnDefault;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "trainer_member_mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class TrainerMemberMapping extends BaseTimeEntity<TrainerMemberMapping, Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mapping_id")
	private Long mappingId;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "trainer_id")
	@ToString.Exclude
	private Member trainer;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	@ToString.Exclude
	private Member member;

	@ColumnDefault("999")
	private int ranking = 999;

	@ColumnDefault("999")
	private int lastMonthRanking = 999;

	private String memo;

	@Builder
	public TrainerMemberMapping(Member trainer, Member member, int lessonCnt, int remainLessonCnt, String memo) {
		this.trainer = trainer;
		this.member = member;
		this.memo = memo;
	}

	public static TrainerMemberMapping create(Member trainer, Member member) {
		return TrainerMemberMapping.builder()
			.trainer(trainer)
			.member(member)
			.build();
	}

	public void changeRanking(int ranking) {
		this.ranking = ranking;
	}

	public void changeLastMonthRanking(int lastMonthRanking) {
		this.lastMonthRanking = lastMonthRanking;
	}

	public void changeMemo(String memo) {
		this.memo = memo;
	}
}
