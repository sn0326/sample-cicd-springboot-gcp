package com.sn0326.cicddemo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class FormAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 入力されたユーザー名を取得
        String username = request.getParameter("username");

        // エラーメッセージの決定
        String errorMessage = getErrorMessage(exception);

        // ユーザー名をURLエンコードしてリダイレクト
        String redirectUrl = "/login?error";
        if (username != null && !username.isEmpty()) {
            redirectUrl += "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        }
        if (errorMessage != null) {
            redirectUrl += "&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        }

        setDefaultFailureUrl(redirectUrl);
        super.onAuthenticationFailure(request, response, exception);
    }

    private String getErrorMessage(AuthenticationException exception) {
        if (exception instanceof LockedException) {
            return "アカウントがロックされています。しばらくしてから再度お試しください。";
        } else if (exception instanceof DisabledException) {
            return "アカウントが無効です。";
        } else if (exception instanceof AccountExpiredException) {
            return "アカウントの有効期限が切れています。";
        } else if (exception instanceof BadCredentialsException) {
            return "ユーザー名またはパスワードが正しくありません。";
        }
        return "認証に失敗しました。";
    }
}
