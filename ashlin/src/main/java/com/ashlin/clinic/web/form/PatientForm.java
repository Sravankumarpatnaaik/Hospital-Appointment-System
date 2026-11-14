package com.ashlin.clinic.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class PatientForm {

	@NotBlank(message = "Enter the patient's name.")
	@Size(max = 80, message = "Name must be 80 characters or fewer.")
	private String fullName;

	@NotBlank(message = "Enter an email.")
	@Email(message = "Enter a valid email.")
	@Size(max = 120)
	private String email;

	@NotBlank(message = "Enter a phone number.")
	@Pattern(regexp = "[0-9+()\\- ]{8,20}", message = "Phone should be 8 to 20 digits.")
	private String phone;

	@NotNull(message = "Enter a date of birth.")
	@Past(message = "Date of birth has to be in the past.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate dateOfBirth;

	@Size(max = 72, message = "Password must be at most 72 characters.")
	private String password;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
