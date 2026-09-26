package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.Utils.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.member.domain.SocialType.*;
import static io.micrometer.common.util.StringUtils.*;
import static org.springframework.http.MediaType.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.junghaebom.geonganghaegym.common.Utils;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.OAuthError.GoogleError;
import com.junghaebom.geonganghaegym.common.error.OAuthError.KakaoError;
import com.junghaebom.geonganghaegym.common.error.OAuthError.NaverError;
import com.junghaebom.geonganghaegym.common.error.OAuthException;
import com.junghaebom.geonganghaegym.common.redis.RedisKeyPrefix;
import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.config.KeyUtil;
import com.junghaebom.geonganghaegym.config.OAuthProperties;
import com.junghaebom.geonganghaegym.config.OAuthProperties.AppleProperties;
import com.junghaebom.geonganghaegym.config.jwt.JwtTokenGenerator;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.course.presentation.dto.in.CourseAddCommand;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;
import com.junghaebom.geonganghaegym.member.domain.ComplimentaryLoginHistory;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberProfile;
import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.member.domain.NonMember;
import com.junghaebom.geonganghaegym.member.domain.SocialType;
import com.junghaebom.geonganghaegym.member.domain.Tokens;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.AppleToken;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandAppleUserInfo;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandFindMemberPassword;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandJoinMember;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandLoginMember;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandRefreshToken;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandSocialLogin;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandValidateEmail;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandVerification;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.IdToken;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.OAuthInfo;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.OAuthInfo.GoogleUserInfo;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.OAuthInfo.KakaoUserInfo;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.OAuthInfo.NaverUserInfo;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.CommandFindMemberPasswordResult;
import com.junghaebom.geonganghaegym.member.presentation.dto.out.CommandJoinMemberResult;
import com.junghaebom.geonganghaegym.member.repository.ComplimentaryLoginHistoryRepository;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.member.repository.NonMemberRepository;
import com.junghaebom.geonganghaegym.trainer.application.TrainerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberAuthCommandService {

	private final WebClient webClient;
	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;
	private final JwtTokenGenerator tokenGenerator;
	private final RedisService redisService;
	private final TrainerService trainerService;
	private final ObjectMapper objectMapper;
	private final OAuthProperties oAuthProperties;
	private final LocalFileStorageService fileStorageService;
	private final MailService mailService;
	private final CourseService courseService;
	private final NonMemberRepository nonMemberRepository;
	private final ComplimentaryLoginHistoryRepository complimentaryLoginHistoryRepository;
	private final JwtDecoder appleJwtDecoder;

	private static final String APPLE_TOKEN_URI = "https://appleid.apple.com/auth/token";

	public String sendEmailVerification(CommandValidateEmail request) {
		memberRepository.findByEmail(request.email()).ifPresent(e -> {
			throw new CustomException(MEMBER_EMAIL_DUPLICATION);
		});

		String authKey = Utils.getAuthCode(6);

		redisService.setValuesWithTimeout(request.email(), authKey, EMAIL_AUTH_TIMEOUT); // 3분

		mailService.sendAuthMail(request.email(), authKey);

		return request.email();
	}

	public Boolean verifyEmailAuthNumber(CommandVerification request) {
		String value = redisService.getValues(request.email());

		if (isEmpty(value) || !value.equals(request.emailKey())) {
			throw new CustomException(MAIL_AUTH_CODE_NOT_VALID);
		}

		redisService.deleteValues(request.email());

		return true;
	}

	public CommandJoinMemberResult joinMember(CommandJoinMember request) {
		validateName(request.name());
		validatePassword(request);
		validateDuplicationUserId(request.userId());
		validateDuplicationEmail(request.email());

		String password = passwordEncoder.encode(request.password());
		Member member = Member.join(request.userId(), request.email(), request.name(), request.memberType(),
			password);

		log.info("[회원가입] member: {}", member);
		if (StringUtils.isEmpty(request.uuid())) {
			memberRepository.save(member);
			return CommandJoinMemberResult.from(member);

		} else {
			return updateNonMemberInfo(request, password);
		}
	}

	public Tokens login(CommandLoginMember request) {
		Member member = memberRepository.findByUserId(request.userId())
			.orElseThrow(() -> new CustomException(MEMBER_LOGIN_FAILED));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new CustomException(MEMBER_LOGIN_FAILED);
		}

		if (!request.memberType().equals(member.getMemberType())) {
			throw new IllegalArgumentException(String.format("%s로 가입한 사용자입니다.", member.getTransformedMemberType()));
		}

		saveComplimentaryLoginHistory(request, member);

		return tokenGenerator.create(member);
	}

	private void saveComplimentaryLoginHistory(CommandLoginMember request, Member member) {
		if (!request.complimentaryLogin() || !member.isComplimentaryAccount()) {
			return;
		}

		complimentaryLoginHistoryRepository.save(ComplimentaryLoginHistory.create(member));
	}

	public Tokens refreshToken(CommandRefreshToken request) {
		String result = redisService.getValues(request.userId());

		if (isEmpty(result)) {
			throw new CustomException(REFRESH_TOKEN_NOT_FOUND);
		}

		if (!result.equals(request.refreshToken())) {
			throw new CustomException(REFRESH_TOKEN_NOT_VALID);
		}

		Member member = memberRepository.findByUserId(request.userId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

		return tokenGenerator.exchangeAccessToken(member.getId(),
			member.getName(),
			member.getUserId(),
			member.getMemberType(),
			request.refreshToken(),
			member.getGym());
	}

	public CommandFindMemberPasswordResult findMemberPW(CommandFindMemberPassword request) {
		Member member = memberRepository.findByEmailAndName(request.email(), request.name())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		member.validateModifiableAccount();

		if (member.getSocialType() != NONE) {
			return CommandFindMemberPasswordResult.from(
				member,
				String.format("%s은 %s 계정으로 가입되어 있습니다.", member.getEmail(), member.getSocialType().getDescription())
			);
		}

		sendResetPassword(member.getEmail(), member);

		return CommandFindMemberPasswordResult.from(
			member,
			String.format("%s으로 초기화된 비밀번호가 발송되었습니다.", member.getEmail())
		);
	}

	public CommandJoinMemberResult updateNonMemberInfo(CommandJoinMember request, String password) {
		NonMember nonMember = nonMemberRepository.findByInvitationUuid(request.uuid())
			.orElseThrow(() -> new CustomException(INVITE_LINK_NOT_FOUND));

		Member member = memberRepository.findById(nonMember.getMember().getId())
			.orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
		if (!member.getName().equals(request.name())) {
			throw new CustomException(INVITE_NAME_NOT_VALID);
		}

		member.updateNonMemberInfo(request.userId(), request.email(), request.name(), request.memberType(),
			password);
		nonMemberRepository.delete(nonMember);
		return CommandJoinMemberResult.from(member);
	}

	public Tokens getNaverAccessToken(CommandSocialLogin request) {
		OAuthInfo response = getNaverOAuthAccessToken(request.code(), request.state());
		NaverUserInfo authorization = getNaverUserInfo(response);
		Optional<Member> findMember = findSocialMember(NAVER, authorization.response().id(), request.memberType());

		if (findMember.isPresent()) {
			findMember.get().updateSocialRefreshToken(response.refreshToken());
			return tokenGenerator.create(findMember.get());
		}

		validateSocialJoinEmail(authorization.response().email());
		Member member = Member.join(
			authorization.response().email(),
			authorization.response().name(),
			request.memberType(),
			NAVER,
			authorization.response().id(),
			response.refreshToken()
		);

		MemberProfile profile = getProfile(authorization.response().profileImage(), member);
		member.setMemberProfile(profile);
		memberRepository.save(member);

		if (StringUtils.isNotEmpty(request.uuid())) {
			mappingTrainerAndStudent(member, request.uuid(), authorization.response().name(), true);
		}

		log.info("[네이버 회원가입 및 로그인] member: {}", member);
		return tokenGenerator.create(member);
	}

	public Tokens getKakaoAccessToken(CommandSocialLogin request) {
		String redirectUri = OAuthProperties.resolveRedirectUri(request.redirectUrl(), "/kakao/callback",
			oAuthProperties.getKakao().getRedirectUri());
		KakaoUserInfo response = getKakaoOAuthAccessToken(request.code(), redirectUri);
		Optional<Member> findMember = findSocialMember(KAKAO, String.valueOf(response.id()), request.memberType());

		if (findMember.isPresent()) {
			return tokenGenerator.create(findMember.get());
		}

		String email = getKakaoEmail(response);
		validateSocialJoinEmail(email);
		String name = defaultNameFrom(getKakaoNickname(response), email);
		Member member = Member.join(email, name, request.memberType(), KAKAO, String.valueOf(response.id()));
		MemberProfile profile = getProfile(getKakaoProfileImage(response), member);
		member.setMemberProfile(profile);
		memberRepository.save(member);

		if (StringUtils.isNotEmpty(request.uuid())) {
			mappingTrainerAndStudent(member, request.uuid(), name, true);
		}
		log.info("[카카오 회원가입 및 로그인] member: {}", member);
		return tokenGenerator.create(member);
	}

	@Transactional
	public Tokens getGoogleOAuth(CommandSocialLogin request) {
		String redirectUri = OAuthProperties.resolveRedirectUri(request.redirectUrl(), "/google/callback",
			oAuthProperties.getGoogle().getRedirectUri());
		OAuthInfo googleToken = getGoogleAccessToken(request.code(), redirectUri);
		GoogleUserInfo userInfo = getGoogleUserInfo(googleToken.accessToken());

		Optional<Member> findMember = findSocialMember(GOOGLE, userInfo.id(), request.memberType());

		if (findMember.isPresent()) {
			return tokenGenerator.create(findMember.get());
		}

		validateSocialJoinEmail(userInfo.email());
		String name = defaultNameFrom(userInfo.name(), userInfo.email());
		Member member = Member.join(userInfo.email(), name, request.memberType(), GOOGLE, userInfo.id());
		MemberProfile profile = getProfile(userInfo.picture(), member);
		member.setMemberProfile(profile);
		memberRepository.save(member);

		if (StringUtils.isNotEmpty(request.uuid())) {
			mappingTrainerAndStudent(member, request.uuid(), name, true);
		}
		log.info("[구글 회원가입 및 로그인] member: {}", member);
		return tokenGenerator.create(member);
	}

	public Tokens getAppleOAuth(CommandSocialLogin request) {
		IdToken userInfo = parseAppleIdToken(request.id_token());

		Optional<Member> findMember = findSocialMember(APPLE, userInfo.sub(), request.memberType());
		if (findMember.isPresent()) {
			return tokenGenerator.create(findMember.get());
		}

		validateSocialJoinEmail(userInfo.email());
		String name = extractAppleName(request.user(), userInfo.email());
		AppleToken token = requestAppleToken(request.code(), request.redirectUrl());

		Member member = Member.join(userInfo.sub(), userInfo.email(), name, request.memberType(), APPLE,
			userInfo.sub(), token.refresh_token());

		memberRepository.save(member);

		if (StringUtils.isNotEmpty(request.uuid())) {
			mappingTrainerAndStudent(member, request.uuid(), name, true);
		}

		log.info("[애플 회원가입 및 로그인] member: {}", member);

		return tokenGenerator.create(member);
	}

	/**
	 * id_token 은 클라이언트가 보내오는 값이므로 애플 공개키로 서명을 검증한다.
	 * 검증 없이 payload 의 sub 를 신뢰하면 임의로 서명한 토큰으로 타인의 세션을 발급받을 수 있다.
	 * 서명 외에 iss/aud/만료도 appleJwtDecoder 에서 함께 검증한다.
	 */
	private IdToken parseAppleIdToken(String idToken) {
		if (isEmpty(idToken)) {
			throw new CustomException(ACCESS_TOKEN_NOT_FOUND);
		}

		try {
			Jwt jwt = appleJwtDecoder.decode(idToken);

			return IdToken.ofApple(
				jwt.getSubject(),
				jwt.getClaimAsString("email"),
				jwt.getAudience() == null || jwt.getAudience().isEmpty() ? null : jwt.getAudience().get(0),
				jwt.getIssuer() == null ? null : jwt.getIssuer().toString(),
				isAppleEmailVerified(jwt)
			);
		} catch (JwtException e) {
			log.error("애플 id_token 검증에 실패했습니다.", e);
			throw new CustomException(APPLE_ID_TOKEN_NOT_VALID);
		}
	}

	/**
	 * 애플은 email_verified 를 boolean 또는 문자열("true")로 내려준다.
	 */
	private static boolean isAppleEmailVerified(Jwt jwt) {
		Object emailVerified = jwt.getClaim("email_verified");

		if (emailVerified instanceof Boolean verified) {
			return verified;
		}

		return Boolean.parseBoolean(String.valueOf(emailVerified));
	}

	/**
	 * 애플은 최초 인가 시에만 user(이름) 정보를 함께 내려준다.
	 * 재로그인이나 최초 가입 실패 후 재시도에는 없으므로 이메일에서 이름을 만들어 사용한다.
	 */
	private static String extractAppleName(CommandAppleUserInfo user, String email) {
		if (user != null && user.name() != null) {
			String fullName = StringUtils.defaultString(user.name().lastName())
				+ StringUtils.defaultString(user.name().firstName());
			if (StringUtils.isNotBlank(fullName)) {
				return fullName;
			}
		}

		return defaultNameFrom(null, email);
	}

	private AppleToken requestAppleToken(String code, String requestedRedirectUri) {
		if (isEmpty(code)) {
			throw new CustomException(ACCESS_TOKEN_NOT_FOUND);
		}

		AppleProperties apple = oAuthProperties.getApple();
		// authorize 요청의 redirect_uri 와 같아야 하므로 로그인을 시작한 호스트의 콜백을 쓴다.
		String redirectUri = OAuthProperties.resolveRedirectUri(requestedRedirectUri, "/api/callback/apple",
			apple.getRedirectUri());

		if (isEmpty(redirectUri) || isEmpty(apple.getClientId())) {
			log.error("애플 로그인 설정이 비어 있습니다. clientId: {}, redirectUri: {}",
				apple.getClientId(), redirectUri);
			throw new CustomException(APPLE_CONNECTION_ERROR);
		}

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("client_id", apple.getClientId());
		form.add("client_secret", createClientSecret());
		form.add("code", code.split("&")[0]);
		form.add("grant_type", "authorization_code");
		form.add("redirect_uri", redirectUri);

		AppleToken token = webClient.post()
			.uri(APPLE_TOKEN_URI)
			.contentType(APPLICATION_FORM_URLENCODED)
			.bodyValue(form)
			.retrieve()
			.onStatus(HttpStatusCode::isError, response ->
				response.bodyToMono(String.class).flatMap(error -> {
					log.error("Apple token error => {}", error);
					return Mono.error(new OAuthException("애플 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."));
				}))
			.bodyToMono(AppleToken.class)
			.block();

		if (token == null) {
			throw new CustomException(APPLE_CONNECTION_ERROR);
		}

		return token;
	}

	public String createClientSecret() {
		AppleProperties apple = oAuthProperties.getApple();
		Date now = new Date();

		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
			.issuer(apple.getTeamId())
			.issueTime(now)
			.expirationTime(new Date(now.getTime() + 3600000))
			.audience("https://appleid.apple.com")
			.subject(apple.getClientId())
			.build();

		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(apple.getLoginKey()).build();

		SignedJWT signedJwt = new SignedJWT(header, claimsSet);

		try {
			ECPrivateKey ecPrivateKey = loadPrivateKey(apple.getKeyPath());
			JWSSigner signer = new ECDSASigner(ecPrivateKey);
			signedJwt.sign(signer);
		} catch (InvalidKeySpecException | IOException | JOSEException e) {
			log.error("Apple client secret 생성 실패. keyPath: {}", apple.getKeyPath(), e);
			throw new CustomException(APPLE_CONNECTION_ERROR);
		}

		return signedJwt.serialize();
	}

	private ECPrivateKey loadPrivateKey(String keyPath) throws IOException, InvalidKeySpecException {
		File file = new File(keyPath);

		if (!file.exists()) {
			throw new FileNotFoundException("File not found: " + keyPath);
		}

		try (PemReader pemReader = new PemReader(new FileReader(file))) {
			PemObject pemObject = pemReader.readPemObject();
			byte[] content = pemObject.getContent();
			return KeyUtil.getPrivateKeyFromBytes(content);
		}
	}

	/**
	 * 소셜 회원은 제공자가 주는 사용자 id로 식별한다.
	 * 이메일은 사용자가 제공자 쪽에서 바꿀 수 있어(네이버 연락처 이메일 등) 식별에 쓰면 같은 계정이 새 회원으로 갈라진다.
	 */
	private Optional<Member> findSocialMember(SocialType socialType, String socialId, MemberType memberType) {
		Optional<Member> member = memberRepository.findBySocialTypeAndSocialId(socialType, socialId);
		member.ifPresent(m -> {
			if (!m.getMemberType().equals(memberType)) {
				throw new IllegalArgumentException(String.format("%s로 가입한 사용자입니다.", m.getTransformedMemberType()));
			}
		});
		return member;
	}

	/**
	 * 신규 소셜 가입 시에만 이메일을 본다. 같은 이메일의 회원이 있으면 계정을 연동하지 않고 가입 경로를 안내한다.
	 */
	private void validateSocialJoinEmail(String email) {
		if (isEmpty(email)) {
			throw new CustomException(SOCIAL_EMAIL_NOT_PROVIDED);
		}
		memberRepository.findByEmail(email).ifPresent(m -> {
			if (m.getSocialType().equals(NONE)) {
				throw new IllegalArgumentException("이미 같은 이메일로 가입된 계정이 있습니다. 기존 방식으로 로그인해 주세요.");
			}
			throw new IllegalArgumentException(
				String.format("이미 %s로 가입된 이메일입니다. %s 로그인을 이용해 주세요.", m.getSocialType().getDescription(),
					m.getSocialType().getDescription()));
		});
	}

	/**
	 * 카카오는 앱에 OIDC를 켜고 인가 요청에 openid scope가 있을 때만 id_token을 내려준다.
	 * 회원 정보는 사용자 정보 API로도 모두 얻을 수 있으므로 id_token에 의존하지 않는다.
	 */
	public KakaoUserInfo getKakaoOAuthAccessToken(String code, String redirectUrl) {
		OAuthInfo tokenInfo = requestAccessToken(code, redirectUrl);
		return requestKakaoUserInfo(tokenInfo.accessToken());
	}

	private static String getKakaoEmail(KakaoUserInfo userInfo) {
		return userInfo.kakaoAccount() == null ? null : userInfo.kakaoAccount().email();
	}

	private static String getKakaoNickname(KakaoUserInfo userInfo) {
		if (userInfo.properties() != null && StringUtils.isNotBlank(userInfo.properties().nickname())) {
			return userInfo.properties().nickname();
		}

		if (userInfo.kakaoAccount() != null && userInfo.kakaoAccount().profile() != null) {
			return userInfo.kakaoAccount().profile().nickname();
		}

		return null;
	}

	private static String getKakaoProfileImage(KakaoUserInfo userInfo) {
		if (userInfo.properties() != null && StringUtils.isNotBlank(userInfo.properties().profileImage())) {
			return userInfo.properties().profileImage();
		}

		if (userInfo.kakaoAccount() != null && userInfo.kakaoAccount().profile() != null) {
			return userInfo.kakaoAccount().profile().profileImageUrl();
		}

		return null;
	}

	/**
	 * 소셜에서 닉네임 제공에 동의하지 않으면 이름이 비어서 오기 때문에 이메일 앞부분으로 대체한다.
	 */
	private static String defaultNameFrom(String name, String email) {
		if (StringUtils.isNotBlank(name)) {
			return name;
		}

		if (StringUtils.isNotBlank(email) && email.contains("@")) {
			return email.substring(0, email.indexOf("@"));
		}

		return "회원";
	}

	private OAuthInfo requestAccessToken(String code, String redirectUrl) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", oAuthProperties.getKakao().getGrantType());
		form.add("client_id", oAuthProperties.getKakao().getClientId());
		form.add("redirect_uri", redirectUrl);
		form.add("code", code);
		form.add("client_secret", oAuthProperties.getKakao().getClientSecret());

		return webClient.post()
			.uri(oAuthProperties.getKakao().getTokenUri())
			.contentType(APPLICATION_FORM_URLENCODED)
			.bodyValue(form)
			.retrieve()
			.onStatus(HttpStatusCode::isError, response ->
				response.bodyToMono(KakaoError.class).flatMap(error -> {
					log.warn("Kakao token error: {}", error);
					return Mono.error(new OAuthException(error.getErrorDescription()));
				}))
			.bodyToMono(OAuthInfo.class)
			.block();
	}

	private KakaoUserInfo requestKakaoUserInfo(String accessToken) {
		KakaoUserInfo userInfo = webClient.get()
			.uri(oAuthProperties.getKakao().getUserInfoUri())
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.onStatus(HttpStatusCode::isError, response ->
				response.bodyToMono(String.class).flatMap(error -> {
					log.error("Kakao user info error => {}", error);
					return Mono.error(new CustomException(KAKAO_CONNECTION_ERROR));
				}))
			.bodyToMono(OAuthInfo.KakaoUserInfo.class)
			.block();

		if (userInfo == null) {
			throw new CustomException(KAKAO_CONNECTION_ERROR);
		}

		return userInfo;
	}

	private OAuthInfo getGoogleAccessToken(String code, String redirectUri) {
		String decode = URLDecoder.decode(code, StandardCharsets.UTF_8);
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("client_id", oAuthProperties.getGoogle().getClientId());
		requestBody.add("client_secret", oAuthProperties.getGoogle().getClientSecret());
		requestBody.add("grant_type", oAuthProperties.getGoogle().getGrantType());
		requestBody.add("redirect_uri", redirectUri);
		requestBody.add("code", decode);

		OAuthInfo tokenInfo = webClient.post()
			.uri(oAuthProperties.getGoogle().getTokenUri())
			.contentType(APPLICATION_FORM_URLENCODED)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(requestBody)
			.retrieve()
			.onStatus(HttpStatusCode::isError, response ->
				response.bodyToMono(GoogleError.class).flatMap(e -> {
					log.error("Google token error => {}", e);
					return Mono.error(new OAuthException(e.getErrorDescription()));
				}))
			.bodyToMono(OAuthInfo.class)
			.block();

		if (tokenInfo == null || isEmpty(tokenInfo.accessToken())) {
			throw new CustomException(GOOGLE_CONNECTION_ERROR);
		}

		return tokenInfo;
	}

	/**
	 * 프론트엔드 authorize 요청 scope가 `email profile`(openid 없음)이므로 v2 엔드포인트여야 한다.
	 * v3·OIDC userinfo는 access token에 openid scope를 요구해 401이 된다.
	 */
	private GoogleUserInfo getGoogleUserInfo(String accessToken) {
		String userInfoUri = oAuthProperties.getGoogle().getUserInfoUri();

		GoogleUserInfo userInfo = webClient.get()
			.uri(userInfoUri)
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.onStatus(HttpStatusCode::isError, response ->
				response.bodyToMono(String.class).flatMap(error -> {
					log.error("Google user info error. uri: {}, response: {}", userInfoUri, error);
					return Mono.error(new CustomException(GOOGLE_CONNECTION_ERROR));
				}))
			.bodyToMono(GoogleUserInfo.class)
			.block();

		if (userInfo == null) {
			throw new CustomException(GOOGLE_CONNECTION_ERROR);
		}

		return userInfo;
	}

	public void mappingTrainerAndStudent(Member member, String uuid, String reqName, boolean isSocial) {
		Map<String, String> map = getInviteMappingData(uuid);
		String name = map.get("name");
		if (!isSocial && !name.equals(reqName)) {
			throw new CustomException(INVITE_NAME_NOT_VALID);
		}
		member.changeName(name);

		Long trainerId = Long.valueOf(map.get("trainerId"));
		trainerService.mappingMemberAndTrainer(trainerId, member.getId());

		int lessonCnt = Integer.parseInt(map.get("lessonCnt"));
		courseService.addCourse(trainerId, CourseAddCommand.create(member.getId(), lessonCnt));

		String invitationKey = RedisKeyPrefix.INVITATION.getDescription() + uuid;
		redisService.deleteValues(invitationKey);
	}

	private Map<String, String> getInviteMappingData(String uuid) {
		String invitationKey = RedisKeyPrefix.INVITATION.getDescription() + uuid;
		String mappedData = redisService.getValues(invitationKey);
		if (isEmpty(mappedData)) {
			throw new CustomException(INVITE_LINK_NOT_FOUND);
		}
		HashMap<String, String> map = new HashMap<>();
		try {
			map = objectMapper.readValue(mappedData, HashMap.class);
		} catch (JacksonException e) {
			log.error("error", e);
		}
		return map;
	}

	private void validatePassword(CommandJoinMember request) {
		if (!request.password().equals(request.passwordConfirm())) {
			throw new CustomException(CONFIRM_PASSWORD_NOT_MATCHED);
		}
		if (Utils.validatePassword(request.password())) {
			throw new CustomException(PASSWORD_POLICY_VIOLATION);
		}
	}

	private void validateName(String name) {
		if (Utils.validateNameLength(name)) {
			throw new CustomException(MEMBER_NAME_LENGTH_NOT_VALID);
		}

		if (Utils.validateNameFormat(name)) {
			throw new CustomException(MEMBER_NAME_NOT_VALID);
		}
	}

	private byte[] getProfileImage(String imageName) {
		try {
			return webClient.get()
				.uri(new URI(imageName))
				.retrieve()
				.onStatus(HttpStatusCode::isError, response ->
					response.bodyToMono(String.class).flatMap(error -> {
						log.error("error => {}", error);
						return Mono.error(new CustomException(PROFILE_ACCESS_FAILED));
					}))
				.bodyToMono(byte[].class)
				.share()
				.block();
		} catch (URISyntaxException e) {
			log.error("Invalid URL syntax: {}", imageName);
			throw new CustomException(PROFILE_ACCESS_FAILED);
		}
	}

	/**
	 * 프로필 이미지 제공에 동의하지 않으면 소셜에서 내려주지 않는다.
	 * 가입에 필수인 정보가 아니므로 값이 없거나 내려받기에 실패해도 가입은 계속 진행한다.
	 */
	private MemberProfile getProfile(String profileImage, Member member) {
		if (isEmpty(profileImage)) {
			return null;
		}

		try {
			byte[] image = getProfileImage(profileImage);
			String savedFileName = createProfileName("origin/profile/");
			try (InputStream inputStream = new ByteArrayInputStream(image)) {
				String fileUrl = fileStorageService.store(savedFileName, inputStream);
				return MemberProfile.create(savedFileName, fileUrl, member);
			}
		} catch (Exception e) {
			log.warn("소셜 프로필 이미지 저장에 실패했습니다. url: {}", profileImage, e);
			return null;
		}
	}

	private NaverUserInfo getNaverUserInfo(OAuthInfo oAuthInfo) {
		return webClient.get()
			.uri(oAuthProperties.getNaver().getUserInfoUri())
			.header("Authorization", "Bearer " + oAuthInfo.accessToken())
			.retrieve()
			.onStatus(HttpStatusCode::isError, response ->
				response.bodyToMono(NaverError.class).flatMap(e -> {
					log.error("error => {}", e);
					return Mono.error(new OAuthException(e.getMessage()));
				}))
			.bodyToMono(NaverUserInfo.class)
			.share()
			.block();
	}

	private OAuthInfo getNaverOAuthAccessToken(String code, String state) {
		MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add("grant_type", oAuthProperties.getNaver().getGrantType());
		request.add("client_id", oAuthProperties.getNaver().getClientId());
		request.add("client_secret", oAuthProperties.getNaver().getClientSecret());
		request.add("code", code);
		request.add("state", state);
		OAuthInfo tokenInfo = webClient.post()
			.uri(oAuthProperties.getNaver().getTokenUri())
			.bodyValue(request)
			.headers(header -> header.setContentType(APPLICATION_FORM_URLENCODED))
			.retrieve()
			.bodyToMono(OAuthInfo.class)
			.share()
			.block();

		// 네이버는 인가 코드가 잘못돼도 200으로 error 본문을 내려준다.
		if (tokenInfo == null || isEmpty(tokenInfo.accessToken())) {
			log.error("네이버 토큰 발급에 실패했습니다. response: {}", tokenInfo);
			throw new CustomException(NAVER_CONNECTION_ERROR);
		}
		return tokenInfo;
	}

	private void validateDuplicationUserId(String userId) {
		if (Pattern.matches("^[가-힣]+$", userId)) {
			throw new CustomException(USERID_POLICY_VIOLATION);
		}
		memberRepository.findByUserId(userId).ifPresent(m -> {
			throw new CustomException(MEMBER_ID_DUPLICATION);
		});
	}

	private void validateDuplicationEmail(String email) {
		memberRepository.findByEmail(email).ifPresent(m -> {
			throw new CustomException(MEMBER_EMAIL_DUPLICATION);
		});
	}

	private void sendResetPassword(String email, Member member) {
		String resetPW = RandomStringUtils.random(12, true, true);
		member.resetPassword(passwordEncoder.encode(resetPW));
		mailService.sendResetPassword(email, resetPW);
	}
}
