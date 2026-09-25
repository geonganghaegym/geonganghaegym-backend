package com.junghaebom.geonganghaegym.common.redis;

import static java.util.concurrent.TimeUnit.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RedisService {

	private final RedisTemplate<String, String> redisTemplate;

	public void setValues(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	// 만료시간 설정 -> 자동 삭제
	public void setValuesWithTimeout(String key, String value, long timeout) {
		redisTemplate.opsForValue().set(key, value, timeout, MILLISECONDS);
	}

	public String getValues(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public void deleteValues(String key) {
		redisTemplate.delete(key);
	}
}
