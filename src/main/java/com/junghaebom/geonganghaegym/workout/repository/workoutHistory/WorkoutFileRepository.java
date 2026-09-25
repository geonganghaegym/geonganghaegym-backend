package com.junghaebom.geonganghaegym.workout.repository.workoutHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.junghaebom.geonganghaegym.workout.domain.WorkoutHistoryFiles;

public interface WorkoutFileRepository extends JpaRepository<WorkoutHistoryFiles, Long> {

}
