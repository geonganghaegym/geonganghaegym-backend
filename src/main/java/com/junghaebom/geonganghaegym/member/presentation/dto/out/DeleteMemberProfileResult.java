package com.junghaebom.geonganghaegym.member.presentation.dto.out;

public record DeleteMemberProfileResult(
	String fileUrl,
	String fileName
) {

	public static DeleteMemberProfileResult from(String fileUrl, String fileName) {
		return new DeleteMemberProfileResult(fileUrl, fileName);
	}
}
