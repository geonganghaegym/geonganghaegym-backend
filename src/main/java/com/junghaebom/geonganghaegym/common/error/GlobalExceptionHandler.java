package com.junghaebom.geonganghaegym.common.error;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	protected ResponseEntity<ErrorResponse> handleCustomException(final CustomException e) {
		log.error("CustomException => {}", e.getMessage());
		final ErrorResponse response = ErrorResponse.of(e.getMessage());
		return new ResponseEntity<>(response, e.getErrorCode().getStatus());
	}

	@ExceptionHandler(OAuthException.class)
	protected ResponseEntity<ErrorResponse> handleCustomException(final OAuthException e) {
		log.error("OAuthException => {}", e.getMessage());
		final ErrorResponse response = ErrorResponse.of(e.getMessage());
		return new ResponseEntity<>(response, BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	protected ResponseEntity<ErrorResponse> handleException(final IllegalArgumentException e) {
		log.error("IllegalArgumentException => {}", e.getMessage());
		final ErrorResponse response = ErrorResponse.of(e.getMessage());
		return new ResponseEntity<>(response, BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(final Exception e) {
		log.error("Exception => {}", e.getMessage());
		final ErrorResponse response = ErrorResponse.of("서버에서 에러가 발생하였습니다.");
		return new ResponseEntity<>(response, INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
		log.error("MethodArgumentNotValidException: {}", e.getMessage());
		final ErrorResponse response = ErrorResponse.of("서버에서 에러가 발생하였습니다.");
		return new ResponseEntity<>(response, HttpStatusCode.valueOf(BAD_REQUEST.value()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(HttpMessageNotReadableException e) {
		log.error("HttpMessageNotReadableException: {}", e.getMessage());
		final ErrorResponse response = ErrorResponse.of(e.getMessage());
		return new ResponseEntity<>(response, HttpStatusCode.valueOf(BAD_REQUEST.value()));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	protected ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
		log.error("NoResourceFoundException => {}", e.getMessage());
		final ErrorResponse response = ErrorResponse.of("요청한 리소스를 찾을 수 없습니다.");
		return new ResponseEntity<>(response, NOT_FOUND);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.error("MethodArgumentTypeMismatchException => {}", e.getMessage());
		final ErrorResponse response = ErrorResponse.of("잘못된 파라미터 값입니다: " + e.getValue());
		return new ResponseEntity<>(response, BAD_REQUEST);
	}
}
