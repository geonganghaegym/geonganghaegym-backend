package com.junghaebom.geonganghaegym.member.presentation.dto.out;

public record RegisterMemberProfileResult(
	String fileUrl,
	String fileName
) {

	public static RegisterMemberProfileResult from(String fileUrl, String fileName) {
		return new RegisterMemberProfileResult(fileUrl, fileName);
	}
}
