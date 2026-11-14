package com.ashlin.clinic.model;

import java.time.LocalDateTime;

public record Visit(
		long id,
		long patientId,
		long doctorId,
		LocalDateTime startsAt,
		String reason,
		ApptStatus status,
		String patientName,
		String patientPhone,
		String doctorName,
		String specialization,
		String room) {
}
