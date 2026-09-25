package com.junghaebom.geonganghaegym.common.event;

public record CustomEvent<T>(T result, EventType type) {
}