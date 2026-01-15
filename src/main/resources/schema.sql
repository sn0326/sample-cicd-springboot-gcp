-- Spring Security標準のユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL
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
    login_method    VARCHAR(20) NOT NULL,     -- 'FORM' or 'OIDC'
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
