package com.sn0326.cicddemo.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JdbcTemplateを使用したUserRepositoryの実装
 *
 * Spring Securityのusersテーブルに対する
 * カスタムなデータアクセスを提供します。
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * ユーザー名からメールアドレスを取得
     *
     * @param username ユーザー名
     * @return メールアドレス（見つからない場合はnull）
     */
    @Override
    public String findEmailByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT email FROM users WHERE username = ?",
                    String.class,
                    username
            );
        } catch (EmptyResultDataAccessException e) {
            log.debug("Email not found for user: {}", username);
            return null;
        } catch (Exception e) {
            log.warn("Failed to retrieve email for user: {}", username, e);
            return null;
        }
    }

    /**
     * パスワードを更新し、パスワード変更必須フラグをクリア
     *
     * @param username ユーザー名
     * @param encodedPassword エンコード済みパスワード
     * @return 更新された行数
     */
    @Override
    public int updatePasswordAndClearMustChangeFlag(String username, String encodedPassword) {
        return jdbcTemplate.update(
                "UPDATE users SET password = ?, password_must_change = false WHERE username = ?",
                encodedPassword,
                username
        );
    }
}
