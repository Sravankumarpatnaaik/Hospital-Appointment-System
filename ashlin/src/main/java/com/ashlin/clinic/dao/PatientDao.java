package com.ashlin.clinic.dao;

import com.ashlin.clinic.model.LoginAccount;
import com.ashlin.clinic.model.PatientProfile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PatientDao {

	private final JdbcTemplate jdbc;

	public PatientDao(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public int count() {
		Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM patients", Integer.class);
		return count == null ? 0 : count;
	}

	public long insert(String fullName, String email, String phone, LocalDate dateOfBirth, String passwordHash, LocalDateTime createdAt) {
		KeyHolder keys = new GeneratedKeyHolder();
		jdbc.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"""
					INSERT INTO patients (full_name, email, phone, date_of_birth, password_hash, created_at)
					VALUES (?, ?, ?, ?, ?, ?)
					""",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, fullName);
			ps.setString(2, email);
			ps.setString(3, phone);
			ps.setObject(4, dateOfBirth);
			ps.setString(5, passwordHash);
			ps.setObject(6, createdAt);
			return ps;
		}, keys);
		Number key = keys.getKey();
		if (key == null) {
			throw new IllegalStateException("Patient insert did not return a key");
		}
		return key.longValue();
	}

	public void update(long id, String fullName, String email, String phone, LocalDate dateOfBirth, String passwordHashOrNull) {
		jdbc.update(
				"""
				UPDATE patients
				SET full_name = ?, email = ?, phone = ?, date_of_birth = ?,
				    password_hash = COALESCE(?, password_hash)
				WHERE id = ?
				""",
				fullName, email, phone, dateOfBirth, passwordHashOrNull, id);
	}

	public Optional<PatientProfile> find(long id) {
		List<PatientProfile> rows = jdbc.query(
				"""
				SELECT id, full_name, email, phone, date_of_birth
				FROM patients WHERE id = ?
				""",
				(rs, row) -> new PatientProfile(
						rs.getLong("id"),
						rs.getString("full_name"),
						rs.getString("email"),
						rs.getString("phone"),
						rs.getObject("date_of_birth", LocalDate.class)),
				id);
		return rows.stream().findFirst();
	}

	public Optional<LoginAccount> findForLogin(String email) {
		List<LoginAccount> rows = jdbc.query(
				"""
				SELECT id, full_name, email, password_hash
				FROM patients WHERE email = ?
				""",
				(rs, row) -> new LoginAccount(
						rs.getLong("id"),
						rs.getString("full_name"),
						rs.getString("email"),
						rs.getString("password_hash"),
						true),
				email);
		return rows.stream().findFirst();
	}

	public List<PatientProfile> list() {
		return jdbc.query(
				"""
				SELECT id, full_name, email, phone, date_of_birth
				FROM patients
				ORDER BY full_name
				""",
				(rs, row) -> new PatientProfile(
						rs.getLong("id"),
						rs.getString("full_name"),
						rs.getString("email"),
						rs.getString("phone"),
						rs.getObject("date_of_birth", LocalDate.class)));
	}

	public boolean emailTaken(String email, Long exceptId) {
		Integer count = exceptId == null
				? jdbc.queryForObject("SELECT COUNT(*) FROM patients WHERE email = ?", Integer.class, email)
				: jdbc.queryForObject("SELECT COUNT(*) FROM patients WHERE email = ? AND id <> ?", Integer.class, email, exceptId);
		return count != null && count > 0;
	}

	public void delete(long id) {
		jdbc.update("DELETE FROM patients WHERE id = ?", id);
	}
}
