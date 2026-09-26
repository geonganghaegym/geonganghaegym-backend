package com.junghaebom.geonganghaegym.common.aop;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Joiner;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LogAspect {

	private static final Pattern SENSITIVE_NAME = Pattern.compile("(?i).*(password|token|secret).*");
	// record·Lombok toString의 "필드명=값" 중 민감한 필드의 값. 값은 다음 ", 필드명=" 또는 닫는 괄호 앞까지로 본다
	private static final Pattern SENSITIVE_FIELD = Pattern.compile(
		"(?i)(\\w*(?:password|token|secret)\\w*)=(.*?)(?=, \\w+=|[)\\]]|$)");
	private static final String MASK = "****";

	private static String getParameters(HttpServletRequest request) {
		return request.getParameterMap().entrySet().stream()
			.map(entry -> String.format("%s: (%s)", entry.getKey(),
				SENSITIVE_NAME.matcher(entry.getKey()).matches() ? MASK : Joiner.on(",").join(entry.getValue())))
			.collect(Collectors.joining(", "));
	}

	static String mask(String text) {
		return SENSITIVE_FIELD.matcher(text).replaceAll("$1=" + MASK);
	}

	@Pointcut("bean(*Controller)")
	private void allController() {
	}

	@Around("allController()")
	public Object doLogging(final ProceedingJoinPoint joinPoint) throws Throwable {
		log.info("===========================================================================");
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();

		long start = System.currentTimeMillis();
		try {
			log.info("Request: [{}] {}", request.getMethod(), request.getRequestURL());
			log.info("[Parameters] {}", getParameters(request));
			log.info("[Args] {}", mask(Arrays.toString(joinPoint.getArgs())));
			return joinPoint.proceed();
		} finally {
			long end = System.currentTimeMillis();
			log.info("RunningTime: {} ({}ms)", request.getRequestURI(), end - start);
		}
	}

}
