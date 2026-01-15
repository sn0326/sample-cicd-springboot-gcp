package com.sn0326.cicddemo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OIDC認証失敗時の処理を行うハンドラー
 */
@Component
public class OidcAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public OidcAuthenticationFailureHandler() {
        setDefaultFailureUrl("/login?error");
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "認証に失敗しました";

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String errorCode = oauth2Exception.getError().getErrorCode();

            switch (errorCode) {
                case "connection_not_found":
                    errorMessage = "このアカウントは連携されていません。先にログインしてアカウント連携を行ってください。";
                    break;
                case "unsupported_provider":
                    errorMessage = "サポートされていないプロバイダーです";
                    break;
                default:
                    errorMessage = oauth2Exception.getError().getDescription();
                    if (errorMessage == null) {
                        errorMessage = "認証に失敗しました";
                    }
                    break;
            }
        }

        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        setDefaultFailureUrl("/login?error&message=" + encodedMessage);

        super.onAuthenticationFailure(request, response, exception);
    }
}
