package com.ashlin.clinic.web;

import com.ashlin.clinic.service.AppointmentService;
import com.ashlin.clinic.service.DirectoryService;
import com.ashlin.clinic.support.ClinicCalendar;
import com.ashlin.clinic.support.When;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DoctorController {

	private final AppointmentService appointments;
	private final DirectoryService directory;
	private final Clock clock;

	public DoctorController(AppointmentService appointments, DirectoryService directory, Clock clock) {
		this.appointments = appointments;
		this.directory = directory;
		this.clock = clock;
	}

	@GetMapping("/doctor")
	public String schedule(HttpServletRequest request, Model model) {
		var user = Sessions.current(request);
		var doctor = directory.doctor(user.id());
		LocalDate today = LocalDate.now(clock);
		LocalDateTime now = LocalDateTime.now(clock);
		List<DayBoard> days = new ArrayList<>();
		LocalDate cursor = today;
		int open = 0;
		while (open < 6 && !cursor.isAfter(today.plusDays(14))) {
			if (ClinicCalendar.isOpen(cursor)) {
				var visits = appointments.forDoctor(user.id(), cursor, cursor.plusDays(1)).stream()
						.map(visit -> VisitLine.forDoctor(visit, now))
						.toList();
				days.add(new DayBoard(cursor.format(When.DAY), visits));
				open++;
			}
			cursor = cursor.plusDays(1);
		}
		model.addAttribute("title", "Your clinic");
		model.addAttribute("doctor", doctor);
		model.addAttribute("days", days);
		return "doctor/schedule";
	}

	public record DayBoard(String label, List<VisitLine> visits) {
	}
}
