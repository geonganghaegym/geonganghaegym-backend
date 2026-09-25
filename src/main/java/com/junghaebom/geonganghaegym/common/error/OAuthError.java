package com.junghaebom.geonganghaegym.common.error;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OAuthError {

	@Data
	@ToString
	public static class KakaoError {
		@JsonProperty("error")
		private String error;

		@JsonProperty("error_description")
		private String errorDescription;

		@JsonProperty("error_code")
		private String errorCode;
	}

	@Data
	@ToString
	public static class NaverError {
		private String resultcode;
		private String message;
	}

	@Data
	@ToString
	public static class GoogleError {
		@JsonProperty("error")
		private String error;

		@JsonProperty("error_description")
		private String errorDescription;

	}

}
