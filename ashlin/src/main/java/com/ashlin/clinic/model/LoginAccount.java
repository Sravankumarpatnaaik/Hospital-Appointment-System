package com.ashlin.clinic.model;

public record LoginAccount(long id, String fullName, String email, String passwordHash, boolean active) {
}
