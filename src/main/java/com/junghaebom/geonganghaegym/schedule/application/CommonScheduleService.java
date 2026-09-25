package com.junghaebom.geonganghaegym.schedule.application;

import static com.junghaebom.geonganghaegym.common.Utils.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.common.event.EventType.*;
import static com.junghaebom.geonganghaegym.notification.domain.NotificationCategory.*;
import static com.junghaebom.geonganghaegym.notification.domain.NotificationType.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.event.CustomEventPublisher;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.notification.presentation.dto.in.CommandSendNotification;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.ScheduleIdInfo;
import com.junghaebom.geonganghaegym.schedule.domain.Schedule;
import com.junghaebom.geonganghaegym.schedule.repository.common.CommonScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommonScheduleService {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일(E) h시");

	private final CustomEventPublisher<Long> eventPublisher;
	private final CustomEventPublisher<CommandSendNotification> notificationPublisher;

	private final MemberRepository memberRepository;
	private final CommonScheduleRepository commonScheduleRepository;

	public ScheduleIdInfo reserveSchedule(Long scheduleId, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		Schedule schedule = commonScheduleRepository.findAvailableScheduleById(scheduleId)
			.orElseThrow(() -> new CustomException(NOT_RESERVABLE_SCHEDULE));

		LocalDateTime before30Minutes = LocalDateTime.of(schedule.getLessonDt(),
			schedule.getLessonStartTime().minusMinutes(30));
		if (LocalDateTime.now().isAfter(before30Minutes))
			throw new CustomException(RESERVATION_NOT_VALID);

		schedule.registerSchedule(member);

		// 일정 신청시 알림
		CommandSendNotification notification = new CommandSendNotification(
			RESERVE.getDescription(),
			String.format("%s님이 %s에 예약했어요.",
				schedule.getApplicant().getName(),
				LocalDateTime.of(schedule.getLessonDt(), schedule.getLessonStartTime()).format(formatter)
			),
			List.of(schedule.getTrainer().getId()),
			RESERVE,
			SCHEDULE,
			null,
			String.format(WEB_BASE_URL + "/trainer/manage/%d/reservation?name=%s",
				schedule.getApplicant().getId(), schedule.getApplicant().getName()),
			schedule.getApplicant().getId(),
			schedule.getApplicant().getName()
		);

		notificationPublisher.publish(notification, NOTIFICATION);
		log.info("[수업 신청] member: {}, schedule: {}, trainer: {}", member, schedule, schedule.getTrainer());
		return ScheduleIdInfo.create(schedule, schedule.getLessonStartTime().format(formatter_hmm));
	}

	public ScheduleIdInfo cancelMemberSchedule(Long scheduleId, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		Schedule schedule = commonScheduleRepository.findScheduleByApplicantId(memberId, scheduleId)
			.orElseThrow(() -> new CustomException(SCHEDULE_NOT_FOUND));

		LocalDateTime before24Hour = LocalDateTime.of(schedule.getLessonDt().minusDays(1),
			schedule.getLessonStartTime());
		if (LocalDateTime.now().isAfter(before24Hour))
			throw new CustomException(RESERVATION_CANCEL_NOT_VALID);

		// 일정 취소시 알림
		CommandSendNotification notification = new CommandSendNotification(
			CANCEL.getDescription(),
			String.format("%s님이 %s 예약을 취소했어요.",
				schedule.getApplicant().getName(),
				LocalDateTime.of(schedule.getLessonDt(), schedule.getLessonStartTime()).format(formatter)
			),
			List.of(schedule.getTrainer().getId()),
			CANCEL,
			SCHEDULE,
			null,
			String.format(WEB_BASE_URL + "/trainer/manage/%d/reservation?name=%s",
				schedule.getApplicant().getId(), schedule.getApplicant().getName()),
			schedule.getApplicant().getId(),
			schedule.getApplicant().getName()
		);

		notificationPublisher.publish(notification, NOTIFICATION);

		ScheduleIdInfo idInfo = ScheduleIdInfo.create(schedule, schedule.getLessonStartTime().format(formatter_hmm));
		schedule.cancelMemberSchedule();

		eventPublisher.publish(scheduleId, SCHEDULE_CANCEL);
		log.info("[수업 취소] member: {}, schedule: {}, trainer: {}", member, schedule, schedule.getTrainer());
		return idInfo;
	}

	public ScheduleIdInfo cancelMemberScheduleForce(Long scheduleId, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		Schedule schedule = commonScheduleRepository.findScheduleByApplicantId(memberId, scheduleId)
			.orElseThrow(() -> new CustomException(SCHEDULE_NOT_FOUND));

		// 트레이너가 일정 취소시 알림
		CommandSendNotification notification = new CommandSendNotification(
			CANCEL.getDescription(),
			String.format(CANCEL.getContent(),
				schedule.getTrainer().getName(),
				schedule.getApplicant().getName(),
				LocalDateTime.of(schedule.getLessonDt(), schedule.getLessonStartTime()).format(formatter)
			),
			List.of(schedule.getApplicant().getId()),
			CANCEL,
			SCHEDULE,
			null,
			WEB_BASE_URL + "/student/schedule?tab=myReservation",
			schedule.getApplicant().getId(),
			schedule.getApplicant().getName()
		);

		notificationPublisher.publish(notification, NOTIFICATION);

		ScheduleIdInfo idInfo = ScheduleIdInfo.create(schedule, schedule.getLessonStartTime().format(formatter_hmm));
		schedule.cancelMemberSchedule();

		eventPublisher.publish(scheduleId, SCHEDULE_CANCEL);
		log.info("[수업 취소] member: {}, schedule: {}, trainer: {}", member, schedule, schedule.getTrainer());
		return idInfo;
	}

}
