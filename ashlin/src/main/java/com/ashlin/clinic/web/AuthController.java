package com.ashlin.clinic.web;

import com.ashlin.clinic.service.AuthService;
import com.ashlin.clinic.support.ClinicException;
import com.ashlin.clinic.web.form.LoginForm;
import com.ashlin.clinic.web.form.RegisterForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

	private final AuthService auth;

	public AuthController(AuthService auth) {
		this.auth = auth;
	}

	@GetMapping("/")
	public String root() {
		return "redirect:/login";
	}

	@GetMapping("/login")
	public String loginForm(Model model) {
		model.addAttribute("title", "Sign in");
		model.addAttribute("form", new LoginForm());
		return "login";
	}

	@PostMapping("/login")
	public String login(@Valid @ModelAttribute("form") LoginForm form, BindingResult errors,
			HttpServletRequest request, Model model) {
		model.addAttribute("title", "Sign in");
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getFieldError().getDefaultMessage());
			return "login";
		}
		try {
			var user = auth.login(form.getEmail(), form.getPassword());
			Sessions.establish(request, user);
			return "redirect:" + user.role().home();
		} catch (ClinicException ex) {
			model.addAttribute("error", ex.getMessage());
			return "login";
		}
	}

	@GetMapping("/register")
	public String registerForm(Model model) {
		model.addAttribute("title", "Create a patient account");
		model.addAttribute("form", new RegisterForm());
		return "register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult errors,
			HttpServletRequest request, Model model) {
		model.addAttribute("title", "Create a patient account");
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getFieldError().getDefaultMessage());
			return "register";
		}
		try {
			var user = auth.register(form);
			Sessions.establish(request, user);
			return "redirect:/patient";
		} catch (ClinicException ex) {
			model.addAttribute("error", ex.getMessage());
			return "register";
		}
	}

	@PostMapping("/logout")
	public String logout(HttpServletRequest request) {
		Sessions.clear(request);
		return "redirect:/login";
	}
}
