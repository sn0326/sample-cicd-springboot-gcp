package com.sn0326.cicddemo.security;

import com.sn0326.cicddemo.service.AccountLockoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

/**
 * 認証失敗イベントをハンドリングするリスナー
 * TERASOLUNAガイドラインに準拠した実装
 */
@Component
public class AuthenticationFailureEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureEventListener.class);

    private final AccountLockoutService lockoutService;

    public AuthenticationFailureEventListener(AccountLockoutService lockoutService) {
        this.lockoutService = lockoutService;
    }

    /**
     * パスワード誤りによる認証失敗時に呼ばれる
     * 失敗を記録し、ロック状態をログに記録（ユーザーには通知しない）
     *
     * @param event 認証失敗イベント
     */
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();

        logger.info("Authentication failed for user: {}", username);

        try {
            // 失敗を記録（独立したトランザクション）
            lockoutService.recordFailedAttempt(username);

            // ロック状態をログに記録（ユーザーには通知しない）
            if (lockoutService.isAccountLocked(username)) {
                logger.warn("Account locked due to multiple failed attempts: {}", username);
            }
        } catch (Exception e) {
            // DB障害時はログのみ（認証失敗自体は正常に処理される）
            logger.error("Failed to record authentication failure for user: {}", username, e);
        }
    }
}
