package com.junghaebom.geonganghaegym.member.presentation.dto.in;

public record CommandAppleUserInfo(ApplerUserName name, String email) {

	public record ApplerUserName(String firstName, String lastName) {}
}
