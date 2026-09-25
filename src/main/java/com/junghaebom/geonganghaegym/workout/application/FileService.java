package com.junghaebom.geonganghaegym.workout.application;

import static com.junghaebom.geonganghaegym.common.Utils.*;
import static com.junghaebom.geonganghaegym.common.error.ErrorCode.*;
import static com.junghaebom.geonganghaegym.common.redis.RedisKeyPrefix.*;
import static java.util.UUID.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.redis.RedisService;
import com.junghaebom.geonganghaegym.file.application.LocalFileStorageService;
import com.junghaebom.geonganghaegym.member.domain.Member;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.RegisterFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FileService {

	private final LocalFileStorageService fileStorageService;
	private final RedisService redisService;

	public List<RegisterFile> uploadFiles(String folder, List<MultipartFile> uploadFiles, Member member) {
		List<RegisterFile> uploadFile = new ArrayList<>();
		int fileOrder = 0;
		for (MultipartFile file : uploadFiles) {
			if (!file.isEmpty()) {
				try (InputStream inputStream = file.getInputStream()) {
					String fileName = System.currentTimeMillis() + "-" + randomUUID();
					String savedFilePath = "origin/" + folder + "/" + fileName + ".jpg";
					String fileUrl = fileStorageService.store(savedFilePath, inputStream);
					redisService.setValuesWithTimeout(TEMP_FILE_URI.getDescription() + fileUrl,
						member.getId().toString(), FILE_TEMP_UPLOAD_TIMEOUT);
					uploadFile.add(new RegisterFile(fileUrl, ++fileOrder));
				} catch (Exception e) {
					log.error("error", e);
				}
			}
		}
		return uploadFile;
	}

	public RegisterFile moveDirTempToOrigin(String dir, String oldSavedFileName) {
		String tempPath = fileStorageService.extractFilePath(oldSavedFileName);
		String newFilePath = "origin/" + dir + tempPath.replaceFirst("temp/", "");
		fileStorageService.copy(tempPath, newFilePath);
		String fileUrl = fileStorageService.getFileUrl(newFilePath);
		return new RegisterFile(fileUrl);
	}

	public void deleteDietFile(String fileName) {
		try {
			fileStorageService.delete("origin/diet/" + fileName);
		} catch (Exception e) {
			log.error("error", e);
			throw new CustomException(FILE_REMOVE_ERROR);
		}
	}

	public void deleteHistoryFile(String fileName) {
		try {
			fileStorageService.delete("origin/workout-history/" + fileName);
		} catch (Exception e) {
			log.error("error", e);
			throw new CustomException(FILE_REMOVE_ERROR);
		}
	}
}
