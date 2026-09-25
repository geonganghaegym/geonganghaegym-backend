package com.junghaebom.geonganghaegym.schedule.presentation.dto.out;

import java.util.ArrayList;
import java.util.List;

import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonInfoResult.LessonDetailResult;

public record RetrieveTrainerScheduleByLessonDtResult(
	String trainerName,
	Long scheduleTotalCount,
	List<LessonDetailResult> schedule
) {
	public RetrieveTrainerScheduleByLessonDtResult(String trainerName, Long scheduleTotalCount) {
		this(trainerName, scheduleTotalCount, new ArrayList<>());
	}
}
