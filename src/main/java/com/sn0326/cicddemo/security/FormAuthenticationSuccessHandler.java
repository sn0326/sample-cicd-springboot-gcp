package com.sn0326.cicddemo.security;

import com.sn0326.cicddemo.service.AccountLockoutService;
import com.sn0326.cicddemo.service.ForcePasswordChangeService;
import com.sn0326.cicddemo.service.LastLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * フォーム認証成功時の処理を行うハンドラー
 */
@Component
public class FormAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(FormAuthenticationSuccessHandler.class);

    private final LastLoginService lastLoginService;
    private final ForcePasswordChangeService forcePasswordChangeService;
    private final AccountLockoutService lockoutService;

    public FormAuthenticationSuccessHandler(
            LastLoginService lastLoginService,
            ForcePasswordChangeService forcePasswordChangeService,
            AccountLockoutService lockoutService) {
        this.lastLoginService = lastLoginService;
        this.forcePasswordChangeService = forcePasswordChangeService;
        this.lockoutService = lockoutService;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        String username = authentication.getName();

        // ログイン履歴を記録（独立したトランザクション）
        try {
            lastLoginService.recordLogin(username, "FORM", null, request);
        } catch (Exception e) {
            logger.error("Failed to record login for user: {}", username, e);
        }

        // 失敗記録をリセット（独立したトランザクション）
        try {
            lockoutService.resetFailedAttempts(username);
        } catch (Exception e) {
            logger.error("Failed to reset failed attempts for user: {}", username, e);
            // 次回成功時にリセットされるので継続
        }

        // パスワード変更が必要な場合は専用ページへリダイレクト
        if (forcePasswordChangeService.isPasswordChangeRequired(username)) {
            getRedirectStrategy().sendRedirect(request, response, "/force-change-password");
            return;
        }

        // デフォルトの成功処理（リダイレクト）を実行
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
