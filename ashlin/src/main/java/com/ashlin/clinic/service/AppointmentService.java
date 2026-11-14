package com.ashlin.clinic.service;

import com.ashlin.clinic.dao.AppointmentDao;
import com.ashlin.clinic.dao.DoctorDao;
import com.ashlin.clinic.dao.PatientDao;
import com.ashlin.clinic.model.ApptStatus;
import com.ashlin.clinic.model.DayChip;
import com.ashlin.clinic.model.SlotCell;
import com.ashlin.clinic.model.Visit;
import com.ashlin.clinic.support.ClinicCalendar;
import com.ashlin.clinic.support.ClinicException;
import com.ashlin.clinic.support.SlotTakenException;
import com.ashlin.clinic.support.When;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

	private final AppointmentDao appointments;
	private final DoctorDao doctors;
	private final PatientDao patients;
	private final Clock clock;

	public AppointmentService(AppointmentDao appointments, DoctorDao doctors, PatientDao patients, Clock clock) {
		this.appointments = appointments;
		this.doctors = doctors;
		this.patients = patients;
		this.clock = clock;
	}

	@Transactional
	public Visit book(long patientId, long doctorId, LocalDateTime start, String reason) {
		String cleanReason = cleanReason(reason);
		LocalDateTime now = LocalDateTime.now(clock);
		ClinicCalendar.requireBookable(start, now);
		if (patients.find(patientId).isEmpty()) {
			throw new ClinicException("No patient has that id.");
		}
		var doctor = doctors.find(doctorId).orElseThrow(() -> new ClinicException("No doctor has that id."));
		if (!doctor.active()) {
			throw new ClinicException("That doctor is not taking appointments.");
		}
		doctors.lock(doctorId);
		refuseIfTaken(doctorId, patientId, start, -1, doctor.name());
		try {
			long id = appointments.insert(patientId, doctorId, start, cleanReason, now);
			return appointments.find(id).orElseThrow();
		} catch (DuplicateKeyException ex) {
			throw new SlotTakenException("That hour was just taken. Choose another.");
		}
	}

	@Transactional
	public Visit reschedule(long appointmentId, Long onlyPatientId, LocalDateTime start, String reason) {
		Visit current = requireBooked(appointmentId, onlyPatientId);
		String cleanReason = reason == null ? current.reason() : cleanReason(reason);
		if (current.startsAt().equals(start) && current.reason().equals(cleanReason)) {
			return current;
		}
		LocalDateTime now = LocalDateTime.now(clock);
		if (onlyPatientId != null && !current.startsAt().isAfter(now)) {
			throw new ClinicException("That visit has already started, so it stays on the record.");
		}
		if (!current.startsAt().equals(start)) {
			ClinicCalendar.requireBookable(start, now);
			var doctor = doctors.find(current.doctorId()).orElseThrow(() -> new ClinicException("No doctor has that id."));
			if (!doctor.active()) {
				throw new ClinicException("That doctor is not taking appointments.");
			}
			doctors.lock(current.doctorId());
			refuseIfTaken(current.doctorId(), current.patientId(), start, current.id(), doctor.name());
		}
		appointments.updateWhen(current.id(), start, cleanReason);
		return appointments.find(current.id()).orElseThrow();
	}

	@Transactional
	public void cancel(long appointmentId, Long onlyPatientId) {
		Visit current = requireBooked(appointmentId, onlyPatientId);
		if (onlyPatientId != null && !current.startsAt().isAfter(LocalDateTime.now(clock))) {
			throw new ClinicException("That visit has already started, so it stays on the record.");
		}
		appointments.cancel(appointmentId);
	}

	@Transactional
	public void delete(long appointmentId) {
		if (appointments.find(appointmentId).isEmpty()) {
			throw new ClinicException("No visit has that id.");
		}
		appointments.delete(appointmentId);
	}

	public Visit visit(long id) {
		return appointments.find(id).orElseThrow(() -> new ClinicException("No visit has that id."));
	}

	public List<Visit> forPatient(long patientId) {
		return appointments.forPatient(patientId);
	}

	public List<Visit> forDoctor(long doctorId, LocalDate from, LocalDate toExclusive) {
		return appointments.forDoctorBetween(doctorId, from.atStartOfDay(), toExclusive.atStartOfDay());
	}

	public List<Visit> recent() {
		return appointments.recent();
	}

	public List<DayChip> days(LocalDate selected) {
		LocalDate today = LocalDate.now(clock);
		List<DayChip> chips = new ArrayList<>();
		for (LocalDate day : ClinicCalendar.bookableDays(today, LocalTime.now(clock), 8)) {
			chips.add(new DayChip(day.format(When.ISO_DATE), day.format(When.DAY), day.equals(selected)));
		}
		return chips;
	}

	public LocalDate selectedDay(String raw) {
		LocalDate today = LocalDate.now(clock);
		List<LocalDate> open = ClinicCalendar.bookableDays(today, LocalTime.now(clock), 8);
		if (open.isEmpty()) {
			throw new ClinicException("No open day is left on the book.");
		}
		if (raw == null || raw.isBlank()) {
			return open.get(0);
		}
		LocalDate parsed;
		try {
			parsed = LocalDate.parse(raw);
		} catch (Exception ex) {
			return open.get(0);
		}
		if (!open.contains(parsed)) {
			return open.get(0);
		}
		return parsed;
	}

	public List<SlotCell> slots(long doctorId, LocalDate day, Long viewerPatientId, long exceptAppointmentId) {
		LocalDateTime now = LocalDateTime.now(clock);
		List<Visit> taken = appointments.occupied(doctorId, day.atStartOfDay(), day.plusDays(1).atStartOfDay(), exceptAppointmentId);
		List<SlotCell> cells = new ArrayList<>();
		for (var time : ClinicCalendar.slots()) {
			LocalDateTime start = day.atTime(time);
			Visit held = taken.stream().filter(visit -> visit.startsAt().equals(start)).findFirst().orElse(null);
			String state;
			if (!start.isAfter(now)) {
				state = "past";
			} else if (held != null && viewerPatientId != null && held.patientId() == viewerPatientId) {
				state = "mine";
			} else if (held != null) {
				state = "taken";
			} else {
				state = "open";
			}
			cells.add(new SlotCell(start.format(When.ISO_MINUTE), start.format(When.TIME), state));
		}
		return cells;
	}

	private Visit requireBooked(long appointmentId, Long onlyPatientId) {
		Visit current = appointments.find(appointmentId)
				.orElseThrow(() -> new ClinicException("No visit has that id."));
		if (onlyPatientId != null && current.patientId() != onlyPatientId) {
			throw new ClinicException("That visit is not on your book.");
		}
		if (current.status() != ApptStatus.BOOKED) {
			throw new ClinicException("That visit is already cancelled.");
		}
		return current;
	}

	private void refuseIfTaken(long doctorId, long patientId, LocalDateTime start, long exceptId, String doctorName) {
		if (appointments.countActiveForDoctor(doctorId, start, exceptId) > 0) {
			throw new SlotTakenException(doctorName + " already has a patient at " + start.format(When.TIME) + ". Pick another hour.");
		}
		if (appointments.countActiveForPatient(patientId, start, exceptId) > 0) {
			throw new SlotTakenException("That patient already has a visit at " + start.format(When.TIME) + ".");
		}
	}

	private static String cleanReason(String reason) {
		if (reason == null || reason.isBlank()) {
			throw new ClinicException("Say briefly why you are coming in.");
		}
		String trimmed = reason.trim();
		if (trimmed.length() < 3 || trimmed.length() > 200) {
			throw new ClinicException("The reason should be between 3 and 200 characters.");
		}
		return trimmed;
	}
}
