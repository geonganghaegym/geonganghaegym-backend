package com.junghaebom.geonganghaegym.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CustomPaging<T> {

	private List<T> content;
	private int pageNumber;
	private int pageSize;
	private int totalPages;
	private Long totalElements;
	@JsonProperty("isLast")
	private Boolean isLast;
	private T mainData;

	public CustomPaging(List<T> content, int pageNumber, int pageSize, int totalPages, Long totalElements,
		Boolean isLast) {
		this.content = content;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalPages = totalPages;
		this.totalElements = totalElements;
		this.isLast = isLast;
	}

}
