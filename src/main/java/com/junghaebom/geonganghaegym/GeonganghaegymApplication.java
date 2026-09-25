package com.junghaebom.geonganghaegym;

import static java.util.TimeZone.*;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
@EnableAsync
public class GeonganghaegymApplication {
	public static void main(String[] args) {
		SpringApplication.run(GeonganghaegymApplication.class, args);
	}

	@PostConstruct
	public void setTimeZone() {
		TimeZone.setDefault(getTimeZone("Asia/Seoul"));
		Locale.setDefault(Locale.KOREA);
	}
}