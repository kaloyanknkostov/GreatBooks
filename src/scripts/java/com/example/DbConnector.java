package com.example;

import org.springframework.jdbc.core.JdbcTemplate;

public class DbConnector {

    private final JdbcTemplate jdbc;

    public DbConnector(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void tester(String username, String email, String password) {
        String sql =
            "INSERT INTO users (username, email, password) VALUES (?,?,?);";
        jdbc.update(sql, username, email, password);
    }
}
