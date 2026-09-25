package com.junghaebom.geonganghaegym.diet.domain;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import org.hibernate.annotations.ColumnDefault;

import com.junghaebom.geonganghaegym.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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
@Table(name = "diet_files")
@Builder
@Getter
@ToString
public class DietFiles extends BaseTimeEntity<DietFiles, Long> {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "file_id")
	private Long id;

	private String fileUrl;

	@ColumnDefault("false")
	@Builder.Default
	@ToString.Exclude
	private Boolean delYn = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "diet_id")
	@ToString.Exclude
	private Diet diet;

	@Enumerated(STRING)
	@ColumnDefault("'BREAKFAST'")
	private DietType type;

	private String fileName;

	public static DietFiles create(Diet diet, String fileUrl, DietType type) {
		return DietFiles.builder()
			.fileUrl(fileUrl)
			.diet(diet)
			.type(type)
			.fileName(getFileName(fileUrl))
			.build();
	}

	private static String getFileName(String url) {
		String[] arr = url.split("/");
		return arr[arr.length - 1];
	}

	public void deleteDietFile() {
		this.delYn = true;
	}

}
