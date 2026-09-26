package com.junghaebom.geonganghaegym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient() {
		ExchangeStrategies exchangeStrategies = exchangeStrategies();
		ConnectionProvider connectionProvider = ConnectionProvider.builder("myConnectionPool")
			.maxConnections(2000)//커넥션 풀에서 살아있을 수 있는 커넥션의 최대 수명시간
			.pendingAcquireMaxCount(2000) //커넥션 풀에서 idle 상태의 커넥션을 유지하는 시간
			.build();
		ReactorClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(
			HttpClient.create(connectionProvider));
		return WebClient.builder()
			.exchangeStrategies(exchangeStrategies)
			.clientConnector(clientHttpConnector)
			.build();
	}

	/**
	 * Jackson 3는 누락/null 값을 primitive 필드에 넣으면 실패한다(FAIL_ON_NULL_FOR_PRIMITIVES 기본 활성).
	 * 소셜 응답은 선언한 필드를 생략하는 경우가 있어(예: 구글 토큰의 refresh_token_expires_in) Jackson 2처럼 기본값(0/false)으로 받는다.
	 * 나머지 설정(알 수 없는 필드 무시 등)은 WebClient 기본 코덱과 같다.
	 */
	public static ExchangeStrategies exchangeStrategies() {
		JacksonJsonDecoder decoder = new JacksonJsonDecoder(
			JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));
		decoder.setMaxInMemorySize(-1);
		return ExchangeStrategies.builder() //디폴트 코덱의 최대 버퍼사이즈 조정
			.codecs(configurer -> {
				configurer.defaultCodecs().maxInMemorySize(-1); // to unlimited memory size
				configurer.defaultCodecs().jacksonJsonDecoder(decoder);
			})
			.build();
	}
}