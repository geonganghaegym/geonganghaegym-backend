package com.junghaebom.geonganghaegym.common;

import com.junghaebom.geonganghaegym.schedule.presentation.dto.out.RetrieveTrainerScheduleByLessonInfoResult.LessonDetailResult;
import com.junghaebom.geonganghaegym.schedule.domain.ReservationStatus;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class LessonDetailResultSerializer extends ValueSerializer<LessonDetailResult> {

	@Override
	public void serialize(LessonDetailResult value, JsonGenerator gen, SerializationContext serializers) {
		gen.writeStartObject();

		if (value.scheduleId() != null) {
			gen.writeNumberProperty("scheduleId", value.scheduleId());
		}
		if (value.duration() != null) {
			gen.writeNumberProperty("duration", value.duration());
		}
		gen.writeStringProperty("lessonStartTime", String.valueOf(value.lessonStartTime()));
		gen.writeStringProperty("lessonEndTime", String.valueOf(value.lessonEndTime()));
		gen.writeStringProperty("reservationStatus",
			value.reservationStatus() != null ? value.reservationStatus().name() : null);

		if (value.reservationStatus() != ReservationStatus.DISABLED) {
			gen.writePOJOProperty("applicantId", value.applicantId());
			gen.writeStringProperty("applicantName", value.applicantName());
			gen.writePOJOProperty("waitingStudentId", value.waitingStudentId());
			gen.writeStringProperty("waitingStudentName", value.waitingStudentName());
		}

		gen.writeEndObject();
	}
}
