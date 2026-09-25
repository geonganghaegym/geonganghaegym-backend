package com.junghaebom.geonganghaegym.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomEventPublisher<T> {
	private final ApplicationEventPublisher applicationEventPublisher;

	public void publish(final T result, final EventType type) {
		log.info("Publishing generic event.");
		CustomEvent<T> genericEvent = new CustomEvent<>(result, type);
		applicationEventPublisher.publishEvent(genericEvent);
	}
}
