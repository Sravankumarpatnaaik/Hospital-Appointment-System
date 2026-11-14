package com.ashlin.clinic.service;

import com.ashlin.clinic.dao.DoctorDao;
import com.ashlin.clinic.dao.PatientDao;
import com.ashlin.clinic.support.ClinicCalendar;
import com.ashlin.clinic.support.ClinicException;
import com.ashlin.clinic.support.SlotTakenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AppointmentBookingTest {

	@Autowired AppointmentService appointments;
	@Autowired PatientDao patients;
	@Autowired DoctorDao doctors;
	@Autowired JdbcTemplate jdbc;
	@Autowired Clock clock;

	private final List<Long> patientIds = new ArrayList<>();
	private final List<Long> doctorIds = new ArrayList<>();

	@AfterEach
	void cleanup() {
		for (long id : doctorIds) {
			jdbc.update("DELETE FROM appointments WHERE doctor_id = ?", id);
			jdbc.update("DELETE FROM doctors WHERE id = ?", id);
		}
		for (long id : patientIds) {
			jdbc.update("DELETE FROM appointments WHERE patient_id = ?", id);
			jdbc.update("DELETE FROM patients WHERE id = ?", id);
		}
	}

	@Test
	void secondPatientCannotTakeTheSameDoctorHour() {
		long doctor = doctor("Cardiology");
		long first = patient();
		long second = patient();
		LocalDateTime slot = futureSlot();

		appointments.book(first, doctor, slot, "Review the last report");

		SlotTakenException rejected = assertThrows(SlotTakenException.class,
				() -> appointments.book(second, doctor, slot, "A second opinion"));
		assertTrue(rejected.getMessage().contains("already has a patient"));

		appointments.cancel(appointments.forPatient(first).get(0).id(), first);
		appointments.book(second, doctor, slot, "The hour is free again");
		assertEquals(slot, appointments.forPatient(second).get(0).startsAt());
	}

	@Test
	void aPatientCannotHoldTwoDoctorsAtOnce() {
		long heart = doctor("Cardiology");
		long skin = doctor("Dermatology");
		long person = patient();
		LocalDateTime slot = futureSlot();

		appointments.book(person, heart, slot, "Chest discomfort");
		SlotTakenException rejected = assertThrows(SlotTakenException.class,
				() -> appointments.book(person, skin, slot, "A rash as well"));
		assertTrue(rejected.getMessage().contains("already has a visit"));
	}

	@Test
	void sundayIsRefused() {
		long doctor = doctor("ENT");
		long person = patient();
		LocalDate sunday = LocalDate.now(clock);
		while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
			sunday = sunday.plusDays(1);
		}
		LocalDate closed = sunday;
		assertThrows(ClinicException.class, () -> appointments.book(person, doctor, closed.atTime(9, 0), "Sunday earache"));
	}

	private LocalDateTime futureSlot() {
		List<LocalDate> days = ClinicCalendar.bookableDays(LocalDate.now(clock), LocalTime.now(clock), 8);
		return days.get(days.size() - 1).atTime(9, 0);
	}

	private long doctor(String specialization) {
		Long specId = jdbc.queryForObject("SELECT id FROM specializations WHERE name = ?", Long.class, specialization);
		String email = "doctor-" + UUID.randomUUID() + "@ashlin.test";
		long id = doctors.insert("Test Doctor", email, "hash", specId, "9", true);
		doctorIds.add(id);
		return id;
	}

	private long patient() {
		String email = "patient-" + UUID.randomUUID() + "@ashlin.test";
		long id = patients.insert("Test Patient", email, "9000000000", LocalDate.of(1990, 5, 5), "hash", LocalDateTime.now(clock));
		patientIds.add(id);
		return id;
	}
}
