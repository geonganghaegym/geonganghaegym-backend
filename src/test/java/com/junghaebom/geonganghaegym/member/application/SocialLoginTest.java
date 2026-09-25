package com.junghaebom.geonganghaegym.member.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.config.OAuthProperties;
import com.junghaebom.geonganghaegym.config.OAuthProperties.OAuthServiceProperties;
import com.junghaebom.geonganghaegym.config.jwt.JwtTokenGenerator;
import com.junghaebom.geonganghaegym.course.application.CourseService;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.member.domain.SocialType;
import com.junghaebom.geonganghaegym.member.domain.Tokens;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandSocialLogin;
import com.junghaebom.geonganghaegym.member.repository.ComplimentaryLoginHistoryRepository;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.member.repository.NonMemberRepository;
import com.junghaebom.geonganghaegym.trainer.application.TrainerService;

import reactor.core.publisher.Mono;

/**
 * 구글/카카오 소셜 로그인이 id_token 파싱에 의존하지 않고 사용자 정보 API로 동작하는지 검증한다.
 * WebClient는 ExchangeFunction을 바꿔 끼워 실제 HTTP 호출 없이 소셜 응답을 흉내낸다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SocialLoginTest {

	private static final String GOOGLE_TOKEN_URI = "https://oauth2.googleapis.com/token";
	private static final String GOOGLE_USER_INFO_URI = "https://www.googleapis.com/oauth2/v2/userinfo";
	private static final String KAKAO_TOKEN_URI = "https://kauth.kakao.com/oauth/token";
	private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

	/**
	 * 실제 소셜 응답에는 우리가 선언하지 않은 필드가 함께 온다.
	 * 운영에서 쓰이는 WebClient 기본 코덱이 이를 무시하는지까지 확인하려면 응답 형태를 그대로 흉내내야 한다.
	 */
	private static final String GOOGLE_TOKEN_RESPONSE = """
		{"access_token":"ya29.a0AfB_test","expires_in":3599,"refresh_token":"1//0test",
		"scope":"https://www.googleapis.com/auth/userinfo.email openid","token_type":"Bearer"}
		""";

	private static final String GOOGLE_USER_INFO_V2_RESPONSE = """
		{"id":"104839201938475610293","email":"tester@gmail.com","verified_email":true,"name":"홍길동",
		"given_name":"길동","family_name":"홍",
		"picture":"https://lh3.googleusercontent.com/a/ACg8ocK-Xj_9dQm2VtRr7bZ=s96-c","locale":"ko"}
		""";

	private static final String GOOGLE_USER_INFO_V3_RESPONSE = """
		{"sub":"104839201938475610293","email":"tester@gmail.com","email_verified":true,"name":"홍길동",
		"given_name":"길동","family_name":"홍","locale":"ko"}
		""";

	private static final String KAKAO_TOKEN_RESPONSE = """
		{"access_token":"kakao-access-token","token_type":"bearer","refresh_token":"kakao-refresh-token",
		"expires_in":21599,"scope":"account_email profile_image profile_nickname",
		"refresh_token_expires_in":5183999}
		""";

	private static final String KAKAO_USER_INFO_RESPONSE = """
		{"id":123456789,"connected_at":"2026-07-01T00:00:00Z",
		"properties":{"nickname":"길동이","profile_image":"https://k.kakaocdn.net/profile.jpg",
		"thumbnail_image":"https://k.kakaocdn.net/thumb.jpg"},
		"kakao_account":{"profile_nickname_needs_agreement":false,"profile_image_needs_agreement":false,
		"profile":{"nickname":"길동이","thumbnail_image_url":"https://k.kakaocdn.net/thumb.jpg",
		"profile_image_url":"https://k.kakaocdn.net/profile.jpg","is_default_image":false,
		"is_default_nickname":false},
		"has_email":true,"email_needs_agreement":false,"is_email_valid":true,"is_email_verified":true,
		"email":"tester@kakao.com"}}
		""";

	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private JwtTokenGenerator tokenGenerator;
	@Mock
	private RedisService redisService;
	@Mock
	private TrainerService trainerService;
	@Mock
	private LocalFileStorageService fileStorageService;
	@Mock
	private MailService mailService;
	@Mock
	private CourseService courseService;
	@Mock
	private NonMemberRepository nonMemberRepository;
	@Mock
	private ComplimentaryLoginHistoryRepository complimentaryLoginHistoryRepository;
	@Mock
	private JwtDecoder appleJwtDecoder;

	private OAuthProperties oAuthProperties;

	@BeforeEach
	void setUp() {
		oAuthProperties = new OAuthProperties();
		oAuthProperties.setGoogle(serviceProperties(GOOGLE_TOKEN_URI, GOOGLE_USER_INFO_URI));
		oAuthProperties.setKakao(serviceProperties(KAKAO_TOKEN_URI, KAKAO_USER_INFO_URI));

		when(memberRepository.findByEmail(any())).thenReturn(Optional.empty());
		when(memberRepository.save(any(Member.class))).thenAnswer(it -> it.getArgument(0));
		when(tokenGenerator.create(any(Member.class))).thenAnswer(it -> {
			Member member = it.getArgument(0);
			return new Tokens(1L, member.getName(), "access-token", "refresh-token", member.getUserId(),
				member.getMemberType(), null);
		});
		when(fileStorageService.store(any(), any())).thenReturn("https://files.test/profile.png");
	}

	@Test
	@DisplayName("구글 로그인은 id_token을 디코딩하지 않고 사용자 정보 API 응답으로 회원을 만든다")
	void googleLoginUsesUserInfoApi() {
		// 토큰 응답에 id_token이 아예 없어도 로그인이 되어야 한다.
		MemberAuthCommandService service = service(Map.of(
			"oauth2.googleapis.com", GOOGLE_TOKEN_RESPONSE,
			"googleapis.com/oauth2/v2/userinfo", GOOGLE_USER_INFO_V2_RESPONSE,
			"lh3.googleusercontent.com", "profile-image-bytes"
		));

		Tokens tokens = service.getGoogleOAuth(socialLogin());

		assertEquals("홍길동", tokens.getName());
		Member saved = capturedMember();
		assertEquals("tester@gmail.com", saved.getEmail());
		assertEquals(SocialType.GOOGLE, saved.getSocialType());
		assertEquals("104839201938475610293", saved.getSocialId());
	}

	@Test
	@DisplayName("구글 사용자 정보가 v3 응답(sub)으로 와도 식별자를 저장한다")
	void googleLoginBindsSubFromV3UserInfo() {
		MemberAuthCommandService service = service(Map.of(
			"oauth2.googleapis.com", GOOGLE_TOKEN_RESPONSE,
			"googleapis.com/oauth2/v2/userinfo", GOOGLE_USER_INFO_V3_RESPONSE
		));

		service.getGoogleOAuth(socialLogin());

		assertEquals("104839201938475610293", capturedMember().getSocialId());
	}

	@Test
	@DisplayName("카카오 로그인은 OIDC id_token 없이 사용자 정보 API 응답만으로 성공한다")
	void kakaoLoginWorksWithoutIdToken() {
		MemberAuthCommandService service = service(Map.of(
			"kauth.kakao.com", KAKAO_TOKEN_RESPONSE,
			"kapi.kakao.com", KAKAO_USER_INFO_RESPONSE,
			"k.kakaocdn.net", "profile-image-bytes"
		));

		Tokens tokens = service.getKakaoAccessToken(socialLogin());

		assertEquals("길동이", tokens.getName());
		Member saved = capturedMember();
		assertEquals("tester@kakao.com", saved.getEmail());
		assertEquals(SocialType.KAKAO, saved.getSocialType());
		assertEquals("123456789", saved.getSocialId());
	}

	@Test
	@DisplayName("프로필 이미지를 받지 못해도 가입은 계속 진행된다")
	void joinSucceedsWithoutProfileImage() {
		MemberAuthCommandService service = service(Map.of(
			"oauth2.googleapis.com", "{\"access_token\":\"google-access-token\"}",
			"googleapis.com/oauth2/v2/userinfo", "{\"id\":\"1093\",\"email\":\"tester@gmail.com\",\"name\":\"홍길동\"}"
		));

		Tokens tokens = service.getGoogleOAuth(socialLogin());

		assertEquals("홍길동", tokens.getName());
		assertNull(capturedMember().getMemberProfile());
	}

	@Test
	@DisplayName("닉네임 제공에 동의하지 않으면 이메일 앞부분을 이름으로 사용한다")
	void fallsBackToEmailLocalPartWhenNicknameMissing() {
		MemberAuthCommandService service = service(Map.of(
			"kauth.kakao.com", "{\"access_token\":\"kakao-access-token\"}",
			"kapi.kakao.com", "{\"id\":123456789,\"kakao_account\":{\"email\":\"nickname-less@kakao.com\"}}"
		));

		Tokens tokens = service.getKakaoAccessToken(socialLogin());

		assertEquals("nickname-less", tokens.getName());
	}

	@Test
	@DisplayName("이메일 제공에 동의하지 않으면 원인을 알 수 있는 에러를 던진다")
	void throwsWhenEmailNotProvided() {
		MemberAuthCommandService service = service(Map.of(
			"kauth.kakao.com", "{\"access_token\":\"kakao-access-token\"}",
			"kapi.kakao.com", "{\"id\":123456789,\"kakao_account\":{\"has_email\":false}}"
		));

		CommandSocialLogin request = socialLogin();
		CustomException exception = assertThrows(CustomException.class, () -> service.getKakaoAccessToken(request));

		assertEquals("이메일 제공에 동의해야 소셜 로그인을 완료할 수 있어요.", exception.getMessage());
		verify(memberRepository, never()).save(any(Member.class));
	}

	@Test
	@DisplayName("애플 id_token 서명 검증에 실패하면 회원을 만들지 않고 에러를 던진다")
	void rejectsAppleIdTokenWithInvalidSignature() {
		MemberAuthCommandService service = service(Map.of());
		when(appleJwtDecoder.decode(any())).thenThrow(new BadJwtException("Signed JWT rejected: Invalid signature"));

		CommandSocialLogin request = appleLogin("forged.id.token");
		CustomException exception = assertThrows(CustomException.class, () -> service.getAppleOAuth(request));

		assertEquals("애플 로그인 정보가 유효하지 않습니다. 다시 시도해 주세요.", exception.getMessage());
		verify(memberRepository, never()).save(any(Member.class));
		verify(memberRepository, never()).findByUserId(any());
	}

	@Test
	@DisplayName("검증된 애플 id_token 이면 클레임의 sub 로 기존 회원을 찾아 로그인한다")
	void appleLoginUsesVerifiedClaims() {
		MemberAuthCommandService service = service(Map.of());
		when(appleJwtDecoder.decode(any())).thenReturn(appleJwt("000298.abc.1747", "tester@privaterelay.appleid.com"));

		Member existing = Member.join("000298.abc.1747", "tester@privaterelay.appleid.com", "정선우",
			MemberType.STUDENT, SocialType.APPLE, "000298.abc.1747", "refresh");
		when(memberRepository.findByUserId("000298.abc.1747")).thenReturn(Optional.of(existing));

		Tokens tokens = service.getAppleOAuth(appleLogin("valid.id.token"));

		assertEquals("정선우", tokens.getName());
		// 기존 회원이므로 애플 토큰 엔드포인트를 호출하지 않는다(스텁이 비어 있어도 통과).
		verify(memberRepository, never()).save(any(Member.class));
	}

	private static Jwt appleJwt(String sub, String email) {
		return Jwt.withTokenValue("token")
			.header("alg", "RS256")
			.header("kid", "test-kid")
			.subject(sub)
			.issuer("https://appleid.apple.com")
			.audience(List.of("com.geonganghaejim.signin"))
			.claim("email", email)
			.claim("email_verified", "true")
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plusSeconds(600))
			.build();
	}

	private static CommandSocialLogin appleLogin(String idToken) {
		return new CommandSocialLogin("apple-authorization-code", null, MemberType.STUDENT,
			"https://geonganghaejim.site/api/callback/apple", null, idToken, null);
	}

	private MemberAuthCommandService service(Map<String, String> responseByUrlKeyword) {
		return new MemberAuthCommandService(
			stubWebClient(responseByUrlKeyword),
			passwordEncoder,
			memberRepository,
			tokenGenerator,
			redisService,
			trainerService,
			new ObjectMapper(),
			oAuthProperties,
			fileStorageService,
			mailService,
			courseService,
			nonMemberRepository,
			complimentaryLoginHistoryRepository,
			appleJwtDecoder
		);
	}

	private static WebClient stubWebClient(Map<String, String> responseByUrlKeyword) {
		Map<String, String> responses = new LinkedHashMap<>(responseByUrlKeyword);

		ExchangeFunction exchangeFunction = request -> {
			String url = request.url().toString();
			String body = responses.entrySet().stream()
				.filter(entry -> url.contains(entry.getKey()))
				.map(Map.Entry::getValue)
				.findFirst()
				.orElseThrow(() -> new AssertionError("스텁에 등록되지 않은 요청입니다: " + url));

			return Mono.just(ClientResponse.create(HttpStatus.OK)
				.header("Content-Type", "application/json")
				.body(body)
				.build());
		};

		return WebClient.builder().exchangeFunction(exchangeFunction).build();
	}

	private static OAuthServiceProperties serviceProperties(String tokenUri, String userInfoUri) {
		OAuthServiceProperties properties = new OAuthServiceProperties();
		properties.setGrantType("authorization_code");
		properties.setClientId("test-client-id");
		properties.setClientSecret("test-client-secret");
		properties.setTokenUri(tokenUri);
		properties.setUserInfoUri(userInfoUri);
		return properties;
	}

	private static CommandSocialLogin socialLogin() {
		return new CommandSocialLogin("authorization-code", null, MemberType.STUDENT,
			"https://geonganghaejim.site/callback", null, null, null);
	}

	private Member capturedMember() {
		org.mockito.ArgumentCaptor<Member> captor = org.mockito.ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(captor.capture());
		return captor.getValue();
	}
}
