package com.junghaebom.geonganghaegym.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

	FEEDBACK("피드백", "%d명의 회원님에 대한 수업 피드백을 작성해 주세요!"),
	RESERVE("예약완료", "%s 트레이너가 %s님을 %s 예약에 등록했어요."),
	CANCEL("예약취소", "%s 트레이너가 %s님의 %s 예약을 취소했어요."),
	WRITE("게시글 작성", "트레이너가 새로운 수업일지를 작성하였습니다."),
	COMMENT("게시글 댓글", "내 게시글에 새로운 댓글이 달렸어요."),
	REPLY("댓글 답글", "내 댓글에 새로운 답글이 달렸어요."),
	WAITING("대기 예약 확정", "%s 대기 중이던 예약이 확정되었어요!"),
	WAITING_FOR_TRAINER("대기 예약 확정", "%s님이 %s 대기 중이던 예약이 확정되었어요!");

	private final String description;
	private final String content;
}
