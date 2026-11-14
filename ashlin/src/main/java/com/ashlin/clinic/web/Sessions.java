package com.ashlin.clinic.web;

import com.ashlin.clinic.model.ClinicUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Sessions {

	public static final String KEY = "clinicUser";

	private Sessions() {
	}

	public static ClinicUser current(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		Object value = session.getAttribute(KEY);
		return value instanceof ClinicUser user ? user : null;
	}

	public static void establish(HttpServletRequest request, ClinicUser user) {
		HttpSession existing = request.getSession(false);
		if (existing != null) {
			existing.invalidate();
		}
		request.getSession(true).setAttribute(KEY, user);
	}

	public static void clear(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
