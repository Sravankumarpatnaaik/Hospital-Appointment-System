package com.ashlin.clinic.service;

import com.ashlin.clinic.dao.AdminDao;
import com.ashlin.clinic.dao.DoctorDao;
import com.ashlin.clinic.dao.PatientDao;
import com.ashlin.clinic.dao.SpecializationDao;
import com.ashlin.clinic.model.ClinicUser;
import com.ashlin.clinic.model.LoginAccount;
import com.ashlin.clinic.model.Role;
import com.ashlin.clinic.support.ClinicException;
import com.ashlin.clinic.web.form.RegisterForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

	private final PatientDao patients;
	private final DoctorDao doctors;
	private final AdminDao admins;
	private final SpecializationDao specializations;
	private final PasswordEncoder passwords;
	private final Clock clock;

	public AuthService(PatientDao patients, DoctorDao doctors, AdminDao admins, SpecializationDao specializations,
			PasswordEncoder passwords, Clock clock) {
		this.patients = patients;
		this.doctors = doctors;
		this.admins = admins;
		this.specializations = specializations;
		this.passwords = passwords;
		this.clock = clock;
	}

	public ClinicUser login(String email, String password) {
		String normalized = email(email);
		Optional<LoginAccount> patient = patients.findForLogin(normalized);
		if (patient.isPresent()) {
			return accept(patient.get(), password, Role.PATIENT);
		}
		Optional<LoginAccount> doctor = doctors.findForLogin(normalized);
		if (doctor.isPresent()) {
			return accept(doctor.get(), password, Role.DOCTOR);
		}
		Optional<LoginAccount> admin = admins.findForLogin(normalized);
		if (admin.isPresent()) {
			return accept(admin.get(), password, Role.ADMIN);
		}
		throw new ClinicException("That email and password do not match.");
	}

	@Transactional
	public ClinicUser register(RegisterForm form) {
		String normalized = email(form.getEmail());
		if (emailTaken(normalized, null, null)) {
			throw new ClinicException("An account already uses that email.");
		}
		LocalDate birth = form.getDateOfBirth();
		if (birth == null || !birth.isBefore(LocalDate.now(clock))) {
			throw new ClinicException("Date of birth has to be in the past.");
		}
		long id = patients.insert(
				form.getFullName().trim(),
				normalized,
				form.getPhone().trim(),
				birth,
				passwords.encode(form.getPassword()),
				LocalDateTime.now(clock));
		return new ClinicUser(id, form.getFullName().trim(), normalized, Role.PATIENT);
	}

	public boolean emailTaken(String email, Long exceptPatientId, Long exceptDoctorId) {
		return patients.emailTaken(email, exceptPatientId)
				|| doctors.emailTaken(email, exceptDoctorId)
				|| admins.emailTaken(email);
	}

	public String encode(String raw) {
		return passwords.encode(raw);
	}

	public long specializationId(String name) {
		return specializations.idOf(name)
				.orElseThrow(() -> new ClinicException("Choose a specialization from the list."));
	}

	private ClinicUser accept(LoginAccount account, String password, Role role) {
		if (!account.active()) {
			throw new ClinicException("This doctor is not taking appointments. Ask the registrar.");
		}
		if (!passwords.matches(password, account.passwordHash())) {
			throw new ClinicException("That email and password do not match.");
		}
		return new ClinicUser(account.id(), account.fullName(), account.email(), role);
	}

	public static String email(String raw) {
		if (raw == null) {
			return "";
		}
		return raw.trim().toLowerCase(Locale.ROOT);
	}
}
