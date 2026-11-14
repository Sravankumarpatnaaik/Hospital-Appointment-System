package com.ashlin.clinic.support;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class When {

	public static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);
	public static final DateTimeFormatter VISIT = DateTimeFormatter.ofPattern("EEE d MMM yyyy, HH:mm", Locale.ENGLISH);
	public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
	public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
	public static final DateTimeFormatter ISO_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

	private When() {
	}
}
