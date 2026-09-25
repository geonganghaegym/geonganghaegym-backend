package com.junghaebom.geonganghaegym.file.application;

import static com.junghaebom.geonganghaegym.common.Utils.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.common.error.ErrorCode;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.RegisterFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComnFileService {

	private final LocalFileStorageService fileStorageService;

	public List<RegisterFile> uploadFiles(List<MultipartFile> files) {
		List<RegisterFile> uploadedFiles = new ArrayList<>();
		int fileOrder = 0;
		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				try {
					String fileName = createFileName();
					String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
					String filePath = "temp/" + fileName + extension;
					String fileUrl = fileStorageService.store(filePath, file.getInputStream());
					uploadedFiles.add(new RegisterFile(fileUrl, ++fileOrder));
				} catch (Exception e) {
					throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
				}
			}
		}
		return uploadedFiles;
	}
}
