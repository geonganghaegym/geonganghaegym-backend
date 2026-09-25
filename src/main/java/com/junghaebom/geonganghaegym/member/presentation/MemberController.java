package com.junghaebom.geonganghaegym.member.presentation;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.junghaebom.geonganghaegym.common.CustomPaging;
import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.diet.application.DietService;
import com.junghaebom.geonganghaegym.diet.presentation.dto.DietDto;
import com.junghaebom.geonganghaegym.member.application.MemberService;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.ValidateCurrentPassword;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.MemberInfoResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.RetrieveTrainerInfo;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.TrainerMappingResult;
import com.junghaebom.geonganghaegym.point.application.PointService;
import com.junghaebom.geonganghaegym.workout.application.WorkoutHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Slf4j
@Valid
@Tag(name = "02. 회원 API", description = "인증이 있어야만 접근 가능한 회원 API")
public class MemberController {

	private final MemberService memberService;
	private final WorkoutHistoryService workoutService;
	private final CourseService courseService;
	private final PointService pointService;
	private final DietService dietService;

	@Operation(summary = "내 정보 조회")
	@GetMapping("/me")
	public ApiResult<MemberInfoResult> getMemberInfo(@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("회원정보가 조회 되었습니다.", memberService.getMemberInfo(member.getMemberId()));
	}

	@Operation(summary = "회원 정보조회")
	@GetMapping("/{memberId}")
	public ApiResult<MemberInfoResult> getMemberInfo(@PathVariable Long memberId) {
		return ApiResult.success("회원정보가 조회 되었습니다.", memberService.getMemberInfo(memberId));
	}

	/**
	 * ============================== 운동기록 시작 ==============================
	 */
	@Operation(summary = "내 운동기록 목록 조회")
	@GetMapping("/me/workout-histories")
	public ApiResult<CustomPaging> getWorkoutHistory(String searchDate,
		Pageable pageable,
		@AuthenticationPrincipal CustomMemberDetails loginMember) {
		return ApiResult.success("운동기록이 조회되었습니다.", workoutService.getWorkoutHistory(loginMember.getMember(), loginMember.getMemberId(), pageable, searchDate));
	}

	@Operation(summary = "학생의 운동기록 목록 조회")
	@GetMapping("/{memberId}/workout-histories")
	public ApiResult<CustomPaging> getWorkoutHistory(@PathVariable Long memberId, String searchDate,
		@AuthenticationPrincipal CustomMemberDetails loginMember,
		Pageable pageable) {
		return ApiResult.success("운동기록이 조회되었습니다.", workoutService.getWorkoutHistory(loginMember.getMember(), memberId, pageable, searchDate));
	}

	/**
	 * ============================== 운동기록 종료 ==============================
	 */

	/**
	 * ============================== 식단기록 시작 ==============================
	 */
	@Operation(summary = "내 식단기록 목록 조회")
	@GetMapping("/me/diets")
	public ApiResult<CustomPaging<DietDto>> getDiet(String searchDate,
		Pageable pageable,
		@AuthenticationPrincipal CustomMemberDetails loginMember) {
		return ApiResult.success("식단기록 조회되었습니다.", dietService.getDiet(loginMember.getMemberId(), loginMember.getMemberId(), pageable, searchDate));
	}

	@Operation(summary = "다른 학생의 식단기록 목록 조회")
	@GetMapping("/{memberId}/diets")
	public ApiResult<CustomPaging<DietDto>> getDiet(@AuthenticationPrincipal CustomMemberDetails loginMember,
		@PathVariable Long memberId,
		String searchDate,
		Pageable pageable) {
		return ApiResult.success("식단기록 조회되었습니다.", dietService.getDiet(loginMember.getMemberId(), memberId, pageable, searchDate));
	}

	@Operation(summary = "내 트레이너가 관리하는 학생들의 식단기록 목록 조회하기")
	@GetMapping("/my-trainer/diets")
	public ApiResult<CustomPaging<DietDto>> getDietMyTrainer(String searchDate,
		Pageable pageable,
		@AuthenticationPrincipal CustomMemberDetails loginMember) {
		return ApiResult.success("식단기록 조회되었습니다.", dietService.getDietMyTrainer(loginMember.getMemberId(), pageable, searchDate));
	}

	/**
	 * ============================== 식단기록 종료 ==============================
	 */

	@Operation(summary = "학생이 본인의 수강권 조회")
	@GetMapping("/course")
	public ApiResult<CustomPaging> getMyCourse(String searchDate,
		Pageable pageable,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ApiResult.success("수강권이 조회되었습니다.", courseService.getCourse(customMemberDetails.getMember(), pageable, customMemberDetails.getMemberId(), searchDate));
	}

	@Operation(summary = "트레이너가 학생의 수강권 조회")
	@GetMapping("/{memberId}/course")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<CustomPaging> getCourse(@PathVariable Long memberId,
		String searchDate,
		Pageable pageable,
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ApiResult.success("수강권이 조회되었습니다.", courseService.getCourse(customMemberDetails.getMember(), pageable, memberId, searchDate));
	}

	@Operation(summary = "학생이 본인의 포인트 조회")
	@GetMapping("/point")
	public ApiResult<CustomPaging> getMyPoint(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
		String searchDate, Pageable pageable) {
		return ApiResult.success("포인트가 조회되었습니다.", pointService.getPoint(customMemberDetails.getMember().getId(), searchDate, pageable));
	}

	@Operation(summary = "트레이너가 학생의 포인트 조회")
	@GetMapping("/{memberId}/point")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ApiResult<CustomPaging> getPoint(@PathVariable Long memberId, String searchDate, Pageable pageable) {
		return ApiResult.success("포인트가 조회되었습니다.", pointService.getPoint(memberId, searchDate, pageable));
	}

	@Operation(summary = "학생이 트레이너와 매핑 여부 조회")
	@GetMapping("/trainer-mapping")
	public ApiResult<TrainerMappingResult> getTrainerMapping(
		@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ApiResult.success("매핑 여부가 조회되었습니다.", memberService.getTrainerMapping(customMemberDetails.getMember()));
	}

	@Operation(summary = "학생이 내 트레이너 정보 조회")
	@PreAuthorize("hasAuthority('ROLE_STUDENT')")
	@GetMapping("/trainer-mapping/info")
	public ApiResult<RetrieveTrainerInfo> findMyTrainerInfo(@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("내 트레이너 정보를 조회하였습니다.", memberService.findMyTrainerInfo(member.getMemberId()));
	}

	@Operation(summary = "회원이 현재 비밀번호를 검증한다.")
	@PostMapping("/password")
	public ApiResult<Boolean> validateCurrentPassword(@RequestBody @Valid ValidateCurrentPassword request,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("비밀번호가 확인되었습니다.", memberService.validateCurrentPassword(request, member.getMemberId()));
	}
}
