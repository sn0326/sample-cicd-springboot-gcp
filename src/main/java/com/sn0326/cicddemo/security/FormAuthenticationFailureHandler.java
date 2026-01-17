package com.sn0326.cicddemo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FormAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 入力されたユーザー名を取得
        String username = request.getParameter("username");

        // エラーメッセージの決定
        String errorMessage = getErrorMessage(exception);

        // セッションにユーザー名とエラーメッセージを保存（フラッシュアトリビュートとして）
        HttpSession session = request.getSession();
        if (username != null && !username.isEmpty()) {
            session.setAttribute("SPRING_SECURITY_LAST_USERNAME", username);
        }
        if (errorMessage != null) {
            session.setAttribute("SPRING_SECURITY_LAST_ERROR_MESSAGE", errorMessage);
        }

        setDefaultFailureUrl("/login?error");
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
