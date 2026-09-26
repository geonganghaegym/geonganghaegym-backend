package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.Utils.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static io.micrometer.common.util.StringUtils.*;
import static org.springframework.http.MediaType.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.junghaebom.geonganghaegym.common.Utils;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.config.OAuthProperties;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandAssignNickname;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandChangeEmail;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandChangeMemberPassword;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandChangeName;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandLogout;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandUpdateMemo;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.OAuthInfo;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.CommandAssignNicknameResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.CommandChangeNameResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.DeleteMemberProfileResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.MemberChangeAlarmResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.RegisterMemberProfileResult;
import com.junghaebom.geonganghaegym.member.domain.AlarmStatus;
import com.junghaebom.geonganghaegym.member.domain.AlarmType;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.point.repository.PointRepository;
import com.junghaebom.geonganghaegym.push.domain.MemberToken;
import com.junghaebom.geonganghaegym.push.repository.MemberTokenRepository;
import com.junghaebom.geonganghaegym.trainer.application.TrainerService;
import com.junghaebom.geonganghaegym.trainer.domain.TrainerMemberMapping;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberCommandService {

	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;
	private final RedisService redisService;
	private final TrainerMemberMappingRepository mappingRepository;
	private final TrainerService trainerService;
	private final PointRepository pointRepository;
	private final LocalFileStorageService fileStorageService;
	private final MemberTokenRepository memberTokenRepository;
	private final WebClient webClient;
	private final OAuthProperties oAuthProperties;

	public void logout(Long memberId, CommandLogout request) {
		memberRepository.findById(memberId).ifPresent(m -> {
			redisService.deleteValues(m.getUserId());
			memberTokenRepository.deleteAll(logoutTargetTokens(m, request));
		});
	}

	// 토큰을 모르는 클라이언트(앱 웹뷰·구버전)는 기존처럼 회원의 모든 기기 토큰을 지운다
	private List<MemberToken> logoutTargetTokens(Member member, CommandLogout request) {
		if (request == null || request.fcmToken() == null || request.fcmToken().isBlank()) {
			return member.getMemberToken();
		}
		return member.getMemberToken().stream()
			.filter(memberToken -> memberToken.getToken().equals(request.fcmToken()))
			.toList();
	}

	public String deleteMember(Member loginMember) {
		Member member = memberRepository.findById(loginMember.getId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.validateModifiableAccount();

		switch (member.getSocialType()) {
			case KAKAO -> webClient.post()
				.uri("https://kapi.kakao.com/v1/user/unlink")
				.header("Authorization", "KakaoAK " + oAuthProperties.getKakao().getAdminKey())
				.body(BodyInserters.fromFormData("target_id_type", "user_id")
					.with("target_id", String.valueOf(member.getSocialId())))
				.retrieve().bodyToMono(String.class).block();

			case NAVER -> {
				MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
				request.add("client_id", oAuthProperties.getNaver().getClientId());
				request.add("client_secret", oAuthProperties.getNaver().getClientSecret());
				request.add("refresh_token", member.getSocialRefreshToken());
				request.add("grant_type", oAuthProperties.getNaver().getGrantType());

				OAuthInfo token = webClient.post()
					.uri(oAuthProperties.getNaver().getTokenUri())
					.bodyValue(request)
					.header("content-type", APPLICATION_FORM_URLENCODED_VALUE)
					.retrieve()
					.bodyToMono(OAuthInfo.class)
					.block();

				MultiValueMap<String, String> deleteToken = new LinkedMultiValueMap<>();
				deleteToken.add("client_id", oAuthProperties.getNaver().getClientId());
				deleteToken.add("client_secret", oAuthProperties.getNaver().getClientSecret());
				deleteToken.add("access_token", token.accessToken());
				deleteToken.add("grant_type", "delete");

				webClient.post()
					.uri(oAuthProperties.getNaver().getTokenUri())
					.bodyValue(deleteToken)
					.header("content-type", APPLICATION_FORM_URLENCODED_VALUE)
					.retrieve()
					.bodyToMono(String.class)
					.block();
			}
			case APPLE -> {
				MultiValueMap<String, String> revokeForm = new LinkedMultiValueMap<>();
				revokeForm.add("client_id", oAuthProperties.getApple().getClientId());
				revokeForm.add("client_secret", member.getSocialId());
				revokeForm.add("token", member.getSocialRefreshToken());
				revokeForm.add("token_type_hint", "refresh_token");

				webClient.post()
					.uri("https://appleid.apple.com/auth/revoke")
					.bodyValue(revokeForm)
					.header("content-type", APPLICATION_FORM_URLENCODED_VALUE)
					.retrieve()
					.bodyToMono(String.class)
					.block();
			}
		}
		switch (member.getMemberType()) {
			case TRAINER:
				Long trainerId = member.getId();
				List<TrainerMemberMapping> mappings = mappingRepository.findAllByTrainerId(trainerId);
				if (!mappings.isEmpty()) {
					for (TrainerMemberMapping mapping : mappings) {
						trainerService.refundStudentOfTrainer(mapping.getTrainer(), mapping.getMember().getId());
						pointRepository.deleteByMember(mapping.getMember());
						log.info("[트레이너 탈퇴] trainer: {}, deleteMapping: {}, refundMember: {}", member, mapping,
							mapping.getMember());
					}
				}
				break;

			case STUDENT:
				Long memberId = member.getId();
				Optional<TrainerMemberMapping> mappingOpt = mappingRepository.findByMemberId(memberId);
				if (mappingOpt.isPresent()) {
					TrainerMemberMapping mapping = mappingOpt.get();
					trainerService.refundStudentOfTrainer(mapping.getTrainer(), mapping.getMember().getId());
					pointRepository.deleteByMember(mapping.getMember());
				}
				log.info("[학생 탈퇴] member: {}, deleteMappings: {}, trainer: {}",
					member,
					mappingOpt.orElse(null),
					mappingOpt.<Object>map(TrainerMemberMapping::getTrainer).orElse(null));
				break;
		}
		member.deleteMember();
		return member.getUserId();
	}

	public boolean changePassword(CommandChangeMemberPassword request, Long memberId) {
		if (!request.changePassword1().equals(request.changePassword2())) {
			throw new CustomException(NOT_MATCH_PASSWORD);
		}

		if (Utils.validatePassword(request.changePassword1())) {
			throw new CustomException(PASSWORD_POLICY_VIOLATION);
		}

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.validateModifiableAccount();

		if (passwordEncoder.matches(request.changePassword1(), member.getPassword())) {
			throw new IllegalArgumentException("이전 비밀번호와 동일합니다.");
		}

		String password = passwordEncoder.encode(request.changePassword1());

		member.changePassword(password);

		return true;
	}

	public RegisterMemberProfileResult registerProfile(MultipartFile uploadFile, Long memberId) {
		Member findMember = memberRepository.findMemberById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		findMember.validateModifiableAccount();

		if (uploadFile.isEmpty()) {
			throw new IllegalArgumentException("프로필 사진을 등록해 주세요.");
		}

		String extension = uploadFile.getOriginalFilename()
			.substring(uploadFile.getOriginalFilename().lastIndexOf("."));
		String savedFileName = createFileName("origin/profile/") + extension;

		try (InputStream inputStream = uploadFile.getInputStream()) {
			String fileUrl = fileStorageService.store(savedFileName, inputStream);
			findMember.registerProfile(savedFileName, fileUrl);
			return RegisterMemberProfileResult.from(fileUrl, savedFileName);
		} catch (IOException e) {
			log.error("error", e);
			throw new CustomException(FILE_UPLOAD_ERROR);
		}
	}

	public DeleteMemberProfileResult deleteProfile(Long memberId) {
		Member findMember = memberRepository.findMemberById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		findMember.validateModifiableAccount();

		if (ObjectUtils.isEmpty(findMember.getMemberProfile())) {
			throw new IllegalArgumentException("프로필 사진이 없습니다.");
		}

		String fileUrl = findMember.getMemberProfile().getFileUrl();
		String fileName = findMember.getMemberProfile().getFileName();

		fileStorageService.delete(fileName);

		findMember.deleteProfile();

		return DeleteMemberProfileResult.from(fileUrl, fileName);
	}

	public CommandChangeNameResult changeName(CommandChangeName request, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.validateModifiableAccount();
		validateName(request.name());
		member.changeName(request.name());
		return CommandChangeNameResult.from(member);
	}

	public MemberChangeAlarmResult changeAlarm(AlarmType alarmType, AlarmStatus alarmStatus, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.changeAlarm(alarmType, alarmStatus);
		return MemberChangeAlarmResult.from(alarmType, alarmStatus);
	}

	public void updateMemo(Long trainerId, Long mmeberId, CommandUpdateMemo command) {
		memberRepository.findById(mmeberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		TrainerMemberMapping mapping = mappingRepository.findByTrainerIdAndMemberId(trainerId, mmeberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_MAPPED));
		mapping.changeMemo(command.memo());
	}

	public CommandAssignNicknameResult assignNickname(CommandAssignNickname request, Long studentId) {
		Member member = memberRepository.findById(studentId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.assignNickname(request.nickname());
		return CommandAssignNicknameResult.from(member);
	}

	public Boolean changeEmail(CommandChangeEmail request, Long memberId) {
		memberRepository.findByEmail(request.email()).ifPresent(m -> {
			throw new CustomException(MEMBER_EMAIL_DUPLICATION);
		});

		Member findMember = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		findMember.validateModifiableAccount();

		String value = redisService.getValues(request.email());

		if (isEmpty(value) || !value.equals(request.emailKey())) {
			throw new CustomException(MAIL_AUTH_CODE_NOT_VALID);
		}

		findMember.changeEmail(request.email());
		redisService.deleteValues(request.email());

		return true;
	}

	public Boolean changeScheduleNotice(AlarmStatus alarmStatus, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.changeScheduleNotice(alarmStatus);
		return true;
	}

	public Boolean changeTrainerFeedback(AlarmStatus alarmStatus, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.changeTrainerFeedback(alarmStatus);
		return true;
	}

	private void validateName(String name) {
		if (Utils.validateNameLength(name)) {
			throw new CustomException(MEMBER_NAME_LENGTH_NOT_VALID);
		}

		if (Utils.validateNameFormat(name)) {
			throw new CustomException(MEMBER_NAME_NOT_VALID);
		}
	}

	public Boolean changeDietNotice(AlarmStatus alarmStatus, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.changeDietNotice(alarmStatus);
		return true;
	}
}
