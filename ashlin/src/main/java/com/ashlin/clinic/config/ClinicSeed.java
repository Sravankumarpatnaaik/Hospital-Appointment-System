package com.ashlin.clinic.config;

import com.ashlin.clinic.dao.AdminDao;
import com.ashlin.clinic.dao.DoctorDao;
import com.ashlin.clinic.dao.PatientDao;
import com.ashlin.clinic.dao.SpecializationDao;
import com.ashlin.clinic.service.AppointmentService;
import com.ashlin.clinic.support.ClinicCalendar;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@ConditionalOnProperty(name = "ashlin.seed", havingValue = "true", matchIfMissing = true)
public class ClinicSeed implements ApplicationRunner {

	private final PatientDao patients;
	private final DoctorDao doctors;
	private final AdminDao admins;
	private final SpecializationDao specializations;
	private final AppointmentService appointments;
	private final PasswordEncoder passwords;
	private final Clock clock;

	public ClinicSeed(PatientDao patients, DoctorDao doctors, AdminDao admins, SpecializationDao specializations,
			AppointmentService appointments, PasswordEncoder passwords, Clock clock) {
		this.patients = patients;
		this.doctors = doctors;
		this.admins = admins;
		this.specializations = specializations;
		this.appointments = appointments;
		this.passwords = passwords;
		this.clock = clock;
	}

	@Override
	public void run(ApplicationArguments args) {
		for (String name : new String[] {
				"General medicine", "Cardiology", "Dermatology", "Orthopaedics", "Paediatrics", "ENT"
		}) {
			specializations.insert(name);
		}
		if (patients.count() > 0) {
			return;
		}
		String patientHash = passwords.encode("visit-key");
		String doctorHash = passwords.encode("round-key");
		long leela = doctor("Leela Nair", "leela.nair@ashlin.clinic", doctorHash, "General medicine", "1");
		long anil = doctor("Anil Mehta", "anil.mehta@ashlin.clinic", doctorHash, "Cardiology", "2");
		long meera = doctor("Meera Iyer", "meera.iyer@ashlin.clinic", doctorHash, "Paediatrics", "3");
		long farah = doctor("Farah Qureshi", "farah.qureshi@ashlin.clinic", doctorHash, "Dermatology", "4");
		long kabir = doctor("Kabir Sen", "kabir.sen@ashlin.clinic", doctorHash, "ENT", "5");
		doctor("Joseph D'Souza", "joseph.dsouza@ashlin.clinic", doctorHash, "Orthopaedics", "6");
		admins.insert("Nandita Rao", "registrar@ashlin.clinic", passwords.encode("ledger-key"));

		LocalDateTime now = LocalDateTime.now(clock);
		long rima = patients.insert("Rima Das", "rima.das@ashlin.clinic", "9830011401", LocalDate.of(1991, 4, 12), patientHash, now);
		long arjun = patients.insert("Arjun Ghosh", "arjun.ghosh@ashlin.clinic", "9830011402", LocalDate.of(1984, 9, 3), patientHash, now);
		long nila = patients.insert("Nila Banerjee", "nila.banerjee@ashlin.clinic", "9830011403", LocalDate.of(1996, 1, 28), patientHash, now);

		var days = ClinicCalendar.bookableDays(LocalDate.now(clock), LocalTime.now(clock), 2);
		LocalDate first = days.get(0);
		LocalDate second = days.get(1);
		appointments.book(rima, leela, at(first, 9, 30), "Blood pressure review");
		appointments.book(arjun, anil, at(first, 10, 0), "Chest tightness on stairs");
		appointments.book(nila, meera, at(first, 11, 0), "Fever and cough for two days");
		appointments.book(rima, farah, at(second, 15, 0), "Rash on the left forearm");
		appointments.book(arjun, kabir, at(second, 9, 0), "Blocked ear since Monday");
	}

	private LocalDateTime at(LocalDate day, int hour, int minute) {
		LocalDateTime start = day.atTime(hour, minute);
		if (start.isAfter(LocalDateTime.now(clock))) {
			return start;
		}
		LocalDate later = day.plusDays(1);
		while (!ClinicCalendar.isOpen(later)) {
			later = later.plusDays(1);
		}
		return later.atTime(hour, minute);
	}

	private long doctor(String name, String email, String hash, String specialization, String room) {
		long specializationId = specializations.idOf(specialization).orElseThrow();
		return doctors.insert(name, email, hash, specializationId, room, true);
	}
}
