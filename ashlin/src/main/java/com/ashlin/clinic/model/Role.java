package com.ashlin.clinic.model;

public enum Role {
	PATIENT,
	DOCTOR,
	ADMIN;

	public String home() {
		return switch (this) {
			case PATIENT -> "/patient";
			case DOCTOR -> "/doctor";
			case ADMIN -> "/admin";
		};
	}
}
