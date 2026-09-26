package com.junghaebom.geonganghaegym.home.application;

import static com.junghaebom.geonganghaegym.member.domain.MemberType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.repository.CourseHistoryRepository;
import com.junghaebom.geonganghaegym.diet.application.DietService;
import com.junghaebom.geonganghaegym.lessonhistory.repository.LessonHistoryRepository;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.notification.application.NotificationService;
import com.junghaebom.geonganghaegym.point.repository.PointRepository;
import com.junghaebom.geonganghaegym.schedule.repository.TrainerScheduleRepository;
import com.junghaebom.geonganghaegym.schedule.repository.student.StudentScheduleRepository;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

class HomeServiceTest {

	@Test
	@DisplayName("트레이너 홈의 오늘의 수업은 오늘 날짜로 조회한다 (null이면 날짜 필터가 빠져 다른 날 수업이 섞인다)")
	void trainerHomeQueriesTodayScheduleWithTodayDate() {
		var memberRepository = mock(MemberRepository.class);
		var trainerScheduleRepository = mock(TrainerScheduleRepository.class);
		when(memberRepository.findByIdAndMemberTypeAndDelYnFalse(1L, TRAINER))
			.thenReturn(Optional.of(Member.builder().id(1L).build()));
		var service = new HomeService(mock(PointRepository.class), mock(StudentScheduleRepository.class),
			mock(LessonHistoryRepository.class), mock(DietService.class), mock(TrainerMemberMappingRepository.class),
			trainerScheduleRepository, memberRepository, mock(CourseService.class),
			mock(CourseHistoryRepository.class), mock(NotificationService.class));

		String before = LocalDate.now().toString();
		service.getTrainerHome(1L);
		String after = LocalDate.now().toString();

		var lessonDt = ArgumentCaptor.forClass(String.class);
		verify(trainerScheduleRepository).findOneTrainerTodaySchedule(lessonDt.capture(), eq(1L));
		assertTrue(List.of(before, after).contains(lessonDt.getValue()), "lessonDt=" + lessonDt.getValue());
	}
}
