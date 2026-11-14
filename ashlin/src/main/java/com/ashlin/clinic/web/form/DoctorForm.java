package com.ashlin.clinic.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DoctorForm {

	@NotBlank(message = "Enter the doctor's name.")
	@Size(max = 80, message = "Name must be 80 characters or fewer.")
	private String fullName;

	@NotBlank(message = "Enter an email.")
	@Email(message = "Enter a valid email.")
	@Size(max = 120)
	private String email;

	@NotBlank(message = "Choose a specialization.")
	private String specialization;

	@NotBlank(message = "Enter a room.")
	@Size(max = 16, message = "Room must be 16 characters or fewer.")
	private String room;

	@Size(max = 72, message = "Password must be at most 72 characters.")
	private String password;

	private boolean active = true;

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

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
