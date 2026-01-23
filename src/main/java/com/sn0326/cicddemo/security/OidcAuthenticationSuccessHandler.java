package com.sn0326.cicddemo.security;

import com.sn0326.cicddemo.model.OidcProvider;
import com.sn0326.cicddemo.service.OidcConnectionService;
import com.sn0326.cicddemo.service.LastLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OIDC認証成功時の処理を行うハンドラー
 */
@Component
public class OidcAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final OidcConnectionService oidcConnectionService;
    private final LastLoginService lastLoginService;

    public OidcAuthenticationSuccessHandler(
            OidcConnectionService oidcConnectionService,
            LastLoginService lastLoginService) {
        this.oidcConnectionService = oidcConnectionService;
        this.lastLoginService = lastLoginService;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        // OIDC連携モードかどうかを確認
        HttpSession session = request.getSession(false);
        boolean isConnectionMode = session != null &&
                Boolean.TRUE.equals(session.getAttribute("oidc_connection_mode"));

        if (isConnectionMode && authentication instanceof OAuth2AuthenticationToken) {
            // 連携モード: ログイン済みユーザーがOIDCアカウントを連携している
            handleConnectionMode(request, response, (OAuth2AuthenticationToken) authentication);
        } else {
            // ログインモード: OIDCでログインしている
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                String username = authentication.getName();
                String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

                // ログイン履歴を記録
                lastLoginService.recordLogin(username, "OIDC", registrationId, request);
            }

            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    /**
     * OIDC連携モードの処理
     */
    private void handleConnectionMode(
            HttpServletRequest request,
            HttpServletResponse response,
            OAuth2AuthenticationToken authentication) throws IOException {

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("connecting_username");

        if (username == null) {
            response.sendRedirect("/login?error=invalid_session");
            return;
        }

        try {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String registrationId = authentication.getAuthorizedClientRegistrationId();
            OidcProvider provider = OidcProvider.fromValue(registrationId);

            String providerId = oidcUser.getSubject();
            String email = oidcUser.getEmail();

            // 連携を作成または更新
            oidcConnectionService.createOrUpdateConnection(username, provider, providerId, email);

            // セッションをクリア
            session.removeAttribute("oidc_connection_mode");
            session.removeAttribute("connecting_username");

            // プロフィールページにリダイレクト
            response.sendRedirect("/profile?connection_success");

        } catch (Exception e) {
            response.sendRedirect("/profile?connection_error");
        }
    }
}
