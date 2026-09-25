package com.junghaebom.geonganghaegym.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomPagingResponse<T> {

	@JsonInclude(Include.NON_NULL)
	private String studentName;

	private List<T> content;

	private int pageNumber;

	private int pageSize;

	private int totalPages;

	private long totalElements;

	@JsonProperty("isLast")
	private boolean isLast;
}
