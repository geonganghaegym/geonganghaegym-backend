package com.junghaebom.geonganghaegym.schedule.application;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.StudentScheduleCond;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.*;
import com.junghaebom.geonganghaegym.schedule.repository.student.StudentScheduleRepository;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

class StudentScheduleServiceTest {
	@Test
	void noonBelongsToAfternoonAndEveryScheduleAppearsOnce() {
		var repository = mock(StudentScheduleRepository.class);
		var mappings = mock(TrainerMemberMappingRepository.class);
		var member = Member.builder().id(1L).build();
		var trainer = Member.builder().id(2L).build();
		var mapping = mock(TrainerMemberMapping.class);
		when(mapping.getTrainer()).thenReturn(trainer);
		when(mappings.findTop1ByMemberIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(mapping));
		var cond = new StudentScheduleCond(null, null, null, null);
		var morning = schedule(1L, LocalTime.of(11, 59));
		var noon = schedule(2L, LocalTime.NOON);
		var afternoon = schedule(3L, LocalTime.of(12, 1));
		when(repository.findAllSchedule(cond, 2L, member)).thenReturn(List.of(morning, noon, afternoon));
		var result = new StudentScheduleService(repository, mappings, mock(CourseService.class))
			.findAllScheduleOfTrainer(cond, member);
		assertEquals(List.of(morning), result.morning());
		assertEquals(List.of(noon, afternoon), result.afternoon());
	}

	private ScheduleCommandResult schedule(Long id, LocalTime time) {
		return new ScheduleCommandResult(id, LocalDate.now().plusDays(7), time, time.plusMinutes(30),
			null, "trainer", null, null, null);
	}
}
