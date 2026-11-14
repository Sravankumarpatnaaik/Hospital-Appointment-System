package com.ashlin.clinic.model;

public record DoctorCard(long id, String name, String email, String specialization, String room, boolean active) {
}
