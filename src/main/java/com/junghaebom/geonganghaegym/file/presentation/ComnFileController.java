package com.junghaebom.geonganghaegym.file.presentation;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.junghaebom.geonganghaegym.ApiResult;
import com.junghaebom.geonganghaegym.config.security.CustomMemberDetails;
import com.junghaebom.geonganghaegym.file.application.ComnFileService;
import com.junghaebom.geonganghaegym.workout.presentation.dto.in.RegisterFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class ComnFileController {

	private final ComnFileService comnFileService;

	@PostMapping
	public ApiResult<List<RegisterFile>> uploadFile(
		@RequestPart("files") List<MultipartFile> files,
		@AuthenticationPrincipal CustomMemberDetails member) {
		return ApiResult.success("파일을 업로드하였습니다.", comnFileService.uploadFiles(files));
	}
}
