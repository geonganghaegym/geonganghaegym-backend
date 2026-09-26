package com.junghaebom.geonganghaegym.config.security;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorResponse.*;
import static org.springframework.http.MediaType.*;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.junghaebom.geonganghaegym.common.error.ErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final ErrorResponse exceptionResponse = of(HANDLE_ACCESS_DENIED);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException)
		throws IOException, ServletException {

		log.error("CustomAccessDeniedHandler => {}", accessDeniedException.getMessage());
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(HANDLE_ACCESS_DENIED.getStatus().value());

		try (OutputStream os = response.getOutputStream()) {
			// Jackson 3 기본값(프로퍼티 알파벳 정렬 등) 대신 기존 응답 형태를 유지한다.
			ObjectMapper objectMapper = JsonMapper.builderWithJackson2Defaults().build();
			objectMapper.writeValue(os, exceptionResponse);
			os.flush();
		}
	}
}
