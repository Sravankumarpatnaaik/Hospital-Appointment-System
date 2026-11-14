package com.ashlin.clinic.web;

import com.ashlin.clinic.model.ClinicUser;
import com.ashlin.clinic.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class AuthGuardFilter extends HttpFilter {

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String path = request.getRequestURI();
		ClinicUser user = Sessions.current(request);
		if (isOpen(path)) {
			if (user != null && ("/login".equals(path) || "/register".equals(path) || "/".equals(path))) {
				response.sendRedirect(user.role().home());
				return;
			}
			chain.doFilter(request, response);
			return;
		}
		Role required = required(path);
		if (required == null) {
			chain.doFilter(request, response);
			return;
		}
		if (user == null) {
			response.sendRedirect("/login");
			return;
		}
		if (user.role() != required) {
			response.sendRedirect(user.role().home());
			return;
		}
		chain.doFilter(request, response);
	}

	private static boolean isOpen(String path) {
		return "/".equals(path)
				|| "/login".equals(path)
				|| "/register".equals(path)
				|| "/logout".equals(path)
				|| "/error".equals(path)
				|| path.startsWith("/css/")
				|| path.startsWith("/js/")
				|| "/favicon.svg".equals(path)
				|| "/favicon.ico".equals(path);
	}

	private static Role required(String path) {
		if (path.startsWith("/patient")) {
			return Role.PATIENT;
		}
		if (path.startsWith("/doctor")) {
			return Role.DOCTOR;
		}
		if (path.startsWith("/admin")) {
			return Role.ADMIN;
		}
		return null;
	}
}
