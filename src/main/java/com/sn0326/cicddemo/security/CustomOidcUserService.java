package com.sn0326.cicddemo.security;

import com.sn0326.cicddemo.model.OidcProvider;
import com.sn0326.cicddemo.service.OidcConnectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OIDC認証時のユーザー情報処理をカスタマイズするサービス
 * OIDCプロバイダー（Google等）から取得した情報を処理し、
 * 既存ユーザーとの連携を確認する
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private final OidcConnectionService oidcConnectionService;
    private final JdbcUserDetailsManager userDetailsManager;

    public CustomOidcUserService(
            OidcConnectionService oidcConnectionService,
            JdbcUserDetailsManager userDetailsManager) {
        this.oidcConnectionService = oidcConnectionService;
        this.userDetailsManager = userDetailsManager;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 親クラスでOIDCユーザー情報を取得
        OidcUser oidcUser = super.loadUser(userRequest);

        // プロバイダー名を取得（例: "google"）
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OidcProvider provider;

        try {
            provider = OidcProvider.fromValue(registrationId);
        } catch (IllegalArgumentException e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "サポートされていないプロバイダーです: " + registrationId
            );
        }

        // OIDCのsubクレーム（プロバイダー固有のユーザーID）を取得
        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();

        // セッションから連携モードかどうかを確認
        boolean isConnectionMode = isConnectionMode();

        if (isConnectionMode) {
            // 連携モード: 既存の連携チェックをスキップし、OidcAuthenticationSuccessHandlerで処理
            // セッションからローカルユーザー名を取得
            String connectingUsername = getConnectingUsername();

            // ローカルユーザー名をクレームに追加
            Map<String, Object> claims = new HashMap<>(oidcUser.getUserInfo().getClaims());
            claims.put("preferred_username", connectingUsername);
            OidcUserInfo userInfo = new OidcUserInfo(claims);

            // 一時的な権限でOIDCユーザーを返す（実際の権限はSuccessHandlerで処理後に適用される）
            return new DefaultOidcUser(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    oidcUser.getIdToken(),
                    userInfo,
                    "preferred_username"
            );
        }

        // ログインモード: 既存の連携を検索
        Optional<String> usernameOpt = oidcConnectionService.findUsernameByProviderId(provider, providerId);

        if (usernameOpt.isEmpty()) {
            // 連携が見つからない場合はエラー
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("connection_not_found"),
                    "このアカウントは連携されていません。先にログインしてアカウント連携を行ってください。"
            );
        }

        String username = usernameOpt.get();

        // ローカルユーザーの権限を取得
        UserDetails userDetails = userDetailsManager.loadUserByUsername(username);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // ローカルユーザー名をクレームに追加
        Map<String, Object> claims = new HashMap<>(oidcUser.getUserInfo().getClaims());
        claims.put("preferred_username", username);
        OidcUserInfo userInfo = new OidcUserInfo(claims);

        // ローカルユーザーの権限を持つOIDCユーザーオブジェクトを返す
        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                userInfo,
                "preferred_username"  // nameAttributeKey
        );
    }

    /**
     * セッションから連携モードかどうかを確認
     */
    private boolean isConnectionMode() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpSession session = attributes.getRequest().getSession(false);
                if (session != null) {
                    return Boolean.TRUE.equals(session.getAttribute("oidc_connection_mode"));
                }
            }
        } catch (Exception e) {
            // セッション取得に失敗した場合は連携モードではないとみなす
        }
        return false;
    }

    /**
     * セッションから連携中のローカルユーザー名を取得
     */
    private String getConnectingUsername() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpSession session = attributes.getRequest().getSession(false);
                if (session != null) {
                    Object username = session.getAttribute("connecting_username");
                    if (username instanceof String) {
                        return (String) username;
                    }
                }
            }
        } catch (Exception e) {
            // セッション取得に失敗した場合はnullを返す
        }
        return null;
    }
}
