package com.ashlin.clinic.web;

import com.ashlin.clinic.model.ClinicUser;
import com.ashlin.clinic.service.AppointmentService;
import com.ashlin.clinic.service.DirectoryService;
import com.ashlin.clinic.support.ClinicException;
import com.ashlin.clinic.web.form.BookForm;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;

@Controller
public class PatientController {

	private final DirectoryService directory;
	private final AppointmentService appointments;
	private final Clock clock;

	public PatientController(DirectoryService directory, AppointmentService appointments, Clock clock) {
		this.directory = directory;
		this.appointments = appointments;
		this.clock = clock;
	}

	@ModelAttribute("specializations")
	public List<String> specializations() {
		return directory.specializations();
	}

	@GetMapping("/patient")
	public String home(HttpServletRequest request, Model model) {
		ClinicUser user = Sessions.current(request);
		var now = LocalDateTime.now(clock);
		var lines = appointments.forPatient(user.id()).stream().map(visit -> VisitLine.forPatient(visit, now)).toList();
		model.addAttribute("title", "Your visits");
		model.addAttribute("upcoming", lines.stream().filter(VisitLine::upcoming).toList());
		model.addAttribute("earlier", lines.stream().filter(line -> !line.upcoming()).toList());
		return "patient/home";
	}

	@GetMapping("/patient/doctors")
	public String doctors(@RequestParam(required = false) String specialization, Model model) {
		String chosen = specialization == null ? "" : specialization.trim();
		model.addAttribute("title", "Find a doctor");
		model.addAttribute("chosen", chosen);
		if (!chosen.isEmpty() && !directory.specializations().contains(chosen)) {
			model.addAttribute("error", "That clinic is not on the board.");
			model.addAttribute("doctors", List.of());
			return "patient/doctors";
		}
		model.addAttribute("doctors", directory.doctors(true, chosen));
		return "patient/doctors";
	}

	@GetMapping("/patient/doctors/{id}")
	public String book(@PathVariable long id, @RequestParam(required = false) String day,
			HttpServletRequest request, Model model) {
		return showBook(id, day, "", null, Sessions.current(request).id(), model);
	}

	@PostMapping("/patient/doctors/{id}")
	public String book(@PathVariable long id, @Valid @ModelAttribute("form") BookForm form, BindingResult errors,
			HttpServletRequest request, Model model, RedirectAttributes redirect) {
		ClinicUser user = Sessions.current(request);
		if (errors.hasErrors()) {
			String day = form.getStartsAt() == null ? null : form.getStartsAt().toLocalDate().toString();
			return showBook(id, day, form.getReason(), errors.getFieldError().getDefaultMessage(), user.id(), model);
		}
		try {
			var visit = appointments.book(user.id(), id, form.getStartsAt(), form.getReason());
			redirect.addFlashAttribute("notice", "Booked with " + visit.doctorName() + " at " + visit.startsAt().format(com.ashlin.clinic.support.When.VISIT) + ".");
			return "redirect:/patient";
		} catch (ClinicException ex) {
			return showBook(id, form.getStartsAt().toLocalDate().toString(), form.getReason(), ex.getMessage(), user.id(), model);
		}
	}

	@GetMapping("/patient/appointments/{id}")
	public String reschedule(@PathVariable long id, @RequestParam(required = false) String day,
			HttpServletRequest request, Model model) {
		var visit = own(id, request);
		return showMove(visit, day, null, model);
	}

	@PostMapping("/patient/appointments/{id}")
	public String reschedule(@PathVariable long id, @Valid @ModelAttribute("form") BookForm form, BindingResult errors,
			HttpServletRequest request, Model model, RedirectAttributes redirect) {
		var visit = own(id, request);
		if (errors.hasErrors()) {
			String day = form.getStartsAt() == null ? null : form.getStartsAt().toLocalDate().toString();
			return showMove(visit, day, errors.getFieldError().getDefaultMessage(), model);
		}
		try {
			var moved = appointments.reschedule(id, Sessions.current(request).id(), form.getStartsAt(), visit.reason());
			redirect.addFlashAttribute("notice", "Moved to " + moved.startsAt().format(com.ashlin.clinic.support.When.VISIT) + ".");
			return "redirect:/patient";
		} catch (ClinicException ex) {
			return showMove(visit, form.getStartsAt().toLocalDate().toString(), ex.getMessage(), model);
		}
	}

	@PostMapping("/patient/appointments/{id}/cancel")
	public String cancel(@PathVariable long id, HttpServletRequest request, RedirectAttributes redirect) {
		try {
			appointments.cancel(id, Sessions.current(request).id());
			redirect.addFlashAttribute("notice", "The visit is cancelled and the hour is free again.");
		} catch (ClinicException ex) {
			redirect.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/patient";
	}

	private com.ashlin.clinic.model.Visit own(long id, HttpServletRequest request) {
		var visit = appointments.visit(id);
		if (visit.patientId() != Sessions.current(request).id()) {
			throw new ClinicException("That visit is not on your book.");
		}
		return visit;
	}

	private String showBook(long doctorId, String day, String reason, String error, long patientId, Model model) {
		var doctor = directory.doctor(doctorId);
		if (!doctor.active()) {
			throw new ClinicException("That doctor is not taking appointments.");
		}
		var selected = appointments.selectedDay(day);
		model.addAttribute("title", doctor.name());
		model.addAttribute("doctor", doctor);
		model.addAttribute("days", appointments.days(selected));
		model.addAttribute("dayLabel", selected.format(com.ashlin.clinic.support.When.DAY));
		model.addAttribute("slots", appointments.slots(doctorId, selected, patientId, -1));
		model.addAttribute("reason", reason == null ? "" : reason);
		model.addAttribute("error", error);
		return "patient/book";
	}

	private String showMove(com.ashlin.clinic.model.Visit visit, String day, String error, Model model) {
		var selected = appointments.selectedDay(day == null ? visit.startsAt().toLocalDate().toString() : day);
		model.addAttribute("title", "Move this visit");
		model.addAttribute("visit", VisitLine.forPatient(visit, LocalDateTime.now(clock)));
		model.addAttribute("doctorId", visit.doctorId());
		model.addAttribute("days", appointments.days(selected));
		model.addAttribute("dayLabel", selected.format(com.ashlin.clinic.support.When.DAY));
		model.addAttribute("slots", appointments.slots(visit.doctorId(), selected, visit.patientId(), visit.id()));
		model.addAttribute("error", error);
		return "patient/reschedule";
	}
}
