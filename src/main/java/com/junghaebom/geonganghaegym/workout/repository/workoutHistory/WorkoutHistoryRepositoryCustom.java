package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.junghaebom.geonganghaegym.workout.presentation.dto.out.WorkoutHistoryDto;
import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryFiles;

public interface WorkoutHistoryRepositoryCustom {
	Page<WorkoutHistoryDto> getWorkoutHistoryOfMonth(Long loginMemberId, Long memberId, Pageable pageable,
		String searchDate);

	Page<WorkoutHistoryDto> getWorkoutHistoryOnCommunityByMember(Long loginMemberId, Long gymId, Long memberId,
		Pageable pageable, String searchDate);

	Page<WorkoutHistoryDto> getWorkoutHistoryOnCommunity(Long loginMemberId, Long gymId, Pageable pageable,
		String searchDate);

	List<WorkoutHistoryFiles> getWorkoutHistoryFile(@Param("ids") List<Long> ids);

	WorkoutHistoryDto findByWorkoutHistoryId(Long loginMemberId, Long workoutHistoryId);

}
