-- Spring Security標準のユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL,
    password_must_change BOOLEAN NOT NULL DEFAULT FALSE,
    email VARCHAR(255)
);

-- Spring Security標準の権限テーブル
CREATE TABLE IF NOT EXISTS authorities (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY(username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_username ON authorities (username, authority);

-- OIDC連携情報テーブル
CREATE TABLE IF NOT EXISTS user_oidc_connections (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_oidc_users FOREIGN KEY(username) REFERENCES users(username) ON DELETE CASCADE,
    CONSTRAINT uk_provider_id UNIQUE (provider, provider_id)
);

CREATE INDEX IF NOT EXISTS idx_oidc_username ON user_oidc_connections(username);
CREATE INDEX IF NOT EXISTS idx_oidc_provider_id ON user_oidc_connections(provider, provider_id);

-- ログインテーブル
CREATE TABLE IF NOT EXISTS user_logins (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL,
    logged_in_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    login_method    VARCHAR(20) NOT NULL,     -- 'FORM', 'OIDC', or 'PASSKEY'
    oidc_provider   VARCHAR(20),              -- 'google' (OIDCの場合のみ)
    ip_address      VARCHAR(45),              -- IPv4/IPv6対応
    user_agent      VARCHAR(500),
    success         BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_user_logins_users FOREIGN KEY(username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_logins_username ON user_logins(username);
CREATE INDEX IF NOT EXISTS idx_user_logins_logged_in_at ON user_logins(logged_in_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_logins_username_logged_in_at ON user_logins(username, logged_in_at DESC);

-- 弱いパスワードテーブル（NIST SP 800-63B準拠）
CREATE TABLE IF NOT EXISTS weak_passwords (
    id SERIAL PRIMARY KEY,
    password VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_weak_passwords_password ON weak_passwords(password);

-- ログイン失敗記録テーブル（アカウントロックアウト機能）
CREATE TABLE IF NOT EXISTS failed_authentications (
    username VARCHAR(50) NOT NULL,
    authentication_timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (username, authentication_timestamp),
    CONSTRAINT fk_failed_auth_users FOREIGN KEY(username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_failed_auth_lookup ON failed_authentications(username, authentication_timestamp DESC);

-- Remember Me永続トークンテーブル（Spring Security標準）
CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_persistent_logins_username ON persistent_logins(username);
CREATE INDEX IF NOT EXISTS idx_persistent_logins_last_used ON persistent_logins(last_used);

-- WebAuthn/Passkey関連テーブル (Spring Security 7.0 標準スキーマ)

-- パスキーユーザーエンティティテーブル
-- Spring Security標準のuser_entitiesテーブル
CREATE TABLE IF NOT EXISTS user_entities (
    id VARCHAR(1000) NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    PRIMARY KEY (id)
);

-- パスキークレデンシャルテーブル
-- Spring Security標準のuser_credentialsテーブル（PostgreSQL用にblobをbyteaに変換）
CREATE TABLE IF NOT EXISTS user_credentials (
    credential_id VARCHAR(1000) NOT NULL,
    user_entity_user_id VARCHAR(1000) NOT NULL,
    public_key BYTEA NOT NULL,
    signature_count BIGINT,
    uv_initialized BOOLEAN,
    backup_eligible BOOLEAN NOT NULL,
    authenticator_transports VARCHAR(1000),
    public_key_credential_type VARCHAR(100),
    backup_state BOOLEAN NOT NULL,
    attestation_object BYTEA,
    attestation_client_data_json BYTEA,
    created TIMESTAMP,
    last_used TIMESTAMP,
    label VARCHAR(1000) NOT NULL,
    PRIMARY KEY (credential_id)
);

-- ユーザーとパスキーの紐付けテーブル
-- 既存のusersテーブル（username）とuser_entities（id）を紐付け
-- Spring SecurityのWebAuthn実装では直接の紐付けは不要だが、
-- 既存のユーザー管理システムとの統合のために保持
CREATE TABLE IF NOT EXISTS user_passkey_bindings (
    username VARCHAR(50) NOT NULL,
    user_entity_id VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (username, user_entity_id),
    CONSTRAINT fk_passkey_bindings_users
        FOREIGN KEY(username)
        REFERENCES users(username)
        ON DELETE CASCADE,
    CONSTRAINT fk_passkey_bindings_entity
        FOREIGN KEY(user_entity_id)
        REFERENCES user_entities(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_passkey_bindings_username
    ON user_passkey_bindings(username);
CREATE INDEX IF NOT EXISTS idx_user_passkey_bindings_user_entity_id
    ON user_passkey_bindings(user_entity_id);

-- パスワードリセットトークンテーブル（OWASP準拠）
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_hash VARCHAR(64) NOT NULL PRIMARY KEY,  -- SHA-256ハッシュ（Hex: 64文字）
    username VARCHAR(50) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    CONSTRAINT fk_password_reset_users
        FOREIGN KEY(username)
        REFERENCES users(username)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_password_reset_username ON password_reset_tokens(username);
CREATE INDEX IF NOT EXISTS idx_password_reset_expiry ON password_reset_tokens(expiry_date);

-- パスワードリセット試行記録テーブル（レート制限用）
CREATE TABLE IF NOT EXISTS password_reset_attempts (
    username VARCHAR(50) NOT NULL,
    attempt_time TIMESTAMP NOT NULL,
    PRIMARY KEY (username, attempt_time)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_attempts_time
    ON password_reset_attempts(attempt_time);
