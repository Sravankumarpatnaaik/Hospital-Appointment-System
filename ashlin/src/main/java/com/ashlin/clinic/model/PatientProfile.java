package com.ashlin.clinic.model;

import java.time.LocalDate;

public record PatientProfile(long id, String fullName, String email, String phone, LocalDate dateOfBirth) {
}
