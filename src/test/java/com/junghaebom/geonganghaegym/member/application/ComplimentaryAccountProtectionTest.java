package com.junghaebom.geonganghaegym.member.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUploadFileResult;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.member.domain.MemberType;
import com.junghaebom.geonganghaegym.member.domain.SocialType;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandChangeEmail;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandChangeMemberPassword;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandChangeName;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandFindMemberPassword;
import com.junghaebom.geonganghaegym.member.presentation.dto.in.CommandRegisterMemberProfile;
import com.junghaebom.geonganghaegym.member.repository.MemberRepository;
import com.junghaebom.geonganghaegym.trainer.respository.TrainerMemberMappingRepository;

/**
 * 체험하기로 로그인하는 공유 계정은 탈퇴·비밀번호 변경·비밀번호 초기화가 막혀야 한다.
 * 한 사람이 바꾸면 그 뒤로 모든 사람의 체험하기가 실패하기 때문이다.
 */
@ExtendWith(MockitoExtension.class)
class ComplimentaryAccountProtectionTest {

	private static final String ORIGINAL_PASSWORD = "encoded-original";

	private static Member member(String userId) {
		return Member.builder()
			.id(1L)
			.userId(userId)
			.email(userId + "@example.com")
			.name("체험")
			.password(ORIGINAL_PASSWORD)
			.memberType(MemberType.STUDENT)
			.socialType(SocialType.NONE)
			.build();
	}

	@Nested
	@DisplayName("MemberCommandService")
	class CommandService {

		@Mock
		private MemberRepository memberRepository;
		@Mock
		private PasswordEncoder passwordEncoder;
		@Mock
		private TrainerMemberMappingRepository mappingRepository;
		@Mock
		private LocalFileStorageService fileStorageService;
		@Mock
		private RedisService redisService;
		@InjectMocks
		private MemberCommandService memberCommandService;

		@ParameterizedTest
		@ValueSource(strings = {"healthy-trainer0", "healthy-student0"})
		@DisplayName("체험 계정은 탈퇴할 수 없다")
		void deleteMember_complimentary(String userId) {
			Member demo = member(userId);
			when(memberRepository.findById(1L)).thenReturn(Optional.of(demo));

			CustomException exception = assertThrows(CustomException.class, () -> memberCommandService.deleteMember(demo));

			assertEquals(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE, exception.getErrorCode());
			assertFalse(demo.isDelYn());
		}

		@Test
		@DisplayName("일반 계정은 탈퇴된다")
		void deleteMember_normal() {
			Member normal = member("normal-user");
			when(memberRepository.findById(1L)).thenReturn(Optional.of(normal));

			memberCommandService.deleteMember(normal);

			assertTrue(normal.isDelYn());
		}

		@ParameterizedTest
		@ValueSource(strings = {"healthy-trainer0", "healthy-student0"})
		@DisplayName("체험 계정은 비밀번호를 바꿀 수 없다")
		void changePassword_complimentary(String userId) {
			Member demo = member(userId);
			when(memberRepository.findById(1L)).thenReturn(Optional.of(demo));
			CommandChangeMemberPassword request = new CommandChangeMemberPassword("newpass123", "newpass123");

			CustomException exception = assertThrows(CustomException.class,
				() -> memberCommandService.changePassword(request, 1L));

			assertEquals(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE, exception.getErrorCode());
			assertEquals(ORIGINAL_PASSWORD, demo.getPassword());
		}

		@Test
		@DisplayName("일반 계정은 비밀번호가 바뀐다")
		void changePassword_normal() {
			Member normal = member("normal-user");
			when(memberRepository.findById(1L)).thenReturn(Optional.of(normal));
			when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
			when(passwordEncoder.encode("newpass123")).thenReturn("encoded-new");

			assertTrue(memberCommandService.changePassword(
				new CommandChangeMemberPassword("newpass123", "newpass123"), 1L));

			assertEquals("encoded-new", normal.getPassword());
		}

		@ParameterizedTest
		@ValueSource(strings = {"healthy-trainer0", "healthy-student0"})
		@DisplayName("체험 계정은 프로필 사진을 등록할 수 없다")
		void registerProfile_complimentary(String userId) {
			Member demo = member(userId);
			when(memberRepository.findMemberById(1L)).thenReturn(Optional.of(demo));

			CustomException exception = assertThrows(CustomException.class,
				() -> memberCommandService.registerProfile(mock(MultipartFile.class), 1L));

			assertEquals(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE, exception.getErrorCode());
			assertNull(demo.getMemberProfile());
			verifyNoInteractions(fileStorageService);
		}

		@ParameterizedTest
		@ValueSource(strings = {"healthy-trainer0", "healthy-student0"})
		@DisplayName("체험 계정은 프로필 사진을 삭제할 수 없다")
		void deleteProfile_complimentary(String userId) {
			Member demo = member(userId);
			demo.registerProfile("origin/profile/demo.png", "https://cdn.example.com/demo.png");
			when(memberRepository.findMemberById(1L)).thenReturn(Optional.of(demo));

			CustomException exception = assertThrows(CustomException.class,
				() -> memberCommandService.deleteProfile(1L));

			assertEquals(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE, exception.getErrorCode());
			assertNotNull(demo.getMemberProfile());
			verifyNoInteractions(fileStorageService);
		}

		@ParameterizedTest
		@ValueSource(strings = {"healthy-trainer0", "healthy-student0"})
		@DisplayName("체험 계정은 이름을 바꿀 수 없다")
		void changeName_complimentary(String userId) {
			Member demo = member(userId);
			when(memberRepository.findById(1L)).thenReturn(Optional.of(demo));

			CustomException exception = assertThrows(CustomException.class,
				() -> memberCommandService.changeName(new CommandChangeName("홍길동"), 1L));

			assertEquals(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE, exception.getErrorCode());
			assertEquals("체험", demo.getName());
		}

		@Test
		@DisplayName("일반 계정은 이름이 바뀐다")
		void changeName_normal() {
			Member normal = member("normal-user");
			when(memberRepository.findById(1L)).thenReturn(Optional.of(normal));

			memberCommandService.changeName(new CommandChangeName("홍길동"), 1L);

			assertEquals("홍길동", normal.getName());
		}

		@ParameterizedTest
		@ValueSource(strings = {"healthy-trainer0", "healthy-student0"})
		@DisplayName("체험 계정은 이메일을 바꿀 수 없다")
		void changeEmail_complimentary(String userId) {
			Member demo = member(userId);
			String originalEmail = demo.getEmail();
			when(memberRepository.findById(1L)).thenReturn(Optional.of(demo));

			CustomException exception = assertThrows(CustomException.class,
				() -> memberCommandService.changeEmail(new CommandChangeEmail("new@example.com", "123456"), 1L));

			assertEquals(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE, exception.getErrorCode());
			assertEquals(originalEmail, demo.getEmail());
		}
	}

	@Nested
	@DisplayName("MemberCommandServiceV2")
	class CommandServiceV2 {

		@Mock
		private MemberRepository memberRepository;
		@Mock
		private LocalFileStorageService fileStorageService;
		@InjectMocks
		private MemberCommandServiceV2 memberCommandServiceV2;

		@ParameterizedTest
		@ValueSource(strings = {"healthy-trainer0", "healthy-student0"})
		@DisplayName("체험 계정은 V2로도 프로필 사진을 등록할 수 없다")
		void registerProfile_complimentary(String userId) {
			Member demo = member(userId);
			when(memberRepository.findMemberById(1L)).thenReturn(Optional.of(demo));
			CommandRegisterMemberProfile request = new CommandRegisterMemberProfile(
				new CommandUploadFileResult("https://x/files/temp/a.png", 1));

			CustomException exception = assertThrows(CustomException.class,
				() -> memberCommandServiceV2.registerProfile(request, 1L));

			assertEquals(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE, exception.getErrorCode());
			assertNull(demo.getMemberProfile());
			verifyNoInteractions(fileStorageService);
		}
	}

	@Nested
	@DisplayName("MemberAuthCommandService")
	class AuthCommandService {

		@Mock
		private MemberRepository memberRepository;
		@Mock
		private PasswordEncoder passwordEncoder;
		@Mock
		private MailService mailService;
		@InjectMocks
		private MemberAuthCommandService memberAuthCommandService;

		@ParameterizedTest
		@ValueSource(strings = {"healthy-trainer0", "healthy-student0"})
		@DisplayName("체험 계정은 비밀번호 찾기로 비밀번호를 초기화할 수 없다")
		void findMemberPW_complimentary(String userId) {
			Member demo = member(userId);
			when(memberRepository.findByEmailAndName(demo.getEmail(), demo.getName())).thenReturn(Optional.of(demo));

			CustomException exception = assertThrows(CustomException.class,
				() -> memberAuthCommandService.findMemberPW(
					new CommandFindMemberPassword(demo.getEmail(), demo.getName())));

			assertEquals(COMPLIMENTARY_ACCOUNT_NOT_MODIFIABLE, exception.getErrorCode());
			assertEquals(ORIGINAL_PASSWORD, demo.getPassword());
			verifyNoInteractions(mailService);
		}

		@Test
		@DisplayName("일반 계정은 비밀번호 찾기로 초기화 메일이 발송된다")
		void findMemberPW_normal() {
			Member normal = member("normal-user");
			when(memberRepository.findByEmailAndName(normal.getEmail(), normal.getName()))
				.thenReturn(Optional.of(normal));
			when(passwordEncoder.encode(anyString())).thenReturn("encoded-reset");

			memberAuthCommandService.findMemberPW(new CommandFindMemberPassword(normal.getEmail(), normal.getName()));

			assertEquals("encoded-reset", normal.getPassword());
			verify(mailService).sendResetPassword(eq(normal.getEmail()), anyString());
		}
	}
}
