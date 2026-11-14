package com.ashlin.clinic.dao;

import com.ashlin.clinic.model.LoginAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdminDao {

	private final JdbcTemplate jdbc;

	public AdminDao(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public void insert(String fullName, String email, String passwordHash) {
		jdbc.update(
				"INSERT INTO admins (full_name, email, password_hash) VALUES (?, ?, ?)",
				fullName, email, passwordHash);
	}

	public Optional<LoginAccount> findForLogin(String email) {
		List<LoginAccount> rows = jdbc.query(
				"SELECT id, full_name, email, password_hash FROM admins WHERE email = ?",
				(rs, row) -> new LoginAccount(
						rs.getLong("id"),
						rs.getString("full_name"),
						rs.getString("email"),
						rs.getString("password_hash"),
						true),
				email);
		return rows.stream().findFirst();
	}

	public boolean emailTaken(String email) {
		Integer count = jdbc.queryForObject(
				"SELECT COUNT(*) FROM admins WHERE email = ?",
				Integer.class,
				email);
		return count != null && count > 0;
	}
}
