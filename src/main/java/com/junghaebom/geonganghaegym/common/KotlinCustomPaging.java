package com.junghaebom.geonganghaegym.common;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.junghaebom.geonganghaegym.common.NotificationSenderInfo.SenderInfo;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.NotificationRedDotStatusResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KotlinCustomPaging<T> {

	private List<T> content;

	private int pageNumber;

	private int pageSize;

	private int totalPages;

	private long totalElements;

	@JsonProperty("isLast")
	private boolean isLast;

	@JsonInclude(Include.NON_EMPTY)
	@Builder.Default
	private List<NotificationRedDotStatusResult> redDotStatus = new ArrayList<>();

	@JsonInclude(Include.NON_NULL)
	private SenderInfo sender;

	public KotlinCustomPaging(
		List<T> content,
		int pageNumber,
		int pageSize,
		int totalPages,
		long totalElements,
		boolean isLast
	) {
		this.content = content;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalPages = totalPages;
		this.totalElements = totalElements;
		this.isLast = isLast;
		this.redDotStatus = new ArrayList<>();
		this.sender = null;
	}
}
