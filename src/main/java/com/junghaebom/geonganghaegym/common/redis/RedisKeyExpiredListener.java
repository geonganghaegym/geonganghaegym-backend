package com.junghaebom.geonganghaegym.common.redis;

import static com.junghaebom.geonganghaegym.common.redis.RedisKeyPrefix.*;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisKeyExpiredListener extends KeyExpirationEventMessageListener {

	private final LocalFileStorageService fileStorageService;

	public RedisKeyExpiredListener(RedisMessageListenerContainer listenerContainer,
		LocalFileStorageService fileStorageService) {
		super(listenerContainer);
		this.fileStorageService = fileStorageService;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String fileUrl = message.toString();
		if (fileUrl != null && fileUrl.startsWith(TEMP_FILE_URI.getDescription())) {
			try {
				String url = fileUrl.replace(TEMP_FILE_URI.getDescription(), "");
				String filePath = fileStorageService.extractFilePath(url);
				fileStorageService.delete(filePath);
			} catch (Exception e) {
				log.error("onMessage error => {}", e.getMessage());
			}
		}
		log.info("onMessage pattern => {} | {}", new String(pattern), message);
	}
}
