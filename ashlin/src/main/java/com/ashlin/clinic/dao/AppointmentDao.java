package com.ashlin.clinic.dao;

import com.ashlin.clinic.model.ApptStatus;
import com.ashlin.clinic.model.Visit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AppointmentDao {

	private static final RowMapper<Visit> VISIT = (rs, row) -> new Visit(
			rs.getLong("id"),
			rs.getLong("patient_id"),
			rs.getLong("doctor_id"),
			rs.getObject("starts_at", LocalDateTime.class),
			rs.getString("reason"),
			ApptStatus.valueOf(rs.getString("status")),
			rs.getString("patient_name"),
			rs.getString("patient_phone"),
			rs.getString("doctor_name"),
			rs.getString("specialization"),
			rs.getString("room"));

	private static final String VISIT_SQL = """
			SELECT a.id, a.patient_id, a.doctor_id, a.starts_at, a.reason, a.status,
			       p.full_name AS patient_name, p.phone AS patient_phone,
			       d.full_name AS doctor_name, s.name AS specialization, d.room
			FROM appointments a
			JOIN patients p ON p.id = a.patient_id
			JOIN doctors d ON d.id = a.doctor_id
			JOIN specializations s ON s.id = d.specialization_id
			""";

	private final JdbcTemplate jdbc;

	public AppointmentDao(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public long insert(long patientId, long doctorId, LocalDateTime startsAt, String reason, LocalDateTime createdAt) {
		KeyHolder keys = new GeneratedKeyHolder();
		jdbc.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"""
					INSERT INTO appointments (patient_id, doctor_id, starts_at, reason, status, created_at)
					VALUES (?, ?, ?, ?, 'BOOKED', ?)
					""",
					Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, patientId);
			ps.setLong(2, doctorId);
			ps.setTimestamp(3, Timestamp.valueOf(startsAt));
			ps.setString(4, reason);
			ps.setObject(5, createdAt);
			return ps;
		}, keys);
		Number key = keys.getKey();
		if (key == null) {
			throw new IllegalStateException("Appointment insert did not return a key");
		}
		return key.longValue();
	}

	/**
	 * Active bookings for this doctor at this exact hour.
	 * The booking transaction runs this before insert, and refuses the visit when the count is not zero.
	 */
	public int countActiveForDoctor(long doctorId, LocalDateTime startsAt, long exceptId) {
		Integer count = jdbc.queryForObject(
				"""
				SELECT COUNT(*)
				FROM appointments
				WHERE doctor_id = ?
				  AND starts_at = ?
				  AND status <> 'CANCELLED'
				  AND id <> ?
				""",
				Integer.class,
				doctorId, Timestamp.valueOf(startsAt), exceptId);
		return count == null ? 0 : count;
	}

	public int countActiveForPatient(long patientId, LocalDateTime startsAt, long exceptId) {
		Integer count = jdbc.queryForObject(
				"""
				SELECT COUNT(*)
				FROM appointments
				WHERE patient_id = ?
				  AND starts_at = ?
				  AND status <> 'CANCELLED'
				  AND id <> ?
				""",
				Integer.class,
				patientId, Timestamp.valueOf(startsAt), exceptId);
		return count == null ? 0 : count;
	}

	public List<Visit> occupied(long doctorId, LocalDateTime from, LocalDateTime to, long exceptId) {
		return jdbc.query(
				VISIT_SQL + """
						WHERE a.doctor_id = ?
						  AND a.starts_at >= ?
						  AND a.starts_at < ?
						  AND a.status <> 'CANCELLED'
						  AND a.id <> ?
						ORDER BY a.starts_at
						""",
				VISIT,
				doctorId, Timestamp.valueOf(from), Timestamp.valueOf(to), exceptId);
	}

	public Optional<Visit> find(long id) {
		List<Visit> rows = jdbc.query(VISIT_SQL + " WHERE a.id = ?", VISIT, id);
		return rows.stream().findFirst();
	}

	public List<Visit> forPatient(long patientId) {
		return jdbc.query(
				VISIT_SQL + " WHERE a.patient_id = ? ORDER BY a.starts_at DESC",
				VISIT,
				patientId);
	}

	public List<Visit> forDoctorBetween(long doctorId, LocalDateTime from, LocalDateTime to) {
		return jdbc.query(
				VISIT_SQL + """
						WHERE a.doctor_id = ?
						  AND a.starts_at >= ?
						  AND a.starts_at < ?
						  AND a.status = 'BOOKED'
						ORDER BY a.starts_at
						""",
				VISIT,
				doctorId, Timestamp.valueOf(from), Timestamp.valueOf(to));
	}

	public List<Visit> recent() {
		return jdbc.query(VISIT_SQL + " ORDER BY a.starts_at DESC LIMIT 200", VISIT);
	}

	public int countBookedFrom(LocalDateTime from) {
		Integer count = jdbc.queryForObject(
				"SELECT COUNT(*) FROM appointments WHERE status = 'BOOKED' AND starts_at >= ?",
				Integer.class,
				Timestamp.valueOf(from));
		return count == null ? 0 : count;
	}

	public int countForPatient(long patientId) {
		return count("patient_id", patientId);
	}

	public int countForDoctor(long doctorId) {
		return count("doctor_id", doctorId);
	}

	public void updateWhen(long id, LocalDateTime startsAt, String reason) {
		jdbc.update(
				"UPDATE appointments SET starts_at = ?, reason = ? WHERE id = ? AND status = 'BOOKED'",
				Timestamp.valueOf(startsAt), reason, id);
	}

	public void cancel(long id) {
		jdbc.update("UPDATE appointments SET status = 'CANCELLED' WHERE id = ? AND status = 'BOOKED'", id);
	}

	public void delete(long id) {
		jdbc.update("DELETE FROM appointments WHERE id = ?", id);
	}

	private int count(String column, long id) {
		Integer count = jdbc.queryForObject(
				"SELECT COUNT(*) FROM appointments WHERE " + column + " = ?",
				Integer.class,
				id);
		return count == null ? 0 : count;
	}
}
