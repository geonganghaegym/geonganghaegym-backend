package com.junghaebom.geonganghaegym;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApiResult<T> {

	private final HttpStatus status;
	private final String message;
	private final T data;

	public ApiResult(String message, T data) {
		this.status = HttpStatus.OK;
		this.message = message;
		this.data = data;
	}

	public static <T> ApiResult<T> success(String message, T data) {
		return new ApiResult<>(message, data);
	}

	public static ApiResult<Void> success(String message) {
		return new ApiResult<>(message, null);
	}
}
