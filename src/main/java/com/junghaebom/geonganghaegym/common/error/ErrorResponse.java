package com.junghaebom.geonganghaegym.common.error;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class ErrorResponse {

	private String message;
	private String code;
	private String timestamp;

	public ErrorResponse(String message, String code) {
		this.message = message;
		this.code = code;
		this.timestamp = LocalDateTime.now().toString();
	}

	public ErrorResponse(ErrorCode errorCode) {
		this.message = errorCode.getMessage();
		this.code = errorCode.getCode();
		this.timestamp = LocalDateTime.now().toString();
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.getMessage(), errorCode.getCode());
	}

	public static ErrorResponse of(String errorMessage) {
		return new ErrorResponse(errorMessage, "400");
	}
}
