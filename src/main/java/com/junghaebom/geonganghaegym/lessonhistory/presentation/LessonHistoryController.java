package com.junghaebom.geonganghaegym.lessonhistory.presentation;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.lessonhistory.application.LessonHistoryService;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.RetrieveLessonHistoryByDateCond;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.UnwrittenLessonHistorySearchCond;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CustomRetrieveLessonHistoryByDateCondResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.RetrieveLessonHistoryDetailResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.RetrieveUnwrittenLessonHistory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lessonhistory")
@Tag(name = "07. 수업 일지")
@RequiredArgsConstructor
public class LessonHistoryController {

	private final LessonHistoryService lessonHistoryService;

	@Operation(summary = "전체 수업 일지를 조회한다.")
	@GetMapping
	public ApiResult<CustomRetrieveLessonHistoryByDateCondResult> findAllLessonHistory(
		@ParameterObject RetrieveLessonHistoryByDateCond request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"전체 수업 일지를 조회하였습니다.",
			lessonHistoryService.findAllLessonHistory(request, member)
		);
	}

	@Operation(summary = "수업일지 단건을 조회한다.")
	@GetMapping("/{lessonHistoryId}")
	public ApiResult<RetrieveLessonHistoryDetailResult> findOneLessonHistory(
		@PathVariable Long lessonHistoryId,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"수업 일지 단건을 조회하였습니다.",
			lessonHistoryService.findOneLessonHistory(lessonHistoryId, member)
		);
	}

	@Operation(summary = "학생의 수업일지 전체를 조회한다.")
	@GetMapping("/student/{studentId}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<CustomRetrieveLessonHistoryByDateCondResult> findAllLessonHistoryByMemberId(
		@PathVariable Long studentId,
		@ParameterObject RetrieveLessonHistoryByDateCond request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"학생의 수업 일지 전체를 조회하였습니다.",
			lessonHistoryService.findAllLessonHistoryByMemberId(studentId, request, member)
		);
	}

	@Operation(summary = "수업일지를 작성하지 않은 수업들을 조회하였습니다.")
	@GetMapping("/unwritten")
	public ApiResult<List<RetrieveUnwrittenLessonHistory>> findAllUnwrittenLessonHistory(
		UnwrittenLessonHistorySearchCond request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success(
			"수업일지를 작성하지 않은 수업들을 조회하였습니다.",
			lessonHistoryService.findAllUnwrittenLessonHistory(request, member.getMemberId())
		);
	}
}
