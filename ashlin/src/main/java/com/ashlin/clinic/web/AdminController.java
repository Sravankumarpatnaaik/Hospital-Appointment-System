package com.ashlin.clinic.web;

import com.ashlin.clinic.model.DoctorCard;
import com.ashlin.clinic.model.PatientProfile;
import com.ashlin.clinic.model.Visit;
import com.ashlin.clinic.service.AppointmentService;
import com.ashlin.clinic.service.DirectoryService;
import com.ashlin.clinic.support.ClinicException;
import com.ashlin.clinic.support.When;
import com.ashlin.clinic.web.form.BookForm;
import com.ashlin.clinic.web.form.DoctorForm;
import com.ashlin.clinic.web.form.PatientForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDateTime;

@Controller
public class AdminController {

	private final DirectoryService directory;
	private final AppointmentService appointments;
	private final Clock clock;

	public AdminController(DirectoryService directory, AppointmentService appointments, Clock clock) {
		this.directory = directory;
		this.appointments = appointments;
		this.clock = clock;
	}

	@GetMapping("/admin")
	public String home(Model model) {
		model.addAttribute("title", "Registrar");
		model.addAttribute("stats", directory.stats());
		return "admin/home";
	}

	@GetMapping("/admin/patients")
	public String patients(Model model) {
		model.addAttribute("title", "Patients");
		model.addAttribute("patients", directory.patients());
		return "admin/patients";
	}

	@GetMapping("/admin/patients/new")
	public String newPatient(Model model) {
		model.addAttribute("title", "New patient");
		model.addAttribute("form", new PatientForm());
		model.addAttribute("patientId", null);
		return "admin/patient-form";
	}

	@GetMapping("/admin/patients/{id}")
	public String editPatient(@PathVariable long id, Model model) {
		PatientProfile patient = directory.patient(id);
		PatientForm form = new PatientForm();
		form.setFullName(patient.fullName());
		form.setEmail(patient.email());
		form.setPhone(patient.phone());
		form.setDateOfBirth(patient.dateOfBirth());
		model.addAttribute("title", patient.fullName());
		model.addAttribute("form", form);
		model.addAttribute("patientId", id);
		model.addAttribute("canDelete", directory.patientCanBeDeleted(id));
		return "admin/patient-form";
	}

	@PostMapping("/admin/patients")
	public String createPatient(@Valid @ModelAttribute("form") PatientForm form, BindingResult errors,
			Model model, RedirectAttributes redirect) {
		return savePatient(null, form, errors, model, redirect);
	}

	@PostMapping("/admin/patients/{id}")
	public String updatePatient(@PathVariable long id, @Valid @ModelAttribute("form") PatientForm form, BindingResult errors,
			Model model, RedirectAttributes redirect) {
		return savePatient(id, form, errors, model, redirect);
	}

	@PostMapping("/admin/patients/{id}/delete")
	public String deletePatient(@PathVariable long id, RedirectAttributes redirect) {
		try {
			directory.deletePatient(id);
			redirect.addFlashAttribute("notice", "Patient record deleted.");
		} catch (ClinicException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/admin/patients";
	}

	@GetMapping("/admin/doctors")
	public String doctors(Model model) {
		model.addAttribute("title", "Doctors");
		model.addAttribute("doctors", directory.doctors(false, ""));
		return "admin/doctors";
	}

	@GetMapping("/admin/doctors/new")
	public String newDoctor(Model model) {
		model.addAttribute("title", "New doctor");
		model.addAttribute("form", new DoctorForm());
		model.addAttribute("doctorId", null);
		model.addAttribute("specializations", directory.specializations());
		return "admin/doctor-form";
	}

	@GetMapping("/admin/doctors/{id}")
	public String editDoctor(@PathVariable long id, Model model) {
		DoctorCard doctor = directory.doctor(id);
		DoctorForm form = new DoctorForm();
		form.setFullName(doctor.name());
		form.setEmail(doctor.email());
		form.setSpecialization(doctor.specialization());
		form.setRoom(doctor.room());
		form.setActive(doctor.active());
		model.addAttribute("title", doctor.name());
		model.addAttribute("form", form);
		model.addAttribute("doctorId", id);
		model.addAttribute("canDelete", directory.doctorCanBeDeleted(id));
		model.addAttribute("specializations", directory.specializations());
		return "admin/doctor-form";
	}

	@PostMapping("/admin/doctors")
	public String createDoctor(@Valid @ModelAttribute("form") DoctorForm form, BindingResult errors,
			Model model, RedirectAttributes redirect) {
		return saveDoctor(null, form, errors, model, redirect);
	}

	@PostMapping("/admin/doctors/{id}")
	public String updateDoctor(@PathVariable long id, @Valid @ModelAttribute("form") DoctorForm form, BindingResult errors,
			Model model, RedirectAttributes redirect) {
		return saveDoctor(id, form, errors, model, redirect);
	}

	@PostMapping("/admin/doctors/{id}/delete")
	public String deleteDoctor(@PathVariable long id, RedirectAttributes redirect) {
		try {
			directory.deleteDoctor(id);
			redirect.addFlashAttribute("notice", "Doctor record deleted.");
		} catch (ClinicException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/admin/doctors";
	}

	@GetMapping("/admin/appointments")
	public String appointments(Model model) {
		LocalDateTime now = LocalDateTime.now(clock);
		model.addAttribute("title", "Appointments");
		model.addAttribute("visits", appointments.recent().stream().map(visit -> VisitLine.forAdmin(visit, now)).toList());
		return "admin/appointments";
	}

	@GetMapping("/admin/appointments/new")
	public String newAppointment(@RequestParam(required = false) Long doctorId,
			@RequestParam(required = false) String day, Model model) {
		return showAppointment(null, doctorId, day, "", null, null, model);
	}

	@PostMapping("/admin/appointments")
	public String createAppointment(@Valid @ModelAttribute("form") BookForm form, BindingResult errors,
			@RequestParam long doctorId, Model model, RedirectAttributes redirect) {
		if (form.getPatientId() == null) {
			return showAppointment(null, doctorId, dayOf(form), form.getReason(), form.getPatientId(), "Choose a patient.", model);
		}
		if (errors.hasErrors()) {
			return showAppointment(null, doctorId, dayOf(form), form.getReason(), form.getPatientId(),
					errors.getFieldError().getDefaultMessage(), model);
		}
		try {
			var visit = appointments.book(form.getPatientId(), doctorId, form.getStartsAt(), form.getReason());
			redirect.addFlashAttribute("notice", "Booked " + visit.patientName() + " with " + visit.doctorName() + ".");
			return "redirect:/admin/appointments";
		} catch (ClinicException ex) {
			return showAppointment(null, doctorId, form.getStartsAt().toLocalDate().toString(), form.getReason(),
					form.getPatientId(), ex.getMessage(), model);
		}
	}

	@GetMapping("/admin/appointments/{id}")
	public String editAppointment(@PathVariable long id, @RequestParam(required = false) String day, Model model) {
		Visit visit = appointments.visit(id);
		return showAppointment(visit, visit.doctorId(), day == null ? visit.startsAt().toLocalDate().toString() : day,
				visit.reason(), visit.patientId(), null, model);
	}

	@PostMapping("/admin/appointments/{id}")
	public String updateAppointment(@PathVariable long id, @Valid @ModelAttribute("form") BookForm form, BindingResult errors,
			Model model, RedirectAttributes redirect) {
		Visit visit = appointments.visit(id);
		if (errors.hasErrors()) {
			return showAppointment(visit, visit.doctorId(), dayOf(form), form.getReason(), visit.patientId(),
					errors.getFieldError().getDefaultMessage(), model);
		}
		try {
			appointments.reschedule(id, null, form.getStartsAt(), form.getReason());
			redirect.addFlashAttribute("notice", "Visit updated.");
			return "redirect:/admin/appointments";
		} catch (ClinicException ex) {
			return showAppointment(visit, visit.doctorId(), form.getStartsAt().toLocalDate().toString(), form.getReason(),
					visit.patientId(), ex.getMessage(), model);
		}
	}

	@PostMapping("/admin/appointments/{id}/cancel")
	public String cancel(@PathVariable long id, RedirectAttributes redirect) {
		appointments.cancel(id, null);
		redirect.addFlashAttribute("notice", "Visit cancelled. That doctor hour is free again.");
		return "redirect:/admin/appointments";
	}

	@PostMapping("/admin/appointments/{id}/delete")
	public String delete(@PathVariable long id, RedirectAttributes redirect) {
		appointments.delete(id);
		redirect.addFlashAttribute("notice", "Visit deleted from the book.");
		return "redirect:/admin/appointments";
	}

	private String savePatient(Long id, PatientForm form, BindingResult errors, Model model, RedirectAttributes redirect) {
		model.addAttribute("title", id == null ? "New patient" : "Edit patient");
		model.addAttribute("patientId", id);
		if (id != null) {
			model.addAttribute("canDelete", directory.patientCanBeDeleted(id));
		}
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getFieldError().getDefaultMessage());
			return "admin/patient-form";
		}
		try {
			directory.savePatient(id, form);
			redirect.addFlashAttribute("notice", id == null ? "Patient added." : "Patient updated.");
			return "redirect:/admin/patients";
		} catch (ClinicException ex) {
			model.addAttribute("error", ex.getMessage());
			return "admin/patient-form";
		}
	}

	private String saveDoctor(Long id, DoctorForm form, BindingResult errors, Model model, RedirectAttributes redirect) {
		model.addAttribute("title", id == null ? "New doctor" : "Edit doctor");
		model.addAttribute("doctorId", id);
		model.addAttribute("specializations", directory.specializations());
		if (id != null) {
			model.addAttribute("canDelete", directory.doctorCanBeDeleted(id));
		}
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getFieldError().getDefaultMessage());
			return "admin/doctor-form";
		}
		try {
			directory.saveDoctor(id, form);
			redirect.addFlashAttribute("notice", id == null ? "Doctor added." : "Doctor updated.");
			return "redirect:/admin/doctors";
		} catch (ClinicException ex) {
			model.addAttribute("error", ex.getMessage());
			return "admin/doctor-form";
		}
	}

	private String showAppointment(Visit visit, Long doctorId, String day, String reason, Long patientId, String error, Model model) {
		var doctors = directory.doctors(false, "");
		if (doctors.isEmpty()) {
			throw new ClinicException("Add a doctor before booking a visit.");
		}
		long chosenDoctor = doctorId == null ? doctors.get(0).id() : doctorId;
		var doctor = directory.doctor(chosenDoctor);
		var selected = appointments.selectedDay(day);
		model.addAttribute("title", visit == null ? "Book a visit" : "Update a visit");
		model.addAttribute("visit", visit == null ? null : VisitLine.forAdmin(visit, LocalDateTime.now(clock)));
		model.addAttribute("doctors", doctors);
		model.addAttribute("doctor", doctor);
		model.addAttribute("patients", directory.patients());
		model.addAttribute("patientId", patientId);
		model.addAttribute("days", appointments.days(selected));
		model.addAttribute("dayLabel", selected.format(When.DAY));
		model.addAttribute("slots", appointments.slots(chosenDoctor, selected, patientId, visit == null ? -1 : visit.id()));
		model.addAttribute("reason", reason == null ? "" : reason);
		model.addAttribute("error", error);
		return "admin/appointment-form";
	}

	private static String dayOf(BookForm form) {
		return form.getStartsAt() == null ? null : form.getStartsAt().toLocalDate().toString();
	}
}
