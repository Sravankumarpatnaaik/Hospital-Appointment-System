package com.ashlin.clinic.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class ClinicCalendar {

	public static final LocalTime FIRST = LocalTime.of(9, 0);
	public static final LocalTime LAST = LocalTime.of(16, 30);

	private ClinicCalendar() {
	}

	public static boolean isOpen(LocalDate day) {
		return day.getDayOfWeek() != DayOfWeek.SUNDAY;
	}

	public static List<LocalTime> slots() {
		List<LocalTime> times = new ArrayList<>();
		add(times, LocalTime.of(9, 0), LocalTime.of(13, 0));
		add(times, LocalTime.of(14, 0), LocalTime.of(17, 0));
		return List.copyOf(times);
	}

	public static boolean isSlot(LocalTime time) {
		return slots().contains(time);
	}

	public static List<LocalDate> bookableDays(LocalDate today, LocalTime now, int count) {
		List<LocalDate> days = new ArrayList<>();
		LocalDate cursor = today;
		while (days.size() < count && !cursor.isAfter(today.plusDays(70))) {
			if (isOpen(cursor) && hasRemainingSlot(cursor, today, now)) {
				days.add(cursor);
			}
			cursor = cursor.plusDays(1);
		}
		return List.copyOf(days);
	}

	public static void requireBookable(LocalDateTime start, LocalDateTime now) {
		if (start.getSecond() != 0 || start.getNano() != 0 || !isSlot(start.toLocalTime())) {
			throw new ClinicException("That is not a clinic hour. Hours run 09:00–12:30 and 14:00–16:30.");
		}
		if (!isOpen(start.toLocalDate())) {
			throw new ClinicException("The clinic is closed on Sunday.");
		}
		if (!start.isAfter(now)) {
			throw new ClinicException("That hour has already passed.");
		}
		if (start.isAfter(now.plusDays(60))) {
			throw new ClinicException("The book is only open for the next 60 days.");
		}
	}

	private static boolean hasRemainingSlot(LocalDate day, LocalDate today, LocalTime now) {
		if (day.isAfter(today)) {
			return true;
		}
		for (LocalTime slot : slots()) {
			if (slot.isAfter(now)) {
				return true;
			}
		}
		return false;
	}

	private static void add(List<LocalTime> times, LocalTime from, LocalTime until) {
		LocalTime cursor = from;
		while (cursor.isBefore(until)) {
			times.add(cursor);
			cursor = cursor.plusMinutes(30);
		}
	}
}
