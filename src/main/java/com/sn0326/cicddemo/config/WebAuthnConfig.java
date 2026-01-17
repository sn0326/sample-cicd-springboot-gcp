package com.sn0326.cicddemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.UserCredential;

import javax.sql.DataSource;

/**
 * WebAuthn/Passkey機能のBean定義を管理する設定クラス
 */
@Configuration
public class WebAuthnConfig {

    /**
     * パスキーユーザーエンティティを管理するリポジトリ
     * JDBCベースの実装を使用してpasskey_user_entitiesテーブルに永続化
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
     * JDBCベースの実装を使用してpasskey_credentialsテーブルに永続化
     *
     * @param jdbcOperations JDBCオペレーション
     * @return UserCredentialRepository
     */
    @Bean
    public UserCredentialRepository userCredentialRepository(JdbcOperations jdbcOperations) {
        return new JdbcUserCredentialRepository(jdbcOperations);
    }

    /**
     * JDBCベースのPublicKeyCredentialUserEntityRepository実装
     * passkey_user_entitiesテーブルを使用
     */
    private static class JdbcPublicKeyCredentialUserEntityRepository
            implements PublicKeyCredentialUserEntityRepository {

        private final JdbcOperations jdbcOperations;

        public JdbcPublicKeyCredentialUserEntityRepository(JdbcOperations jdbcOperations) {
            this.jdbcOperations = jdbcOperations;
        }

        @Override
        public void save(PublicKeyCredentialUserEntity userEntity) {
            String sql = """
                INSERT INTO passkey_user_entities (id, name, display_name)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    display_name = EXCLUDED.display_name
                """;
            jdbcOperations.update(sql,
                    userEntity.getId(),
                    userEntity.getName(),
                    userEntity.getDisplayName());
        }

        @Override
        public PublicKeyCredentialUserEntity findByUsername(String username) {
            String sql = """
                SELECT pe.id, pe.name, pe.display_name
                FROM passkey_user_entities pe
                INNER JOIN user_passkey_bindings upb ON pe.id = upb.user_entity_id
                WHERE upb.username = ?
                LIMIT 1
                """;
            return jdbcOperations.query(sql,
                    (rs, rowNum) -> PublicKeyCredentialUserEntity.builder()
                            .id(rs.getString("id"))
                            .name(rs.getString("name"))
                            .displayName(rs.getString("display_name"))
                            .build(),
                    username).stream().findFirst().orElse(null);
        }

        @Override
        public PublicKeyCredentialUserEntity findById(String id) {
            String sql = """
                SELECT id, name, display_name
                FROM passkey_user_entities
                WHERE id = ?
                """;
            return jdbcOperations.query(sql,
                    (rs, rowNum) -> PublicKeyCredentialUserEntity.builder()
                            .id(rs.getString("id"))
                            .name(rs.getString("name"))
                            .displayName(rs.getString("display_name"))
                            .build(),
                    id).stream().findFirst().orElse(null);
        }

        @Override
        public void delete(String id) {
            String sql = "DELETE FROM passkey_user_entities WHERE id = ?";
            jdbcOperations.update(sql, id);
        }
    }

    /**
     * JDBCベースのUserCredentialRepository実装
     * passkey_credentialsテーブルを使用
     */
    private static class JdbcUserCredentialRepository implements UserCredentialRepository {

        private final JdbcOperations jdbcOperations;

        public JdbcUserCredentialRepository(JdbcOperations jdbcOperations) {
            this.jdbcOperations = jdbcOperations;
        }

        @Override
        public void save(UserCredential credential) {
            String sql = """
                INSERT INTO passkey_credentials (
                    id, user_entity_id, credential_public_key, signature_count,
                    uv_initialized, transports, backup_eligible, backup_state,
                    attestation_object, attestation_client_data_json, label
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    signature_count = EXCLUDED.signature_count,
                    last_used_at = CURRENT_TIMESTAMP,
                    label = EXCLUDED.label
                """;
            jdbcOperations.update(sql,
                    credential.getCredentialId(),
                    credential.getUserEntityUserId(),
                    credential.getPublicKey(),
                    credential.getSignatureCount(),
                    credential.isUvInitialized(),
                    credential.getTransports() != null ? String.join(",", credential.getTransports()) : null,
                    credential.isBackupEligible(),
                    credential.isBackupState(),
                    credential.getAttestationObject(),
                    credential.getAttestationClientDataJSON(),
                    credential.getLabel());
        }

        @Override
        public UserCredential findByCredentialId(String credentialId) {
            String sql = """
                SELECT id, user_entity_id, credential_public_key, signature_count,
                       uv_initialized, transports, backup_eligible, backup_state,
                       attestation_object, attestation_client_data_json, label
                FROM passkey_credentials
                WHERE id = ?
                """;
            return jdbcOperations.query(sql,
                    (rs, rowNum) -> {
                        String transports = rs.getString("transports");
                        return UserCredential.builder()
                                .credentialId(rs.getString("id"))
                                .userEntityUserId(rs.getString("user_entity_id"))
                                .publicKey(rs.getBytes("credential_public_key"))
                                .signatureCount(rs.getLong("signature_count"))
                                .uvInitialized(rs.getBoolean("uv_initialized"))
                                .transports(transports != null ? java.util.Arrays.asList(transports.split(",")) : null)
                                .backupEligible(rs.getBoolean("backup_eligible"))
                                .backupState(rs.getBoolean("backup_state"))
                                .attestationObject(rs.getBytes("attestation_object"))
                                .attestationClientDataJSON(rs.getBytes("attestation_client_data_json"))
                                .label(rs.getString("label"))
                                .build();
                    },
                    credentialId).stream().findFirst().orElse(null);
        }

        @Override
        public java.util.List<UserCredential> findByUserId(String userId) {
            String sql = """
                SELECT id, user_entity_id, credential_public_key, signature_count,
                       uv_initialized, transports, backup_eligible, backup_state,
                       attestation_object, attestation_client_data_json, label
                FROM passkey_credentials
                WHERE user_entity_id = ?
                ORDER BY created_at DESC
                """;
            return jdbcOperations.query(sql,
                    (rs, rowNum) -> {
                        String transports = rs.getString("transports");
                        return UserCredential.builder()
                                .credentialId(rs.getString("id"))
                                .userEntityUserId(rs.getString("user_entity_id"))
                                .publicKey(rs.getBytes("credential_public_key"))
                                .signatureCount(rs.getLong("signature_count"))
                                .uvInitialized(rs.getBoolean("uv_initialized"))
                                .transports(transports != null ? java.util.Arrays.asList(transports.split(",")) : null)
                                .backupEligible(rs.getBoolean("backup_eligible"))
                                .backupState(rs.getBoolean("backup_state"))
                                .attestationObject(rs.getBytes("attestation_object"))
                                .attestationClientDataJSON(rs.getBytes("attestation_client_data_json"))
                                .label(rs.getString("label"))
                                .build();
                    },
                    userId);
        }

        @Override
        public void delete(String credentialId) {
            String sql = "DELETE FROM passkey_credentials WHERE id = ?";
            jdbcOperations.update(sql, credentialId);
        }
    }
}
