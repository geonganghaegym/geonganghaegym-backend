package com.junghaebom.geonganghaegym.schedule.application;

import static com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus.*;
import static com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleCommandResult.SoldOutReason.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.StudentScheduleCond;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleCommandResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleCommandResult.SoldOutReason;
import com.junghaebom.geonganghaegym.schedule.repository.student.StudentScheduleRepository;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

/**
 * 회원 수업 예약 화면이 마감(SOLD_OUT) 슬롯을 사유별로 구분할 수 있게 사유를 함께 내려주는지 검증한다.
 * 판정은 현재 시각 기준이라, 시각이 날짜를 넘어가는 자정 직전에는 시간 의존 케이스를 건너뛴다.
 */
class StudentScheduleSoldOutReasonTest {

	private final StudentScheduleRepository repository = mock(StudentScheduleRepository.class);
	private final Member member = Member.builder().id(1L).build();
	private final StudentScheduleCond cond = new StudentScheduleCond(null, null, null, null);
	private StudentScheduleService service;

	@BeforeEach
	void setUp() {
		var mappings = mock(TrainerMemberMappingRepository.class);
		var mapping = mock(TrainerMemberMapping.class);
		when(mapping.getTrainer()).thenReturn(Member.builder().id(2L).build());
		when(mappings.findTop1ByMemberIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(mapping));
		service = new StudentScheduleService(repository, mappings, mock(CourseService.class));
	}

	@Test
	@DisplayName("지난 수업은 내 예약이어도 '지난 수업' 사유로 마감된다")
	void pastLesson() {
		assumeTrue(LocalTime.now().isAfter(LocalTime.of(2, 0)));
		var mine = at(1L, LocalDateTime.now().minusHours(1), SOLD_OUT, "나", null, MY_RESERVATION);

		assertThat(reasonOf(mine)).isEqualTo(PAST);
	}

	@Test
	@DisplayName("다가오는 내 예약은 '내 예약' 사유로 마감된다")
	void myReservation() {
		var mine = at(1L, LocalDateTime.now().plusDays(3), SOLD_OUT, "나", null, MY_RESERVATION);

		assertThat(reasonOf(mine)).isEqualTo(MY_RESERVATION);
	}

	@Test
	@DisplayName("빈 슬롯이라도 시작 30분 전부터는 '예약 마감' 사유로 마감된다")
	void reservationClosed() {
		assumeTrue(LocalTime.now().isBefore(LocalTime.of(23, 0)));
		var empty = at(1L, LocalDateTime.now().plusMinutes(20), AVAILABLE, null, null, null);

		assertThat(reasonOf(empty)).isEqualTo(RESERVATION_CLOSED);
	}

	@Test
	@DisplayName("다른 회원이 예약했고 대기자까지 있으면 '대기 마감' 사유로 마감된다")
	void waitingFull() {
		var taken = at(1L, LocalDateTime.now().plusDays(3), COMPLETED, "다른 회원", "대기 회원", null);

		assertThat(reasonOf(taken)).isEqualTo(WAITING_FULL);
	}

	@Test
	@DisplayName("다른 회원이 예약한 수업이 24시간 안이면 대기할 수 없어 '대기 불가' 사유로 마감된다")
	void waitingClosed() {
		assumeTrue(LocalTime.now().isBefore(LocalTime.of(21, 0)));
		var taken = at(1L, LocalDateTime.now().plusHours(2), COMPLETED, "다른 회원", null, null);

		assertThat(reasonOf(taken)).isEqualTo(WAITING_CLOSED);
	}

	@Test
	@DisplayName("예약·대기가 가능한 슬롯은 마감되지 않고 사유도 없다")
	void notSoldOut() {
		var empty = at(1L, LocalDateTime.now().plusDays(3), AVAILABLE, null, null, null);
		var taken = at(2L, LocalDateTime.now().plusDays(3), COMPLETED, "다른 회원", null, null);

		List<ScheduleCommandResult> result = findAll(empty, taken);

		assertThat(result).extracting(ScheduleCommandResult::reservationStatus).containsExactly(AVAILABLE, COMPLETED);
		assertThat(result).extracting(ScheduleCommandResult::soldOutReason).containsOnlyNulls();
	}

	private SoldOutReason reasonOf(ScheduleCommandResult schedule) {
		ScheduleCommandResult result = findAll(schedule).get(0);
		assertThat(result.reservationStatus()).isEqualTo(SOLD_OUT);
		return result.soldOutReason();
	}

	private List<ScheduleCommandResult> findAll(ScheduleCommandResult... schedules) {
		when(repository.findAllSchedule(cond, 2L, member)).thenReturn(List.of(schedules));
		var response = service.findAllScheduleOfTrainer(cond, member);
		List<ScheduleCommandResult> all = new ArrayList<>();
		all.addAll(response.morning() == null ? List.of() : response.morning());
		all.addAll(response.afternoon() == null ? List.of() : response.afternoon());
		return all;
	}

	private ScheduleCommandResult at(Long id, LocalDateTime start, ReservationStatus status, String applicant,
		String waitingBy, SoldOutReason reason) {
		return new ScheduleCommandResult(id, start.toLocalDate(), start.toLocalTime(),
			start.toLocalTime().plusHours(1), status, "trainer", applicant, waitingBy, reason);
	}
}
