package com.junghaebom.geonganghaegym.config;

import static java.nio.charset.StandardCharsets.*;
import static java.time.Duration.*;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
			.requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
			.connectTimeout(ofMillis(5000)) // connection-timeout
			.readTimeout(ofMillis(5000)) // read-timeout
			.additionalMessageConverters(new StringHttpMessageConverter(UTF_8))
			.build();
	}
}
