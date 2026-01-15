package com.sn0326.cicddemo.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ログイン履歴を管理するサービス
 */
@Service
public class LastLoginService {

    private final JdbcTemplate jdbcTemplate;

    public LastLoginService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * ログイン履歴を記録する
     *
     * @param username ユーザー名
     * @param loginMethod ログイン方式 ("FORM" or "OIDC")
     * @param oidcProvider OIDCプロバイダー名（OIDCの場合のみ）
     * @param request HTTPリクエスト（IP、User-Agent取得用）
     */
    @Transactional
    public void recordLogin(String username, String loginMethod, String oidcProvider, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        String sql = "INSERT INTO user_login_history (username, login_method, oidc_provider, ip_address, user_agent, success) " +
                     "VALUES (?, ?, ?, ?, ?, true)";

        jdbcTemplate.update(sql, username, loginMethod, oidcProvider, ipAddress, userAgent);
    }

    /**
     * 前回のログイン日時を取得する（現在のログインを除く）
     *
     * @param username ユーザー名
     * @return 前回ログイン日時（存在しない場合はOptional.empty()）
     */
    public Optional<LocalDateTime> getLastLogin(String username) {
        String sql = "SELECT login_at FROM user_login_history " +
                     "WHERE username = ? AND success = true " +
                     "ORDER BY login_at DESC " +
                     "LIMIT 1 OFFSET 1";

        try {
            LocalDateTime lastLogin = jdbcTemplate.queryForObject(sql, LocalDateTime.class, username);
            return Optional.ofNullable(lastLogin);
        } catch (Exception e) {
            // データが存在しない場合
            return Optional.empty();
        }
    }

    /**
     * クライアントのIPアドレスを取得する
     * プロキシ経由の場合はX-Forwarded-Forヘッダーを確認
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-Forには複数のIPが含まれる場合があるため、最初のIPを取得
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
