package com.junghaebom.geonganghaegym.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LocalFileStorageConfig implements WebMvcConfigurer {

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String resourceLocation = Paths.get(uploadDir)
			.toAbsolutePath()
			.normalize()
			.toUri()
			.toString();
		if (!resourceLocation.endsWith("/")) {
			resourceLocation += "/";
		}

		registry.addResourceHandler("/files/**")
			.addResourceLocations(resourceLocation);
	}
}
