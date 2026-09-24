package com.tobe.healthy.config.security;

import static org.springframework.security.config.http.SessionCreationPolicy.*;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.tobe.healthy.config.jwt.JwtFilter;
import com.tobe.healthy.config.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.httpBasic(AbstractHttpConfigurer::disable)
			.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
			.exceptionHandling(exceptionHandling -> {
				exceptionHandling.authenticationEntryPoint(authenticationEntryPoint);
				exceptionHandling.accessDeniedHandler(accessDeniedHandler);
			})
			.authorizeHttpRequests(
				authorize -> authorize
					.requestMatchers(
						"/api/v1/auth/**",
						"/favicon.ico",
						"/actuator/**",
						"/api/v1/push/webview",
						"/api/v1/schedule/all/{trainerId}",
						"/swagger-ui/**",
						"/swagger-ui.html",
						"/v3/api-docs/**",
						"/swagger-resources/**",
						"/files/**"
					).permitAll()
					.anyRequest().authenticated())
			.addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	CorsConfigurationSource corsConfigurationSource() {
		return request -> {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedHeaders(Collections.singletonList("*"));
			config.setAllowedMethods(Collections.singletonList("*"));
			// to-be-healthy.shop 도메인은 모두 폐기되어 geonganghaejim.site로 이전했다.
			config.setAllowedOriginPatterns(List.of(
				"http://localhost:3000",
				"https://geonganghaejim.site",
				"https://www.geonganghaejim.site",
				"https://health.junghaebom.com",
				"https://geonganghaegym.junghaebom.com"
			));
			config.setAllowCredentials(true);
			return config;
		};
	}
}
