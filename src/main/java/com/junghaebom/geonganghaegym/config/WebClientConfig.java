package com.junghaebom.geonganghaegym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient() {
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder() //디폴트 코덱의 최대 버퍼사이즈 조정
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
			.build(); // to unlimited memory size .build();
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
}