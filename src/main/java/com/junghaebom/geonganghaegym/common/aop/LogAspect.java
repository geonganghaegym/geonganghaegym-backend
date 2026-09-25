package com.junghaebom.geonganghaegym.common.aop;

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

	private static String getParameters(HttpServletRequest request) {
		return request.getParameterMap().entrySet().stream()
			.map(entry -> String.format("%s: (%s)", entry.getKey(), Joiner.on(",").join(entry.getValue())))
			.collect(Collectors.joining(", "));
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
			log.info("[Args] {}", joinPoint.getArgs());
			return joinPoint.proceed();
		} finally {
			long end = System.currentTimeMillis();
			log.info("RunningTime: {} ({}ms)", request.getRequestURI(), end - start);
		}
	}

}
