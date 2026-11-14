package com.ashlin.clinic.web;

import com.ashlin.clinic.model.ApptStatus;
import com.ashlin.clinic.model.Visit;
import com.ashlin.clinic.support.When;

import java.time.LocalDateTime;

public record VisitLine(
		long id,
		String when,
		String title,
		String meta,
		String reason,
		String status,
		boolean upcoming,
		boolean booked) {

	public static VisitLine forPatient(Visit visit, LocalDateTime now) {
		return line(visit, now, visit.doctorName(), visit.specialization() + " · Room " + visit.room());
	}

	public static VisitLine forDoctor(Visit visit, LocalDateTime now) {
		return line(visit, now, visit.patientName(), visit.patientPhone());
	}

	public static VisitLine forAdmin(Visit visit, LocalDateTime now) {
		return line(visit, now, visit.patientName() + " with " + visit.doctorName(),
				visit.specialization() + " · Room " + visit.room());
	}

	private static VisitLine line(Visit visit, LocalDateTime now, String title, String meta) {
		boolean booked = visit.status() == ApptStatus.BOOKED;
		boolean upcoming = booked && visit.startsAt().isAfter(now);
		return new VisitLine(
				visit.id(),
				visit.startsAt().format(When.VISIT),
				title,
				meta,
				visit.reason(),
				booked ? "Booked" : "Cancelled",
				upcoming,
				booked);
	}
}
