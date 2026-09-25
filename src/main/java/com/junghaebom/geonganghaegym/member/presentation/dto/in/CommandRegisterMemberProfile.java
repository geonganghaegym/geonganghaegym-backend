package com.junghaebom.geonganghaegym.member.presentation.dto.in;

import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUploadFileResult;

public record CommandRegisterMemberProfile(
	CommandUploadFileResult uploadFile
) {
}
