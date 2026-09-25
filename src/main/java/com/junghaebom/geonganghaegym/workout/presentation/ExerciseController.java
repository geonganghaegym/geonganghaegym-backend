package com.junghaebom.geonganghaegym.workout.presentation;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.workout.application.ExerciseService;
import com.junghaebom.geonganghaegym.workout.presentation.dto.ExerciseDto;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.CustomExerciseAddCommand;
import com.junghaebom.geonganghaegym.workout.presentation.dto.out.ExerciseCategoryDto;
import com.junghaebom.geonganghaegym.workout.domain.ExerciseCategory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exercise")
@Tag(name = "06-00. 운동종류 API", description = "운동종류 API")
@Slf4j
public class ExerciseController {

	private final ExerciseService exerciseService;

	@Operation(summary = "운동 카레고리 조회")
	@GetMapping("/category")
	public ApiResult<List<ExerciseCategoryDto>> getExerciseCategory() {
		return ApiResult.success("운동 카테고리가 조회되었습니다.", Arrays.stream(ExerciseCategory.values()).map(ExerciseCategoryDto::from).toList());
	}

	@Operation(summary = "운동 종류 목록 조회")
	@GetMapping
	public ApiResult<CustomPaging<ExerciseDto>> getExercise(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "카테고리") @RequestParam(required = false) ExerciseCategory exerciseCategory,
		@Parameter(description = "검색할 이름", example = "임채린") @RequestParam(required = false) String searchValue,
		Pageable pageable) {
		return ApiResult.success("운동 종류가 조회되었습니다.", exerciseService.getExercise(customMemberDetails.getMember(), exerciseCategory, pageable, searchValue));
	}

	@Operation(summary = "운동 종류 추가")
	@PostMapping
	public ApiResult<Void> addExerciseCustom(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Valid @RequestBody CustomExerciseAddCommand command) {
		exerciseService.addExerciseCustom(customMemberDetails.getMember(), command);
		return ApiResult.success("운동 종류가 등록되었습니다.");
	}

	@Operation(summary = "운동 종류 삭제")
	@DeleteMapping("/{exerciseId}")
	public ApiResult<Void> deleteExerciseCustom(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		@Parameter(description = "운동종류 ID") @PathVariable Long exerciseId) {
		exerciseService.deleteExerciseCustom(customMemberDetails.getMember(), exerciseId);
		return ApiResult.success("운동 종류가 삭제되었습니다.");
	}

}
