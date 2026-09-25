package com.junghaebom.geonganghaegym.common;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonInfoResult.LessonDetailResult;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;

public class LessonDetailResultSerializer extends JsonSerializer<LessonDetailResult> {

	@Override
	public void serialize(LessonDetailResult value, JsonGenerator gen, SerializerProvider serializers) throws
		IOException {
		gen.writeStartObject();

		if (value.scheduleId() != null) {
			gen.writeNumberField("scheduleId", value.scheduleId());
		}
		if (value.duration() != null) {
			gen.writeNumberField("duration", value.duration());
		}
		gen.writeStringField("lessonStartTime", String.valueOf(value.lessonStartTime()));
		gen.writeStringField("lessonEndTime", String.valueOf(value.lessonEndTime()));
		gen.writeStringField("reservationStatus",
			value.reservationStatus() != null ? value.reservationStatus().name() : null);

		if (value.reservationStatus() != ReservationStatus.DISABLED) {
			gen.writeObjectField("applicantId", value.applicantId());
			gen.writeStringField("applicantName", value.applicantName());
			gen.writeObjectField("waitingStudentId", value.waitingStudentId());
			gen.writeStringField("waitingStudentName", value.waitingStudentName());
		}

		gen.writeEndObject();
	}
}
