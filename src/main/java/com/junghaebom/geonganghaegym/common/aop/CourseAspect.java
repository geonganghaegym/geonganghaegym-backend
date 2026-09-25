package com.junghaebom.geonganghaegym.common.aop;

import static com.junghaebom.geonganghaegym.course.domain.CourseHistoryType.*;
import static com.junghaebom.geonganghaegym.point.domain.Calculation.*;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.presentation.dto.in.CourseUpdateCommand;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.CommandCancelStudentReservationResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.CommandRegisterScheduleByStudentResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleIdInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class CourseAspect {

	private final int ONE_LESSON = 1;

	private final ObjectProvider<CourseService> courseServiceObjectProvider;

	public CourseAspect(ObjectProvider<CourseService> courseServiceObjectProvider) {
		this.courseServiceObjectProvider = courseServiceObjectProvider;
	}

	@Pointcut("execution(* com.junghaebom.geonganghaegym.schedule.application.CommonScheduleService.cancelMemberSchedule(..))")
	private void cancelMemberSchedule() {
	}

	@Pointcut("execution(* com.junghaebom.geonganghaegym.schedule.application.CommonScheduleService.reserveSchedule(..))")
	private void reserveSchedule() {
	}

	@Pointcut("execution(* com.junghaebom.geonganghaegym.schedule.application.TrainerScheduleCommandService.registerStudentInTrainerSchedule(..))")
	private void registerStudentInTrainerSchedule() {
	}

	@Pointcut("execution(* com.junghaebom.geonganghaegym.schedule.application.TrainerScheduleCommandService.cancelStudentReservation(..))")
	private void cancelStudentReservation() {
	}

	/*
	 * 학생이 수업 취소
	 */
	@AfterReturning(value = "cancelMemberSchedule()", returning = "returnValue")
	public void plusCourseByStudent(JoinPoint joinPoint, Object returnValue) {
		ScheduleIdInfo scheduleIdInfo = ((ScheduleIdInfo)returnValue);
		Long scheduleId = scheduleIdInfo.scheduleId();
		Long studentId = scheduleIdInfo.studentId();
		Long trainerId = scheduleIdInfo.trainerId();

		plusCourse(studentId, scheduleId, trainerId);
	}

	/*
	 * 트레이너가 학생의 수업 취소
	 */
	@AfterReturning(value = "cancelStudentReservation()", returning = "returnValue")
	public void plusCourseByTrainer(JoinPoint joinPoint, Object returnValue) {
		CommandCancelStudentReservationResult result = ((CommandCancelStudentReservationResult)returnValue);
		Long scheduleId = result.scheduleId();
		Long studentId = result.studentId();
		Long trainerId = result.trainerId();

		plusCourse(studentId, scheduleId, trainerId);
	}

	/*
	 * 학생이 수업 예약
	 */
	@AfterReturning(value = "reserveSchedule()", returning = "returnValue")
	public void minusCourseByStudent(JoinPoint joinPoint, Object returnValue) {
		ScheduleIdInfo scheduleIdInfo = ((ScheduleIdInfo)returnValue);
		Long scheduleId = scheduleIdInfo.scheduleId();
		Long studentId = scheduleIdInfo.studentId();
		Long trainerId = scheduleIdInfo.trainerId();

		minusCourse(studentId, scheduleId, trainerId);
	}

	/*
	 * 트레이너가 학생의 수업 예약
	 */
	@AfterReturning(value = "registerStudentInTrainerSchedule()", returning = "returnValue")
	public void minusCourseByTrainer(JoinPoint joinPoint, Object returnValue) {
		CommandRegisterScheduleByStudentResult result = ((CommandRegisterScheduleByStudentResult)returnValue);
		Long scheduleId = result.scheduleId();
		Long studentId = result.studentId();
		Long trainerId = result.trainerId();

		minusCourse(studentId, scheduleId, trainerId);
	}

	private void minusCourse(Long studentId, Long scheduleId, Long trainerId) {
		CourseService courseService = courseServiceObjectProvider.getObject();
		CourseUpdateCommand command = CourseUpdateCommand.create(studentId, MINUS, RESERVATION, ONE_LESSON);
		courseService.updateCourseByMember(scheduleId, trainerId, command);
	}

	private void plusCourse(Long studentId, Long scheduleId, Long trainerId) {
		CourseService courseService = courseServiceObjectProvider.getObject();
		CourseUpdateCommand command = CourseUpdateCommand.create(studentId, PLUS, RESERVATION_CANCEL, ONE_LESSON);
		courseService.updateCourseByMember(scheduleId, trainerId, command);
	}

}
