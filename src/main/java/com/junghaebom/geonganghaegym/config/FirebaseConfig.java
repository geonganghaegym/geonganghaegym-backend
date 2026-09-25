package com.junghaebom.geonganghaegym.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

	@Value("${firebase.admin-sdk.file:}")
	private String firebaseAdminsdkFile;

	@Bean
	public FirebaseApp initFirebase() throws IOException {
		if (firebaseAdminsdkFile.isBlank() || !new File(firebaseAdminsdkFile).exists()) {
			log.warn("Firebase Admin SDK 파일이 설정되지 않았거나 존재하지 않습니다. Firebase 초기화를 건너뜁니다.");
			return null;
		}
		try (FileInputStream fis = new FileInputStream(firebaseAdminsdkFile)) {
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(fis))
				.build();
			if (FirebaseApp.getApps().isEmpty()) {
				return FirebaseApp.initializeApp(options);
			}
			return FirebaseApp.getInstance();
		}
	}
}
