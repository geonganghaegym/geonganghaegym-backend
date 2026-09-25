package com.junghaebom.geonganghaegym.schedule.domain;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import org.hibernate.annotations.DynamicUpdate;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@DynamicUpdate
@AllArgsConstructor
@Builder
public class ScheduleWaiting extends BaseTimeEntity<ScheduleWaiting, Long> {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "schedule_waiting_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "schedule_id")
	private Schedule schedule;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public static ScheduleWaiting register(Member member, Schedule schedule) {
		return ScheduleWaiting.builder()
			.schedule(schedule)
			.member(member)
			.build();
	}

}
