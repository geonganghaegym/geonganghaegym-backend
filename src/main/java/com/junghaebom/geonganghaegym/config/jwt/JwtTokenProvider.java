package com.junghaebom.geonganghaegym.config.jwt;

import static java.lang.String.*;

import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.junghaebom.geonganghaegym.config.security.CustomMemberDetailService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

	private final CustomMemberDetailService customMemberDetailService;

	@Value("${jwt.secret}")
	private String secretKey;

	@PostConstruct
	protected void init() {
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
	}

	public String resolveToken(HttpServletRequest request) {
		return request.getHeader("Authorization");
	}

	public Claims decode(String token) {
		return Jwts.parser()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	// Jwt 토큰으로 인증 정보를 조회
	public Authentication getAuthentication(String token) {
		Claims claims = decode(token.substring("Bearer ".length()));
		UserDetails userDetails = customMemberDetailService.loadUserByUsername(valueOf(claims.get("memberId")));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	// Jwt 토큰의 유효성 + 만료일자 확인
	public boolean validateToken(String jwtToken) {
		Jws<Claims> claims = Jwts.parser()
			.setSigningKey(secretKey)
			.build()
			.parseSignedClaims(jwtToken.substring("Bearer ".length()));
		return !claims.getBody().getExpiration().before(new Date());// 만료시간이 현재시간보다 전인지 확인
	}
}
