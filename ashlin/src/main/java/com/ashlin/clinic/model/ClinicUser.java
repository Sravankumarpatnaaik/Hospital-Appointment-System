package com.ashlin.clinic.model;

import java.io.Serializable;

public record ClinicUser(long id, String name, String email, Role role) implements Serializable {
}
