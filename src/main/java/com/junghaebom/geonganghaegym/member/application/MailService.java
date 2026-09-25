package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.member.domain.Member;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
	private final JavaMailSender mailSender;

	@Async
	public void sendAuthMail(String email, String authKey) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
			mimeMessageHelper.setTo(email);
			mimeMessageHelper.setSubject("안녕하세요. 건강해짐 회원가입 인증번호입니다."); // 메일 제목
			String text = String.format("안녕하세요. 건강해짐 인증번호는 %s 입니다. \n확인후 입력해 주세요.", authKey);
			mimeMessageHelper.setText(text, false); // 메일 본문 내용, HTML 여부
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new CustomException(MAIL_SEND_ERROR);
		}
	}

	@Async
	public void sendResetPassword(String email, String resetPW) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
			mimeMessageHelper.setTo(email);
			mimeMessageHelper.setSubject("안녕하세요. 건강해짐 초기화 비밀번호입니다."); // 메일 제목
			String text = String.format("안녕하세요. 건강해짐 초기화 비밀번호는 %s 입니다. \n로그인 후 반드시 비밀번호를 변경해 주세요.", resetPW);
			mimeMessageHelper.setText(text, false); // 메일 본문 내용, HTML 여부
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new CustomException(MAIL_SEND_ERROR);
		}
	}

	@Async
	public void sendInviteLink(String email, Member trainer, String invitationLink) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
			mimeMessageHelper.setTo(email);
			mimeMessageHelper.setSubject(
				"[건강해짐] 안녕하세요. {trainerName}님이 고객님을 초대했습니다.".replace("{trainerName}", trainer.getName())); // 메일 제목
			String text = "안녕하세요. {trainerName}님이 고객님을 초대했습니다.\n하단 링크를 통해 회원가입을 해주세요.\n{inviteLink}"
				.replace("{trainerName}", trainer.getName())
				.replace("{inviteLink}", invitationLink);
			mimeMessageHelper.setText(text, false); // 메일 본문 내용, HTML 여부
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			log.error("error", e);
			throw new CustomException(MAIL_SEND_ERROR);
		}
	}

}