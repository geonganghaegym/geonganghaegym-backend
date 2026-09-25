package com.junghaebom.geonganghaegym.lessonhistory.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.RetrieveLessonHistoryByDateCond;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.UnwrittenLessonHistorySearchCond;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CustomRetrieveLessonHistoryByDateCondResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.RetrieveLessonHistoryByDateCondResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.RetrieveLessonHistoryDetailResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.RetrieveUnwrittenLessonHistory;
import com.junghaebom.geonganghaegym.lessonhistory.repository.LessonHistoryRepository;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.schedule.repository.TrainerScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LessonHistoryService {

	private final LessonHistoryRepository lessonHistoryRepository;
	private final MemberRepository memberRepository;
	private final TrainerScheduleRepository trainerScheduleRepository;

	public CustomRetrieveLessonHistoryByDateCondResult findAllLessonHistory(
		RetrieveLessonHistoryByDateCond request,
		CustomMemberDetails member) {

		List<RetrieveLessonHistoryByDateCondResult> contents = lessonHistoryRepository
			.findAllLessonHistory(request, member.getMemberId(), member.getMemberType())
			.stream()
			.map(RetrieveLessonHistoryByDateCondResult::from)
			.collect(Collectors.toList());

		return CustomRetrieveLessonHistoryByDateCondResult.from(contents);
	}

	public CustomRetrieveLessonHistoryByDateCondResult findAllLessonHistoryByMemberId(
		Long studentId,
		RetrieveLessonHistoryByDateCond request,
		CustomMemberDetails member) {

		Member findMember = memberRepository.findById(studentId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		List<RetrieveLessonHistoryByDateCondResult> contents = lessonHistoryRepository
			.findAllLessonHistoryByMemberId(findMember.getId(), request, member.getMemberId())
			.stream()
			.map(RetrieveLessonHistoryByDateCondResult::from)
			.collect(Collectors.toList());

		return CustomRetrieveLessonHistoryByDateCondResult.from(contents);
	}

	@Transactional
	public RetrieveLessonHistoryDetailResult findOneLessonHistory(Long lessonHistoryId, CustomMemberDetails member) {
		var lessonHistory = lessonHistoryRepository.findOneLessonHistory(lessonHistoryId, member.getMemberId(),
			member.getMemberType());
		if (lessonHistory == null) {
			return null;
		}

		if (member.getMemberType() == MemberType.STUDENT) {
			lessonHistory.updateFeedbackChecked();
		}

		return RetrieveLessonHistoryDetailResult.detailFrom(lessonHistory);
	}

	public List<RetrieveUnwrittenLessonHistory> findAllUnwrittenLessonHistory(
		UnwrittenLessonHistorySearchCond request,
		Long memberId) {
		return trainerScheduleRepository.findAllUnwrittenLessonHistory(request, memberId)
			.stream()
			.filter(schedule -> LocalDateTime.now()
				.isAfter(LocalDateTime.of(schedule.getLessonDt(), schedule.getLessonEndTime())))
			.map(RetrieveUnwrittenLessonHistory::from)
			.collect(Collectors.toList());
	}
}
