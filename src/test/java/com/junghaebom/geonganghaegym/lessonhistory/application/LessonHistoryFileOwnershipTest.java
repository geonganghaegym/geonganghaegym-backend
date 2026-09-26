package com.junghaebom.geonganghaegym.lessonhistory.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistory;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryComment;
import com.junghaebom.geonganghaegym.lessonhistory.domain.LessonHistoryFiles;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.CommandUpdateComment;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.in.CommandUpdateLessonHistory;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUpdateLessonHistoryResult;
import com.junghaebom.geonganghaegym.lessonhistory.presentation.dto.out.CommandUploadFileResult;
import com.junghaebom.geonganghaegym.lessonhistory.repository.LessonHistoryCommentRepository;
import com.junghaebom.geonganghaegym.lessonhistory.repository.LessonHistoryFilesRepository;
import com.junghaebom.geonganghaegym.lessonhistory.repository.LessonHistoryRepository;
import com.junghaebom.geonganghaegym.member.domain.Member;

/**
 * 수업일지·댓글 수정 요청에 다른 사람 파일 URL을 넣어도 저장되면 안 된다. 저장되면 글을 지울 때 그 파일이 삭제된다.
 * 수정으로 빠진 자기 파일은 디스크에서도 지워져야 한다.
 */
@ExtendWith(MockitoExtension.class)
class LessonHistoryFileOwnershipTest {

	private static final String OWN_FILE = "https://geonganghaejim.site/files/origin/lesson-history/own.jpg";
	private static final String OWN_FILE_NEW_DOMAIN =
		"https://geonganghaegym.junghaebom.com/files/origin/lesson-history/own.jpg";
	private static final String FOREIGN_FILE = "https://geonganghaegym.junghaebom.com/files/origin/profile/victim.jpg";

	@Mock
	private LessonHistoryRepository lessonHistoryRepository;
	@Mock
	private LessonHistoryFilesRepository lessonHistoryFilesRepository;
	@Mock
	private LessonHistoryCommentRepository lessonHistoryCommentRepository;
	@Spy
	private LocalFileStorageService fileStorageService = new LocalFileStorageService();
	@InjectMocks
	private LessonHistoryCommandService service;

	private static final String OTHER_OWN_FILE = "https://geonganghaejim.site/files/origin/lesson-history/other.jpg";

	@TempDir
	Path root;

	private final Member trainer = Member.builder().id(1L).build();

	@BeforeEach
	void setUp() throws IOException {
		ReflectionTestUtils.setField(fileStorageService, "uploadDir", root.toString());
		ReflectionTestUtils.setField(fileStorageService, "baseUrl", "https://x");
		fileStorageService.init();
		for (String path : List.of("origin/lesson-history/own.jpg", "origin/lesson-history/other.jpg",
			"origin/profile/victim.jpg")) {
			Files.createDirectories(root.resolve(path).getParent());
			Files.writeString(root.resolve(path), "img");
		}
	}

	private boolean exists(String path) {
		return Files.exists(root.resolve(path));
	}

	@Test
	@DisplayName("수업일지 수정: 원래 파일은 유지하고 이 글에 없던 URL은 버린다")
	void updateLessonHistory_dropsForeignUrl() {
		LessonHistory lessonHistory = LessonHistory.register("제목", "내용", null, trainer, null);
		lessonHistory.getFiles().add(new LessonHistoryFiles(OWN_FILE, 1, trainer, lessonHistory));
		when(lessonHistoryRepository.findOneLessonHistoryWithFiles(10L, 1L)).thenReturn(lessonHistory);

		List<CommandUploadFileResult> requestFiles = List.of(
			new CommandUploadFileResult(OWN_FILE, 1), new CommandUploadFileResult(FOREIGN_FILE, 2));

		CommandUpdateLessonHistoryResult result = service.updateLessonHistory(10L,
			new CommandUpdateLessonHistory("제목", "내용", requestFiles), 1L);

		assertEquals(List.of(OWN_FILE), lessonHistory.getFiles().stream().map(LessonHistoryFiles::getFileUrl).toList());
		assertEquals(1, result.files().size());
		assertTrue(exists("origin/profile/victim.jpg"));
		assertTrue(exists("origin/lesson-history/own.jpg"));
	}

	@Test
	@DisplayName("수업일지 수정: 원래 파일을 새 도메인 URL로 보내도 경로가 같으면 유지한다")
	void updateLessonHistory_keepsOwnFileAcrossDomains() {
		LessonHistory lessonHistory = LessonHistory.register("제목", "내용", null, trainer, null);
		lessonHistory.getFiles().add(new LessonHistoryFiles(OWN_FILE, 1, trainer, lessonHistory));
		when(lessonHistoryRepository.findOneLessonHistoryWithFiles(10L, 1L)).thenReturn(lessonHistory);

		service.updateLessonHistory(10L, new CommandUpdateLessonHistory(
			"제목", "내용", List.of(new CommandUploadFileResult(OWN_FILE_NEW_DOMAIN, 1))), 1L);

		assertEquals(List.of(OWN_FILE), lessonHistory.getFiles().stream().map(LessonHistoryFiles::getFileUrl).toList());
	}

	@Test
	@DisplayName("수업일지 수정: 요청에서 빠진 파일은 디스크에서도 지운다")
	void updateLessonHistory_deletesRemovedFile() {
		LessonHistory lessonHistory = LessonHistory.register("제목", "내용", null, trainer, null);
		lessonHistory.getFiles().add(new LessonHistoryFiles(OWN_FILE, 1, trainer, lessonHistory));
		lessonHistory.getFiles().add(new LessonHistoryFiles(OTHER_OWN_FILE, 2, trainer, lessonHistory));
		when(lessonHistoryRepository.findOneLessonHistoryWithFiles(10L, 1L)).thenReturn(lessonHistory);

		service.updateLessonHistory(10L, new CommandUpdateLessonHistory(
			"제목", "내용", List.of(new CommandUploadFileResult(OWN_FILE_NEW_DOMAIN, 1))), 1L);

		assertTrue(exists("origin/lesson-history/own.jpg"));
		assertFalse(exists("origin/lesson-history/other.jpg"));
	}

	@Test
	@DisplayName("댓글 수정: 파일을 전부 빼면 디스크에서도 지운다")
	void updateComment_deletesRemovedFiles() {
		LessonHistory lessonHistory = LessonHistory.register("제목", "내용", null, trainer, null);
		LessonHistoryComment comment = new LessonHistoryComment(1, "댓글", trainer, lessonHistory);
		comment.getFiles().add(new LessonHistoryFiles(OWN_FILE, 1, trainer, lessonHistory, comment));
		CustomMemberDetails writer = mock(CustomMemberDetails.class);
		when(writer.getMemberId()).thenReturn(1L);
		when(lessonHistoryCommentRepository.findLessonHistoryCommentWithFiles(20L, 1L)).thenReturn(comment);

		service.updateLessonHistoryComment(20L, new CommandUpdateComment("댓글", List.of()), writer);

		assertTrue(comment.getFiles().isEmpty());
		assertFalse(exists("origin/lesson-history/own.jpg"));
	}

	@Test
	@DisplayName("댓글 수정: 이 댓글에 없던 URL은 버린다")
	void updateComment_dropsForeignUrl() {
		LessonHistory lessonHistory = LessonHistory.register("제목", "내용", null, trainer, null);
		LessonHistoryComment comment = new LessonHistoryComment(1, "댓글", trainer, lessonHistory);
		comment.getFiles().add(new LessonHistoryFiles(OWN_FILE, 1, trainer, lessonHistory, comment));
		CustomMemberDetails writer = mock(CustomMemberDetails.class);
		when(writer.getMemberId()).thenReturn(1L);
		when(lessonHistoryCommentRepository.findLessonHistoryCommentWithFiles(20L, 1L)).thenReturn(comment);

		service.updateLessonHistoryComment(20L, new CommandUpdateComment("댓글",
			List.of(new CommandUploadFileResult(OWN_FILE, 1), new CommandUploadFileResult(FOREIGN_FILE, 2))), writer);

		assertEquals(List.of(OWN_FILE), comment.getFiles().stream().map(LessonHistoryFiles::getFileUrl).toList());
	}
}
