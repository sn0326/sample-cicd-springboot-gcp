package com.sn0326.cicddemo.security;

import com.sn0326.cicddemo.service.ForcePasswordChangeService;
import com.sn0326.cicddemo.service.LastLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * フォーム認証成功時の処理を行うハンドラー
 */
@Component
public class FormAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final LastLoginService lastLoginService;
    private final ForcePasswordChangeService forcePasswordChangeService;

    public FormAuthenticationSuccessHandler(
            LastLoginService lastLoginService,
            ForcePasswordChangeService forcePasswordChangeService) {
        this.lastLoginService = lastLoginService;
        this.forcePasswordChangeService = forcePasswordChangeService;
        setDefaultTargetUrl("/home");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        // ログイン履歴を記録
        String username = authentication.getName();
        lastLoginService.recordLogin(username, "FORM", null, request);

        // パスワード変更が必要な場合は専用ページへリダイレクト
        if (forcePasswordChangeService.isPasswordChangeRequired(username)) {
            getRedirectStrategy().sendRedirect(request, response, "/force-change-password");
            return;
        }

        // デフォルトの成功処理（リダイレクト）を実行
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
