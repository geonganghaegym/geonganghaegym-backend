package com.junghaebom.geonganghaegym.lessonhistory.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.RetrieveLessonHistoryByDateCond;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.RetrieveLessonHistoryByDateCondResult;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistory;
import com.junghaebom.geonganghaegym.member.domain.MemberType;

public interface LessonHistoryRepositoryCustom {
	LessonHistory findById(Long lessonHistoryId, Long memberId, MemberType memberType);

	List<LessonHistory> findAllLessonHistory(RetrieveLessonHistoryByDateCond request, Long memberId,
		MemberType memberType);

	LessonHistory findOneLessonHistory(Long lessonHistoryId, Long memberId, MemberType memberType);

	List<LessonHistory> findAllLessonHistoryByMemberId(Long studentId, RetrieveLessonHistoryByDateCond request,
		Long trainerId);

	RetrieveLessonHistoryByDateCondResult findTop1LessonHistoryByMemberId(Long studentId);

	Page<LessonHistory> findAllMyLessonHistory(RetrieveLessonHistoryByDateCond request, Pageable pageable,
		CustomMemberDetails member);

	LessonHistory findOneLessonHistoryWithFiles(Long lessonHistoryId, Long trainerId);

	boolean validateDuplicateLessonHistory(Long trainerId, Long studentId, Long scheduleId);
}
