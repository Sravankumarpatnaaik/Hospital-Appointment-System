package com.ashlin.clinic.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SpecializationDao {

	private final JdbcTemplate jdbc;

	public SpecializationDao(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public List<String> names() {
		return jdbc.query("SELECT name FROM specializations ORDER BY name", (rs, row) -> rs.getString("name"));
	}

	public Optional<Long> idOf(String name) {
		List<Long> ids = jdbc.query(
				"SELECT id FROM specializations WHERE name = ?",
				(rs, row) -> rs.getLong("id"),
				name);
		return ids.stream().findFirst();
	}

	public void insert(String name) {
		jdbc.update("INSERT IGNORE INTO specializations (name) VALUES (?)", name);
	}
}
