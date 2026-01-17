package com.sn0326.cicddemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;

/**
 * WebAuthn/Passkey機能のBean定義を管理する設定クラス
 */
@Configuration
public class WebAuthnConfig {

    /**
     * パスキーユーザーエンティティを管理するリポジトリ
     * Spring Security提供のJDBC実装を使用
     *
     * @param jdbcOperations JDBCオペレーション
     * @return PublicKeyCredentialUserEntityRepository
     */
    @Bean
    public PublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository(
            JdbcOperations jdbcOperations) {
        return new JdbcPublicKeyCredentialUserEntityRepository(jdbcOperations);
    }

    /**
     * パスキークレデンシャル（公開鍵）を管理するリポジトリ
     * Spring Security提供のJDBC実装を使用
     *
     * @param jdbcOperations JDBCオペレーション
     * @return UserCredentialRepository
     */
    @Bean
    public UserCredentialRepository userCredentialRepository(JdbcOperations jdbcOperations) {
        return new JdbcUserCredentialRepository(jdbcOperations);
    }
}
