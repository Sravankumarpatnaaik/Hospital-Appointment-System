package com.ashlin.clinic.dao;

import com.ashlin.clinic.model.DoctorCard;
import com.ashlin.clinic.model.LoginAccount;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class DoctorDao {

	private final JdbcTemplate jdbc;

	public DoctorDao(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public int countActive() {
		Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM doctors WHERE active = 1", Integer.class);
		return count == null ? 0 : count;
	}

	public long insert(String fullName, String email, String passwordHash, long specializationId, String room, boolean active) {
		KeyHolder keys = new GeneratedKeyHolder();
		jdbc.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(
					"""
					INSERT INTO doctors (full_name, email, password_hash, specialization_id, room, active)
					VALUES (?, ?, ?, ?, ?, ?)
					""",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, fullName);
			ps.setString(2, email);
			ps.setString(3, passwordHash);
			ps.setLong(4, specializationId);
			ps.setString(5, room);
			ps.setInt(6, active ? 1 : 0);
			return ps;
		}, keys);
		Number key = keys.getKey();
		if (key == null) {
			throw new IllegalStateException("Doctor insert did not return a key");
		}
		return key.longValue();
	}

	public void update(long id, String fullName, String email, String passwordHashOrNull, long specializationId, String room, boolean active) {
		jdbc.update(
				"""
				UPDATE doctors
				SET full_name = ?, email = ?, password_hash = COALESCE(?, password_hash),
				    specialization_id = ?, room = ?, active = ?
				WHERE id = ?
				""",
				fullName, email, passwordHashOrNull, specializationId, room, active ? 1 : 0, id);
	}

	public Optional<DoctorCard> find(long id) {
		return queryCards("WHERE d.id = ?", id).stream().findFirst();
	}

	public Optional<LoginAccount> findForLogin(String email) {
		List<LoginAccount> rows = jdbc.query(
				"""
				SELECT id, full_name, email, password_hash, active
				FROM doctors WHERE email = ?
				""",
				(rs, row) -> new LoginAccount(
						rs.getLong("id"),
						rs.getString("full_name"),
						rs.getString("email"),
						rs.getString("password_hash"),
						rs.getInt("active") == 1),
				email);
		return rows.stream().findFirst();
	}

	public List<DoctorCard> list(boolean activeOnly, String specialization) {
		String where = """
				WHERE (? = 0 OR d.active = 1)
				  AND (? = '' OR s.name = ?)
				""";
		return jdbc.query(
				cardSql(where) + " ORDER BY s.name, d.full_name",
				(rs, row) -> map(rs),
				activeOnly ? 1 : 0,
				specialization == null ? "" : specialization,
				specialization == null ? "" : specialization);
	}

	public void lock(long id) {
		try {
			jdbc.queryForObject("SELECT id FROM doctors WHERE id = ? FOR UPDATE", Long.class, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new com.ashlin.clinic.support.ClinicException("No doctor has that id.");
		}
	}

	public boolean emailTaken(String email, Long exceptId) {
		Integer count = exceptId == null
				? jdbc.queryForObject("SELECT COUNT(*) FROM doctors WHERE email = ?", Integer.class, email)
				: jdbc.queryForObject("SELECT COUNT(*) FROM doctors WHERE email = ? AND id <> ?", Integer.class, email, exceptId);
		return count != null && count > 0;
	}

	public void delete(long id) {
		jdbc.update("DELETE FROM doctors WHERE id = ?", id);
	}

	private List<DoctorCard> queryCards(String where, Object arg) {
		return jdbc.query(cardSql(where), (rs, row) -> map(rs), arg);
	}

	private static String cardSql(String where) {
		return """
				SELECT d.id, d.full_name, d.email, d.room, d.active, s.name AS specialization
				FROM doctors d
				JOIN specializations s ON s.id = d.specialization_id
				""" + where;
	}

	private static DoctorCard map(java.sql.ResultSet rs) throws java.sql.SQLException {
		return new DoctorCard(
				rs.getLong("id"),
				rs.getString("full_name"),
				rs.getString("email"),
				rs.getString("specialization"),
				rs.getString("room"),
				rs.getInt("active") == 1);
	}
}
