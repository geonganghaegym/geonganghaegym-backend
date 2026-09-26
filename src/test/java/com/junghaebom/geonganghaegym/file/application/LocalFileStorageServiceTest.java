package com.junghaebom.geonganghaegym.file.application;

import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.junghaebom.geonganghaegym.common.error.CustomException;

/**
 * 파일 경로는 클라이언트가 보낸 URL에서 뽑아 쓰는 곳이 많다. 업로드 루트 밖이나 temp/ 밖을 가리키는 경로는 막혀야 한다.
 */
class LocalFileStorageServiceTest {

	@TempDir
	Path base;

	private Path root;
	private Path outside;
	private LocalFileStorageService service;

	@BeforeEach
	void setUp() throws IOException {
		root = Files.createDirectories(base.resolve("uploads"));
		outside = Files.writeString(base.resolve("secret.txt"), "secret");
		Files.createDirectories(root.resolve("temp"));
		Files.writeString(root.resolve("temp/a.png"), "img");
		Files.createDirectories(root.resolve("origin/profile"));
		Files.writeString(root.resolve("origin/profile/other.png"), "other");

		service = new LocalFileStorageService();
		ReflectionTestUtils.setField(service, "uploadDir", root.toString());
		ReflectionTestUtils.setField(service, "baseUrl", "https://x");
		service.init();
	}

	@Test
	@DisplayName("temp 파일은 origin으로 복사된다")
	void copy_temp() {
		service.copy("temp/a.png", "origin/profile/a.png");

		assertTrue(Files.exists(root.resolve("origin/profile/a.png")));
	}

	@ParameterizedTest
	@ValueSource(strings = {"temp/../../secret.txt", "../secret.txt", "origin/profile/other.png"})
	@DisplayName("temp 밖 파일은 복사 원본이 될 수 없다")
	void copy_rejectsSourceOutsideTemp(String source) {
		CustomException exception = assertThrows(CustomException.class,
			() -> service.copy(source, "origin/profile/b.png"));

		assertEquals(FILE_PATH_NOT_VALID, exception.getErrorCode());
		assertFalse(Files.exists(root.resolve("origin/profile/b.png")));
	}

	@Test
	@DisplayName("복사 대상도 루트 밖이면 거절한다")
	void copy_rejectsTargetOutsideRoot() {
		assertThrows(CustomException.class, () -> service.copy("temp/a.png", "../copied.png"));

		assertFalse(Files.exists(base.resolve("copied.png")));
	}

	@Test
	@DisplayName("루트 밖 파일은 삭제할 수 없다")
	void delete_rejectsOutsideRoot() {
		CustomException exception = assertThrows(CustomException.class, () -> service.delete("temp/../../secret.txt"));

		assertEquals(FILE_PATH_NOT_VALID, exception.getErrorCode());
		assertTrue(Files.exists(outside));
	}

	@Test
	@DisplayName("루트 자체는 삭제 대상이 아니다")
	void delete_rejectsRoot() {
		assertThrows(CustomException.class, () -> service.delete("temp/.."));
	}
}
