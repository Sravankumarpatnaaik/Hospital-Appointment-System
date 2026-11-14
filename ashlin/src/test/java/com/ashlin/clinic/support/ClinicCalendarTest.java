package com.ashlin.clinic.support;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClinicCalendarTest {

	@Test
	void hoursSkipLunchAndStopAtHalfPastFour() {
		var slots = ClinicCalendar.slots();
		assertEquals(LocalTime.of(9, 0), slots.get(0));
		assertTrue(slots.contains(LocalTime.of(12, 30)));
		assertFalse(slots.contains(LocalTime.of(13, 0)));
		assertTrue(slots.contains(LocalTime.of(14, 0)));
		assertEquals(LocalTime.of(16, 30), slots.get(slots.size() - 1));
		assertEquals(14, slots.size());
	}

	@Test
	void sundayIsClosedAndALateFridayStartsOnSaturday() {
		assertFalse(ClinicCalendar.isOpen(LocalDate.of(2026, 11, 15)));
		var days = ClinicCalendar.bookableDays(LocalDate.of(2026, 11, 13), LocalTime.of(21, 57), 3);
		assertEquals(LocalDate.of(2026, 11, 14), days.get(0));
		assertEquals(DayOfWeek.SATURDAY, days.get(0).getDayOfWeek());
		assertEquals(LocalDate.of(2026, 11, 16), days.get(1));
	}
}
