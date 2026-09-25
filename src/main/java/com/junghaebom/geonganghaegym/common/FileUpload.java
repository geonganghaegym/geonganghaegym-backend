package com.junghaebom.geonganghaegym.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileUpload {

	FILE_MAXIMUM_UPLOAD_SIZE(3),
	FILE_TEMP_UPLOAD_TIMEOUT(30 * 60 * 1000);

	private final int description;
}
