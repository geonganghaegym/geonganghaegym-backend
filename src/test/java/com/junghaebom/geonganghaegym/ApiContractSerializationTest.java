package com.junghaebom.geonganghaegym;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.junghaebom.geonganghaegym.common.KotlinCustomPaging;
import com.junghaebom.geonganghaegym.common.error.CustomException;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.CommandNotificationStatusResult;
import com.junghaebom.geonganghaegym.notification.presentation.dto.out.RetrieveNotificationWithRedDotResult;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.CommandRegisterSchedule;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.in.RetrieveTrainerScheduleByLessonInfo;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ApiContractSerializationTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void apiResultResponseDefaultsStatusToOk() {
		ApiResult<String> response = new ApiResult<>("ok", "payload");

		assertEquals(org.springframework.http.HttpStatus.OK, response.getStatus());
		assertEquals("ok", response.getMessage());
		assertEquals("payload", response.getData());
	}

	@Test
	void kotlinCustomPagingKeepsIsLastPropertyName() throws Exception {
		KotlinCustomPaging<String> paging = new KotlinCustomPaging<>(List.of("item"), 0, 10, 1, 1L, true);

		JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(paging));

		assertTrue(json.has("isLast"));
		assertEquals(true, json.get("isLast").booleanValue());
		assertFalse(json.has("last"));
	}

	@Test
	void notificationDtosKeepIsReadPropertyName() throws Exception {
		CommandNotificationStatusResult statusResult = new CommandNotificationStatusResult(1L, true);

		RetrieveNotificationWithRedDotResult.RetrieveNotificationResult notificationResult =
			new RetrieveNotificationWithRedDotResult.RetrieveNotificationResult(
				2L, "SCHEDULE-FEEDBACK", null, null, "title", "content", "2026-03-28T00:00:00", true, null, null, null
			);

		JsonNode statusJson = objectMapper.readTree(objectMapper.writeValueAsString(statusResult));
		JsonNode notificationJson = objectMapper.readTree(objectMapper.writeValueAsString(notificationResult));

		assertTrue(statusJson.has("isRead"));
		assertEquals(true, statusJson.get("isRead").booleanValue());
		assertFalse(statusJson.has("read"));

		assertTrue(notificationJson.has("isRead"));
		assertEquals(true, notificationJson.get("isRead").booleanValue());
		assertFalse(notificationJson.has("read"));
	}

	@Test
	void scheduleRequestKeepsConstructorValidation() {
		CustomException exception = assertThrows(
			CustomException.class,
			() -> new CommandRegisterSchedule(
				LocalDate.of(2026, 3, 30),
				LocalDate.of(2026, 3, 29)
			)
		);

		assertEquals("수업 시작일은 종료일보다 빨라야 합니다.", exception.getMessage());
	}

	@Test
	void scheduleSearchRequestKeepsDefaultMonthWhenEmpty() {
		RetrieveTrainerScheduleByLessonInfo request = new RetrieveTrainerScheduleByLessonInfo();

		assertEquals(LocalDate.now().withDayOfMonth(1).toString().substring(0, 7), request.lessonDt());
		assertNull(request.lessonStartDt());
		assertNull(request.lessonEndDt());
	}
}
