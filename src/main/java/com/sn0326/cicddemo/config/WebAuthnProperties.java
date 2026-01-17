package com.sn0326.cicddemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * WebAuthn/Passkey機能の設定プロパティ
 *
 * application.yamlのwebauthn配下の設定を管理します。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "webauthn")
public class WebAuthnProperties {

    /**
     * Relying Party設定
     */
    private RelyingParty rp = new RelyingParty();

    /**
     * パスキー認証を許可するオリジン（URL）のリスト
     * 例: ["https://example.com", "http://localhost:8080"]
     */
    private List<String> allowedOrigins;

    /**
     * Relying Party（RP）の設定情報
     */
    @Data
    public static class RelyingParty {
        /**
         * アプリケーションの識別子（ドメイン名）
         * 例: "example.com"
         */
        private String id;

        /**
         * アプリケーションの表示名
         * ブラウザの認証ダイアログに表示されます
         * 例: "CICD Demo Application"
         */
        private String name;
    }

    // Getter methods for convenience
    public String getRpId() {
        return rp.getId();
    }

    public String getRpName() {
        return rp.getName();
    }
}
