package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.model.UserLogin;
import com.sn0326.cicddemo.repository.UserLoginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ログインを管理するサービス
 */
@Service
public class LastLoginService {

    private final UserLoginRepository userLoginRepository;

    public LastLoginService(UserLoginRepository userLoginRepository) {
        this.userLoginRepository = userLoginRepository;
    }

    /**
     * ログインを記録する
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

        UserLogin userLogin = new UserLogin(username, loginMethod, oidcProvider, ipAddress, userAgent);
        userLoginRepository.save(userLogin);
    }

    /**
     * 前回のログイン日時を取得する（現在のログインを除く）
     *
     * @param username ユーザー名
     * @return 前回ログイン日時（存在しない場合はOptional.empty()）
     */
    public Optional<LocalDateTime> getLastLogin(String username) {
        try {
            LocalDateTime lastLogin = userLoginRepository.findLastLoginTime(username);
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
