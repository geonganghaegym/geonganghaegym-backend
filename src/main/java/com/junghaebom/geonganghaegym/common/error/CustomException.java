package com.junghaebom.geonganghaegym.common.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException {
	private ErrorCode errorCode;

	public HttpStatus getStatus() {
		return this.errorCode.getStatus();
	}

	public String getCode() {
		return this.errorCode.getCode();
	}

	public String getMessage() {
		return this.errorCode.getMessage();
	}
}
