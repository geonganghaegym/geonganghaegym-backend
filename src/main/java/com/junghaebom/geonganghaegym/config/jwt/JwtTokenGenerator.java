package com.junghaebom.geonganghaegym.config.jwt;

import static io.jsonwebtoken.SignatureAlgorithm.*;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.gym.domain.Gym;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.member.domain.Tokens;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenGenerator {
	private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh-token:";

	private final Long accessTokenValidSeconds;
	private final Long refreshTokenValidSeconds;
	private final Key key;
	private final RedisService redisService;

	public JwtTokenGenerator(@Value("${jwt.access-token-valid-seconds}") Long accessTokenValidSeconds,
		@Value("${jwt.refresh-token-valid-seconds}") Long refreshTokenValidSeconds,
		@Value("${jwt.secret}") String jwtSecret,
		RedisService redisService) {
		this.accessTokenValidSeconds = accessTokenValidSeconds;
		this.refreshTokenValidSeconds = refreshTokenValidSeconds;
		this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
		this.redisService = redisService;
	}

	private static @NotNull Map<String, Object> createClaims(Long memberId, String userId, String memberType, Gym gym) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("memberId", memberId);
		claims.put("userId", userId);
		claims.put("memberType", memberType);
		claims.put("uuid", UUID.randomUUID().toString());
		claims.put("gymId", gym != null ? gym.getId() : null);
		return claims;
	}

	public Tokens create(Member member) {
		long nowInMilliseconds = new Date().getTime();

		String accessToken = createAccessToken(member.getId(), member.getUserId(), member.getMemberType().name(),
			getAccessTokenValid(nowInMilliseconds), member.getGym());

		String refreshToken = createRefreshToken(member.getId(), member.getUserId(),
			getRefreshTokenValid(nowInMilliseconds), member.getMemberType().name(), member.getGym());

		// 기기마다 따로 저장해 같은 계정의 다른 로그인이 이 기기의 갱신 토큰을 덮어쓰지 않게 한다
		redisService.setValuesWithTimeout(refreshTokenKey(refreshToken), member.getUserId(),
			refreshTokenValidSeconds * 1000);

		return new Tokens(member.getId(), member.getName(), accessToken, refreshToken, member.getUserId(),
			member.getMemberType(), member.getGym());
	}

	public static String refreshTokenKey(String refreshToken) {
		return REFRESH_TOKEN_KEY_PREFIX + refreshToken;
	}

	private Date getRefreshTokenValid(long nowInMilliseconds) {
		return new Date(nowInMilliseconds + refreshTokenValidSeconds * 1000);
	}

	private Date getAccessTokenValid(long nowInMilliseconds) {
		return new Date(nowInMilliseconds + accessTokenValidSeconds * 1000);
	}

	public Tokens exchangeAccessToken(Long memberId, String name, String userId, MemberType memberType,
		String refreshToken, Gym gym) {
		long nowInMilliseconds = new Date().getTime();
		String changedAccessToken = createAccessToken(memberId, userId, memberType.name(),
			getAccessTokenValid(nowInMilliseconds), gym);
		return new Tokens(memberId, name, changedAccessToken, refreshToken, userId, memberType, gym);
	}

	private String createAccessToken(Long memberId, String userId, String memberType, Date expiry, Gym gym) {
		Map<String, Object> claims = createClaims(memberId, userId, memberType, gym);
		return Jwts.builder()
			.claims(claims)
			.issuedAt(new Date())
			.expiration(expiry)
			.signWith(key, HS256)
			.compact();
	}

	private String createRefreshToken(Long memberId, String userId, Date expiry, String memberType, Gym gym) {
		Map<String, Object> claims = createClaims(memberId, userId, memberType, gym);
		return Jwts.builder()
			.claims(claims)
			.issuedAt(new Date())
			.expiration(expiry)
			.signWith(key, HS256)
			.compact();
	}
}
