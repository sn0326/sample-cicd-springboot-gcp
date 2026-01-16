package com.sn0326.cicddemo.security;

import com.sn0326.cicddemo.service.ForcePasswordChangeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * パスワード変更が必要なユーザーを専用ページへリダイレクトするフィルター
 * Spring Securityのフィルターチェーン内で動作し、認証後のアクセスを制御する
 */
@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    private static final String FORCE_CHANGE_PASSWORD_URL = "/force-change-password";
    private static final Set<String> ALLOWED_PATHS = Set.of(
            FORCE_CHANGE_PASSWORD_URL,
            "/logout"
    );

    private final ForcePasswordChangeService forcePasswordChangeService;

    public PasswordChangeRequiredFilter(ForcePasswordChangeService forcePasswordChangeService) {
        this.forcePasswordChangeService = forcePasswordChangeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 認証済みユーザーで、フォーム認証の場合のみチェック
        if (auth != null &&
            auth.isAuthenticated() &&
            !(auth instanceof AnonymousAuthenticationToken) &&
            auth instanceof UsernamePasswordAuthenticationToken) {

            String requestPath = request.getRequestURI();

            // 許可されたパスはスキップ
            if (ALLOWED_PATHS.stream().noneMatch(requestPath::startsWith)) {
                String username = auth.getName();

                // パスワード変更が必要な場合はリダイレクト
                if (forcePasswordChangeService.isPasswordChangeRequired(username)) {
                    response.sendRedirect(FORCE_CHANGE_PASSWORD_URL);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
