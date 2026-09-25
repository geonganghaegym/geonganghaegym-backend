package com.junghaebom.geonganghaegym.file.presentation.dto.in;

import java.util.List;

public record CommandUploadFile(
	List<String> fileNames
) {
}
