package com.junghaebom.geonganghaegym.schedule.repository.student;

import java.util.List;

import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.StudentScheduleCond;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.MyReservation;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleCommandResult;

public interface StudentScheduleRepositoryCustom {
	List<ScheduleCommandResult> findAllSchedule(StudentScheduleCond searchCond, Long trainerId, Member member);

	List<MyReservation> findNewReservation(Long memberId, StudentScheduleCond searchCond);

	List<MyReservation> findOldReservation(Long memberId, String searchDate);

	MyReservation findMyNextReservation(Long memberId);

	List<String> findMyReservationBlueDot(Long memberId, StudentScheduleCond searchCond);

}
