package com.ashlin.clinic.web;

import com.ashlin.clinic.model.ClinicUser;
import com.ashlin.clinic.support.ClinicException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ClinicAdvice {

	@ModelAttribute("user")
	public ClinicUser user(HttpServletRequest request) {
		return Sessions.current(request);
	}

	@ExceptionHandler(ClinicException.class)
	public String clinic(ClinicException ex, Model model) {
		model.addAttribute("title", "Cannot do that");
		model.addAttribute("error", ex.getMessage());
		return "error";
	}
}
