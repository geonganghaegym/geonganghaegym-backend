package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.repository.CourseHistoryRepository;
import com.junghaebom.geonganghaegym.course.repository.CourseRepository;
import com.junghaebom.geonganghaegym.diet.application.DietService;
import com.junghaebom.geonganghaegym.diet.repository.DietRepository;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.point.application.PointService;
import com.junghaebom.geonganghaegym.point.repository.PointRepository;
import com.junghaebom.geonganghaegym.trainer.application.MemberDataAccessValidator;
import com.junghaebom.geonganghaegym.workout.application.WorkoutHistoryService;
import com.junghaebom.geonganghaegym.workout.repository.workoutHistory.WorkoutHistoryRepository;

/**
 * 트레이너가 매핑되지 않은 회원의 데이터를 조회하면 데이터 조회 전에 거절되는지 검증한다(IDOR B3·B4).
 */
class MemberDataReadAuthorizationTest {
	static final Long FOREIGN_TRAINER_ID = 9L;
	static final Long MEMBER_ID = 5L;
	static final Pageable PAGE = PageRequest.of(0, 10);
	static final String SEARCH_DATE = "2026-09";

	static Member foreignTrainer() {
		return Member.builder().id(FOREIGN_TRAINER_ID).build();
	}

	static void rejectForeignTrainer(MemberDataAccessValidator validator) {
		doThrow(new CustomException(MEMBER_NOT_MAPPED)).when(validator).validateReadable(FOREIGN_TRAINER_ID, MEMBER_ID);
	}

	@Nested
	@ExtendWith(MockitoExtension.class)
	@DisplayName("운동 기록")
	class WorkoutHistory {
		@Mock MemberDataAccessValidator validator;
		@Mock MemberRepository members;
		@Mock WorkoutHistoryRepository histories;
		@InjectMocks WorkoutHistoryService service;

		@BeforeEach
		void setUp() {
			rejectForeignTrainer(validator);
		}

		@Test
		void foreignTrainerIsRejectedBeforeReading() {
			assertThatThrownBy(() -> service.getWorkoutHistory(foreignTrainer(), MEMBER_ID, PAGE, SEARCH_DATE))
				.isInstanceOf(CustomException.class);
			verifyNoInteractions(members, histories);
		}
	}

	@Nested
	@ExtendWith(MockitoExtension.class)
	@DisplayName("식단")
	class Diet {
		@Mock MemberDataAccessValidator validator;
		@Mock DietRepository diets;
		@InjectMocks DietService service;

		@BeforeEach
		void setUp() {
			rejectForeignTrainer(validator);
		}

		@Test
		void foreignTrainerIsRejectedBeforeReading() {
			assertThatThrownBy(() -> service.getDiet(FOREIGN_TRAINER_ID, MEMBER_ID, PAGE, SEARCH_DATE))
				.isInstanceOf(CustomException.class);
			verifyNoInteractions(diets);
		}
	}

	@Nested
	@ExtendWith(MockitoExtension.class)
	@DisplayName("수강권")
	class Course {
		@Mock MemberDataAccessValidator validator;
		@Mock MemberRepository members;
		@Mock CourseRepository courses;
		@Mock CourseHistoryRepository histories;
		@InjectMocks CourseService service;

		@BeforeEach
		void setUp() {
			rejectForeignTrainer(validator);
		}

		@Test
		void foreignTrainerIsRejectedBeforeReading() {
			assertThatThrownBy(() -> service.getCourse(foreignTrainer(), PAGE, MEMBER_ID, SEARCH_DATE))
				.isInstanceOf(CustomException.class);
			verifyNoInteractions(members, courses, histories);
		}
	}

	@Nested
	@ExtendWith(MockitoExtension.class)
	@DisplayName("포인트")
	class Point {
		@Mock MemberDataAccessValidator validator;
		@Mock MemberRepository members;
		@Mock PointRepository points;
		@InjectMocks PointService service;

		@BeforeEach
		void setUp() {
			rejectForeignTrainer(validator);
		}

		@Test
		void foreignTrainerIsRejectedBeforeReading() {
			assertThatThrownBy(() -> service.getPoint(FOREIGN_TRAINER_ID, MEMBER_ID, SEARCH_DATE, PAGE))
				.isInstanceOf(CustomException.class);
			verifyNoInteractions(members, points);
		}
	}
}
