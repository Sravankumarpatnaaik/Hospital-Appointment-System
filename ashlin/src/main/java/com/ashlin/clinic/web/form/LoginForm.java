package com.ashlin.clinic.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginForm {

	@NotBlank(message = "Enter your email.")
	@Email(message = "Enter a valid email.")
	private String email;

	@NotBlank(message = "Enter your password.")
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
