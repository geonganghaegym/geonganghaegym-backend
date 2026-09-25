package com.junghaebom.geonganghaegym.course.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.presentation.dto.in.CourseAddCommand;
import com.junghaebom.geonganghaegym.course.presentation.dto.in.CourseUpdateCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/course")
@Tag(name = "08. 수강권 API", description = "수강권 API")
@Slf4j
public class CourseController {

	private final CourseService courseService;

	@Operation(summary = "수강권 등록")
	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<Void> addCourse(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@RequestBody @Valid CourseAddCommand command) {
		courseService.addCourse(customMemberDetails.getMember().getId(), command);
		return ApiResult.success("수강권이 등록되었습니다.");
	}

	@Operation(summary = "수강권 삭제")
	@DeleteMapping("/{courseId}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<Void> deleteCourse(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "수강권 ID") @PathVariable Long courseId) {
		courseService.deleteCourseByTrainer(customMemberDetails.getMember().getId(), courseId);
		return ApiResult.success("수강권이 삭제되었습니다.");
	}

	@Operation(summary = "수강권 횟수 증가/차감")
	@PatchMapping("/{courseId}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<Void> updateCourse(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "수강권 ID") @PathVariable Long courseId,
		@RequestBody @Valid CourseUpdateCommand command) {
		courseService.updateCourseByTrainer(customMemberDetails.getMember().getId(), courseId, command);
		return ApiResult.success("수강권 횟수가 증가 및 차감 되었습니다.");
	}

}
