package com.junghaebom.geonganghaegym.workout.presentation.dto.in;

public record RegisterFile(
	String fileUrl,
	int fileOrder
) {

	public RegisterFile(String fileUrl) {
		this(fileUrl, 0);
	}

	public RegisterFile withFileOrder(int fileOrder) {
		return new RegisterFile(fileUrl, fileOrder);
	}
}
