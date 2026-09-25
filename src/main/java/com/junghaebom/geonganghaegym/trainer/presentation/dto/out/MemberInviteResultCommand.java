package com.junghaebom.geonganghaegym.trainer.presentation.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberInviteResultCommand(
	@Schema(description = "트레이너/회원정보 매핑 uuid")
	String uuid,

	@Schema(description = "초대링크")
	String invitationLink
) {
}
