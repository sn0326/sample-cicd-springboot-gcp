package com.sn0326.cicddemo.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBCを使用したパスワード変更要求リポジトリの実装
 */
@Repository
public class JdbcPasswordChangeRequirementRepository implements PasswordChangeRequirementRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPasswordChangeRequirementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isPasswordChangeRequired(String username) {
        String sql = "SELECT password_must_change FROM users WHERE username = ?";
        try {
            Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, username);
            return Boolean.TRUE.equals(result);
        } catch (EmptyResultDataAccessException e) {
            // ユーザーが存在しない場合はfalseを返す
            return false;
        }
    }

    @Override
    public void requirePasswordChange(String username) {
        String sql = "UPDATE users SET password_must_change = TRUE WHERE username = ?";
        jdbcTemplate.update(sql, username);
    }

    @Override
    public void clearPasswordChangeRequirement(String username) {
        String sql = "UPDATE users SET password_must_change = FALSE WHERE username = ?";
        jdbcTemplate.update(sql, username);
    }
}
