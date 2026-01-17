-- Spring Security標準のユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL,
    password_must_change BOOLEAN NOT NULL DEFAULT FALSE
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

-- WebAuthn/Passkey関連テーブル

-- パスキーユーザーエンティティテーブル
-- WebAuthnの公開鍵クレデンシャルユーザーエンティティ情報を格納
CREATE TABLE IF NOT EXISTS passkey_user_entities (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- パスキークレデンシャルテーブル
-- 各デバイスのパスキー（公開鍵）情報を格納
CREATE TABLE IF NOT EXISTS passkey_credentials (
    id VARCHAR(255) PRIMARY KEY,
    user_entity_id VARCHAR(255) NOT NULL,
    credential_public_key BYTEA NOT NULL,
    signature_count BIGINT NOT NULL,
    uv_initialized BOOLEAN NOT NULL,
    transports VARCHAR(255),
    backup_eligible BOOLEAN NOT NULL,
    backup_state BOOLEAN NOT NULL,
    attestation_object BYTEA,
    attestation_client_data_json BYTEA,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    label VARCHAR(255),  -- デバイス名（例：「iPhone 15 Pro」「MacBook Pro」）
    CONSTRAINT fk_passkey_credentials_user_entity
        FOREIGN KEY(user_entity_id)
        REFERENCES passkey_user_entities(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_passkey_credentials_user_entity_id
    ON passkey_credentials(user_entity_id);

-- ユーザーとパスキーの紐付けテーブル
-- 既存のusersテーブル（username）とpasskey_user_entities（id）を紐付け
CREATE TABLE IF NOT EXISTS user_passkey_bindings (
    username VARCHAR(50) NOT NULL,
    user_entity_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (username, user_entity_id),
    CONSTRAINT fk_passkey_bindings_users
        FOREIGN KEY(username)
        REFERENCES users(username)
        ON DELETE CASCADE,
    CONSTRAINT fk_passkey_bindings_entity
        FOREIGN KEY(user_entity_id)
        REFERENCES passkey_user_entities(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_passkey_bindings_username
    ON user_passkey_bindings(username);
CREATE INDEX IF NOT EXISTS idx_user_passkey_bindings_user_entity_id
    ON user_passkey_bindings(user_entity_id);
