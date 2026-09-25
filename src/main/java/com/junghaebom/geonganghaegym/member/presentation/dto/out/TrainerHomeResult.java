package com.junghaebom.geonganghaegym.member.presentation.dto.out;

import java.util.List;

import com.junghaebom.geonganghaegym.gym.presentation.dto.out.GymDto;
import com.junghaebom.geonganghaegym.member.repository.dto.MemberInTeamResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonDtResult;

public record TrainerHomeResult(
	Long studentCount,
	List<MemberInTeamResult> bestStudents,
	RetrieveTrainerScheduleByLessonDtResult todaySchedule,
	GymDto gym,
	Boolean redDotStatus
) {
}
