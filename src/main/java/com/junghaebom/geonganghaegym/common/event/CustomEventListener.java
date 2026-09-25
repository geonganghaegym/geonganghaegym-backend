package com.junghaebom.geonganghaegym.common.event;

import static com.junghaebom.geonganghaegym.common.LessonTimeFormatter.*;
import static com.junghaebom.geonganghaegym.common.Utils.WEB_BASE_URL;
import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.course.domain.CourseHistoryType.*;
import static com.junghaebom.geonganghaegym.notification.domain.NotificationCategory.*;
import static com.junghaebom.geonganghaegym.notification.domain.NotificationType.*;
import static com.junghaebom.geonganghaegym.point.domain.Calculation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.presentation.dto.in.CourseUpdateCommand;
import com.junghaebom.geonganghaegym.course.repository.CourseRepository;
import com.junghaebom.geonganghaegym.notification.application.NotificationService;
import com.junghaebom.geonganghaegym.notification.presentation.dto.in.CommandSendNotification;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.domain.ScheduleWaiting;
import com.junghaebom.geonganghaegym.schedule.repository.common.CommonScheduleRepository;
import com.junghaebom.geonganghaegym.schedule.repository.waiting.ScheduleWaitingRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomEventListener {

	private final int ONE_LESSON = 1;

	private final CourseService courseService;
	private final CourseRepository courseRepository;
	private final CommonScheduleRepository commonScheduleRepository;
	private final ScheduleWaitingRepository scheduleWaitingRepository;
	private final NotificationService notificationService;

	@Async
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	@TransactionalEventListener
	public void handleEvent(CustomEvent event) {
		switch (event.type()) {
			case SCHEDULE_CANCEL -> changeWaitingToCompleted((Long)event.result());
			case NOTIFICATION -> sendNotification((CommandSendNotification)event.result());
		}
	}

	public void changeWaitingToCompleted(Long scheduleId) {
		// 대기자 있으면 예약으로 변경
		Optional<ScheduleWaiting> scheduleWaitingOpt = scheduleWaitingRepository.findByScheduleId(scheduleId);
		if (scheduleWaitingOpt.isPresent()) {
			ScheduleWaiting scheduleWaiting = scheduleWaitingOpt.get();
			scheduleWaitingRepository.delete(scheduleWaiting);
			Schedule schedule = commonScheduleRepository.findById(scheduleId)
				.orElseThrow(() -> new CustomException(SCHEDULE_NOT_FOUND));

			//수업시간 24시간 이전인 경우만 대기 -> 예약으로 변경 가능
			if (!isBefore24Hour(schedule))
				return;

			//수강권 유효성 검사
			Long waitingMemberId = scheduleWaiting.getMember().getId();
			courseRepository.findTop1ByMemberIdAndRemainLessonCntGreaterThanOrderByCreatedAtDesc(waitingMemberId, 0)
				.ifPresent(i -> {
					minusCourse(waitingMemberId, scheduleId, schedule.getTrainer().getId());
					schedule.registerSchedule(scheduleWaiting.getMember());

					CommandSendNotification studentNotification = new CommandSendNotification(
						WAITING.getDescription(),
						String.format(WAITING.getContent(),
							LocalDateTime.of(schedule.getLessonDt(), schedule.getLessonStartTime())
								.format(lessonStartDateTimeFormatter())),
						List.of(schedule.getApplicant().getId()),
						WAITING,
						SCHEDULE,
						null,
						WEB_BASE_URL + "/student/schedule?tab=myReservation",
						schedule.getApplicant().getId(),
						schedule.getApplicant().getName()
					);

					CommandSendNotification trainerNotification = new CommandSendNotification(
						WAITING_FOR_TRAINER.getDescription(),
						String.format(WAITING_FOR_TRAINER.getContent(), schedule.getApplicant().getName(),
							LocalDateTime.of(schedule.getLessonDt(), schedule.getLessonStartTime())
								.format(lessonStartDateTimeFormatter())),
						List.of(schedule.getTrainer().getId()),
						WAITING_FOR_TRAINER,
						SCHEDULE,
						null,
						null,
						schedule.getApplicant().getId(),
						schedule.getApplicant().getName()
					);

					sendNotification(studentNotification);
					sendNotification(trainerNotification);

					commonScheduleRepository.save(schedule);
				});
		}
	}

	private void sendNotification(CommandSendNotification request) {
		notificationService.sendNotificationFromSystem(request);
	}

	private boolean isBefore24Hour(Schedule schedule) {
		LocalDateTime before24Hour = LocalDateTime.of(schedule.getLessonDt().minusDays(1),
			schedule.getLessonStartTime());
		return LocalDateTime.now().isBefore(before24Hour);
	}

	private void minusCourse(Long studentId, Long scheduleId, Long trainerId) {
		CourseUpdateCommand command = CourseUpdateCommand.create(studentId, MINUS, RESERVATION, ONE_LESSON);
		courseService.updateCourseByMember(scheduleId, trainerId, command);
	}
}
