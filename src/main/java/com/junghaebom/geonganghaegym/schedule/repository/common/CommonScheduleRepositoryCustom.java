package com.junghaebom.geonganghaegym.schedule.repository.common;

public interface CommonScheduleRepositoryCustom {
	Long getCompletedLessonCnt(Long memberId, Long courseId);
}
