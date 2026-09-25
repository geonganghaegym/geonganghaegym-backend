package com.junghaebom.geonganghaegym.workout.domain;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import org.hibernate.annotations.ColumnDefault;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Table(name = "workout_history_files")
@Builder
@Getter
@ToString
public class WorkoutHistoryFiles extends BaseTimeEntity<WorkoutHistoryFiles, Long> {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "file_id")
	private Long id;

	private String fileUrl;
	private int fileOrder;

	@ColumnDefault("false")
	@Builder.Default
	private Boolean delYn = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_history_id")
	@ToString.Exclude
	private WorkoutHistory workoutHistory;

	private String fileName;

	public static WorkoutHistoryFiles create(WorkoutHistory history, String fileUrl, int fileOrder) {
		return WorkoutHistoryFiles.builder()
			.workoutHistory(history)
			.fileUrl(fileUrl)
			.fileOrder(fileOrder)
			.fileName(getFileName(fileUrl))
			.build();
	}

	private static String getFileName(String url) {
		String[] arr = url.split("/");
		return arr[arr.length - 1];
	}

	public void deleteWorkoutHistoryFile() {
		this.delYn = true;
	}

}
