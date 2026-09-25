package com.junghaebom.geonganghaegym.course.domain;

import static jakarta.persistence.FetchType.*;

import java.util.ArrayList;
import java.util.List;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.point.domain.Calculation;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Course extends BaseTimeEntity<Course, Long> {

	@OneToMany(fetch = LAZY, mappedBy = "course", cascade = CascadeType.ALL)
	@ToString.Exclude
	private final List<CourseHistory> courseHistories = new ArrayList<>();
	@OneToMany(fetch = LAZY, mappedBy = "course", cascade = CascadeType.PERSIST)
	@ToString.Exclude
	private final List<Schedule> schedules = new ArrayList<>();
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "course_id")
	private Long courseId;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id")
	@ToString.Exclude
	private Member member;
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "trainer_id")
	@ToString.Exclude
	private Member trainer;
	private int totalLessonCnt;
	private int remainLessonCnt;

	@Builder
	public Course(Member member, Member trainer, int totalLessonCnt, int remainLessonCnt) {
		this.member = member;
		this.trainer = trainer;
		this.totalLessonCnt = totalLessonCnt;
		this.remainLessonCnt = remainLessonCnt;
	}

	public static Course create(Member member, Member trainer, int totalLessonCnt, int remainLessonCnt) {
		return Course.builder()
			.member(member)
			.trainer(trainer)
			.totalLessonCnt(totalLessonCnt)
			.remainLessonCnt(remainLessonCnt)
			.build();
	}

	public void updateTotalLessonCnt(Calculation calculation, int updateCnt) {
		this.totalLessonCnt = calculation.apply(totalLessonCnt, updateCnt);
	}

	public void updateRemainLessonCnt(Calculation calculation, int updateCnt) {
		this.remainLessonCnt = calculation.apply(remainLessonCnt, updateCnt);
	}

	public void deleteSchedule() {
		this.schedules.forEach(Schedule::deleteCourse);
	}
}
