package com.ashlin.clinic.service;

import com.ashlin.clinic.dao.AppointmentDao;
import com.ashlin.clinic.dao.DoctorDao;
import com.ashlin.clinic.dao.PatientDao;
import com.ashlin.clinic.dao.SpecializationDao;
import com.ashlin.clinic.model.DeskStats;
import com.ashlin.clinic.model.DoctorCard;
import com.ashlin.clinic.model.PatientProfile;
import com.ashlin.clinic.support.ClinicException;
import com.ashlin.clinic.web.form.DoctorForm;
import com.ashlin.clinic.web.form.PatientForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DirectoryService {

	private final PatientDao patients;
	private final DoctorDao doctors;
	private final AppointmentDao appointments;
	private final SpecializationDao specializations;
	private final AuthService auth;
	private final Clock clock;

	public DirectoryService(PatientDao patients, DoctorDao doctors, AppointmentDao appointments,
			SpecializationDao specializations, AuthService auth, Clock clock) {
		this.patients = patients;
		this.doctors = doctors;
		this.appointments = appointments;
		this.specializations = specializations;
		this.auth = auth;
		this.clock = clock;
	}

	public List<String> specializations() {
		return specializations.names();
	}

	public List<DoctorCard> doctors(boolean activeOnly, String specialization) {
		String filter = specialization == null ? "" : specialization.trim();
		return doctors.list(activeOnly, filter);
	}

	public DoctorCard doctor(long id) {
		return doctors.find(id).orElseThrow(() -> new ClinicException("No doctor has that id."));
	}

	public List<PatientProfile> patients() {
		return patients.list();
	}

	public PatientProfile patient(long id) {
		return patients.find(id).orElseThrow(() -> new ClinicException("No patient has that id."));
	}

	public DeskStats stats() {
		return new DeskStats(
				patients.count(),
				doctors.countActive(),
				appointments.countBookedFrom(LocalDateTime.now(clock)));
	}

	@Transactional
	public void savePatient(Long id, PatientForm form) {
		String email = AuthService.email(form.getEmail());
		if (auth.emailTaken(email, id, null)) {
			throw new ClinicException("An account already uses that email.");
		}
		String hash = passwordOrNull(form.getPassword());
		if (id == null) {
			if (hash == null) {
				throw new ClinicException("A new patient needs a password of at least 8 characters.");
			}
			patients.insert(form.getFullName().trim(), email, form.getPhone().trim(), form.getDateOfBirth(), hash, LocalDateTime.now(clock));
			return;
		}
		if (patients.find(id).isEmpty()) {
			throw new ClinicException("No patient has that id.");
		}
		patients.update(id, form.getFullName().trim(), email, form.getPhone().trim(), form.getDateOfBirth(), hash);
	}

	@Transactional
	public void deletePatient(long id) {
		if (patients.find(id).isEmpty()) {
			throw new ClinicException("No patient has that id.");
		}
		if (appointments.countForPatient(id) > 0) {
			throw new ClinicException("This patient still has visits on the book. Cancel and delete those visits first.");
		}
		patients.delete(id);
	}

	@Transactional
	public void saveDoctor(Long id, DoctorForm form) {
		String email = AuthService.email(form.getEmail());
		if (auth.emailTaken(email, null, id)) {
			throw new ClinicException("An account already uses that email.");
		}
		long specializationId = auth.specializationId(form.getSpecialization());
		String hash = passwordOrNull(form.getPassword());
		boolean active = form.isActive();
		if (!active && id != null && appointments.forDoctorBetween(id, LocalDateTime.now(clock), LocalDateTime.now(clock).plusDays(60)).size() > 0) {
			throw new ClinicException("Move or cancel future visits before retiring this doctor.");
		}
		if (id == null) {
			if (hash == null) {
				throw new ClinicException("A new doctor needs a password of at least 8 characters.");
			}
			doctors.insert(form.getFullName().trim(), email, hash, specializationId, form.getRoom().trim(), active);
			return;
		}
		if (doctors.find(id).isEmpty()) {
			throw new ClinicException("No doctor has that id.");
		}
		doctors.update(id, form.getFullName().trim(), email, hash, specializationId, form.getRoom().trim(), active);
	}

	@Transactional
	public void deleteDoctor(long id) {
		if (doctors.find(id).isEmpty()) {
			throw new ClinicException("No doctor has that id.");
		}
		if (appointments.countForDoctor(id) > 0) {
			throw new ClinicException("This doctor still has visits on the book, so the row stays.");
		}
		doctors.delete(id);
	}

	public boolean doctorCanBeDeleted(long id) {
		return appointments.countForDoctor(id) == 0;
	}

	public boolean patientCanBeDeleted(long id) {
		return appointments.countForPatient(id) == 0;
	}

	private String passwordOrNull(String password) {
		if (password == null || password.isBlank()) {
			return null;
		}
		if (password.length() < 8) {
			throw new ClinicException("Password must be at least 8 characters.");
		}
		return auth.encode(password);
	}
}
