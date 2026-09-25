package com.junghaebom.geonganghaegym.file.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocalFileStorageService {

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Value("${file.base-url}")
	private String baseUrl;

	private Path rootPath;

	@PostConstruct
	public void init() {
		try {
			rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
			Files.createDirectories(rootPath);
		} catch (IOException e) {
			throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", e);
		}
	}

	public String store(String filePath, InputStream inputStream) {
		try {
			Path targetPath = rootPath.resolve(filePath).normalize();
			Files.createDirectories(targetPath.getParent());
			Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
			return getFileUrl(filePath);
		} catch (IOException e) {
			log.error("파일 저장 실패: {}", filePath, e);
			throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
		}
	}

	public void copy(String sourcePath, String targetPath) {
		try {
			Path source = rootPath.resolve(sourcePath).normalize();
			Path target = rootPath.resolve(targetPath).normalize();
			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("파일 복사 실패: {} -> {}", sourcePath, targetPath, e);
			throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
		}
	}

	public void delete(String filePath) {
		try {
			Path target = rootPath.resolve(filePath).normalize();
			Files.deleteIfExists(target);
		} catch (IOException e) {
			log.error("파일 삭제 실패: {}", filePath, e);
			throw new CustomException(ErrorCode.FILE_REMOVE_ERROR);
		}
	}

	public String getFileUrl(String filePath) {
		return baseUrl + "/files/" + filePath;
	}

	public String extractFilePath(String fileUrl) {
		if (fileUrl.contains("/files/")) {
			return fileUrl.substring(fileUrl.indexOf("/files/") + "/files/".length());
		}
		return fileUrl;
	}
}
