package com.junghaebom.geonganghaegym.config.security;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorResponse.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junghaebom.geonganghaegym.common.error.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ErrorResponse exceptionResponse = of(HANDLE_ACCESS_DENIED);

	@Override
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
		AuthenticationException e) throws IOException {
		httpServletResponse.setContentType(APPLICATION_JSON_VALUE);
		httpServletResponse.setStatus(UNAUTHORIZED.value());

		try (OutputStream os = httpServletResponse.getOutputStream()) {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(os, exceptionResponse);
			os.flush();
		}
	}
}