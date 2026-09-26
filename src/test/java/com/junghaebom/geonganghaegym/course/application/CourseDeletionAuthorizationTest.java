package com.junghaebom.geonganghaegym.course.application;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.course.domain.Course;
import com.junghaebom.geonganghaegym.course.repository.*;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.schedule.application.CommonScheduleService;
import com.junghaebom.geonganghaegym.schedule.repository.common.CommonScheduleRepository;
import com.junghaebom.geonganghaegym.schedule.repository.student.StudentScheduleRepository;
import com.junghaebom.geonganghaegym.schedule.repository.waiting.ScheduleWaitingRepository;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

@ExtendWith(MockitoExtension.class)
class CourseDeletionAuthorizationTest {
	@Mock MemberRepository members;
	@Mock CourseRepository courses;
	@Mock CourseHistoryRepository histories;
	@Mock TrainerMemberMappingRepository mappings;
	@Mock CommonScheduleRepository schedules;
	@Mock StudentScheduleRepository reservations;
	@Mock CommonScheduleService cancellations;
	@Mock ScheduleWaitingRepository waiting;
	@InjectMocks CourseService service;

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void foreignCourseCannotChangeReservationsOrWaiting(boolean force) {
		Course course = Course.create(Member.builder().id(3L).build(), Member.builder().id(2L).build(), 10, 10);
		when(courses.findById(7L)).thenReturn(Optional.of(course));
		assertThrows(CustomException.class, () -> {
			if (force) service.deleteCourseAndCancelReservation(1L, 7L);
			else service.deleteCourseByTrainer(1L, 7L);
		});
		verifyNoInteractions(schedules, reservations, cancellations, waiting, members, histories);
		verify(courses, never()).deleteByCourseIdAndTrainerId(any(), any());
	}
}
