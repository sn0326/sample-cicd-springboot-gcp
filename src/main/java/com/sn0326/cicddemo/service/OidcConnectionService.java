package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.dto.OidcConnectionInfo;
import com.sn0326.cicddemo.exception.ResourceNotFoundException;
import com.sn0326.cicddemo.exception.UserValidationException;
import com.sn0326.cicddemo.model.OidcProvider;
import com.sn0326.cicddemo.model.UserOidcConnection;
import com.sn0326.cicddemo.repository.UserOidcConnectionRepository;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OIDC連携を管理するサービス
 */
@Service
public class OidcConnectionService {

    private final UserOidcConnectionRepository oidcConnectionRepository;
    private final JdbcUserDetailsManager userDetailsManager;

    public OidcConnectionService(
            UserOidcConnectionRepository oidcConnectionRepository,
            JdbcUserDetailsManager userDetailsManager) {
        this.oidcConnectionRepository = oidcConnectionRepository;
        this.userDetailsManager = userDetailsManager;
    }

    /**
     * OIDC連携を作成または更新する
     */
    @Transactional
    public UserOidcConnection createOrUpdateConnection(
            String username,
            OidcProvider provider,
            String providerId,
            String email) {

        // ユーザーが存在するか確認
        if (!userDetailsManager.userExists(username)) {
            throw new UserValidationException("ユーザー '" + username + "' が存在しません");
        }

        // 既存の連携を確認
        Optional<UserOidcConnection> existing = oidcConnectionRepository
                .findByUsernameAndProvider(username, provider.getValue());

        if (existing.isPresent()) {
            // 既存連携を更新
            UserOidcConnection connection = existing.get();
            connection.setProviderId(providerId);
            connection.setEmail(email);
            connection.setEnabled(true);
            return oidcConnectionRepository.save(connection);
        } else {
            // 新規連携を作成
            UserOidcConnection connection = new UserOidcConnection(
                    username,
                    provider.getValue(),
                    providerId,
                    email
            );
            return oidcConnectionRepository.save(connection);
        }
    }

    /**
     * プロバイダーIDからユーザー名を検索する
     */
    public Optional<String> findUsernameByProviderId(OidcProvider provider, String providerId) {
        return oidcConnectionRepository
                .findByProviderAndProviderId(provider.getValue(), providerId)
                .filter(UserOidcConnection::isEnabled)
                .map(UserOidcConnection::getUsername);
    }

    /**
     * ユーザーの連携状態を確認する
     */
    public boolean isConnected(String username, OidcProvider provider) {
        return oidcConnectionRepository
                .findByUsernameAndProvider(username, provider.getValue())
                .map(UserOidcConnection::isEnabled)
                .orElse(false);
    }

    /**
     * ユーザーの全OIDC連携を取得する
     */
    public List<OidcConnectionInfo> getUserConnections(String username) {
        return oidcConnectionRepository.findByUsername(username)
                .stream()
                .map(this::toConnectionInfo)
                .collect(Collectors.toList());
    }

    /**
     * 特定のOIDC連携を取得する
     */
    public Optional<OidcConnectionInfo> getConnection(String username, OidcProvider provider) {
        return oidcConnectionRepository
                .findByUsernameAndProvider(username, provider.getValue())
                .map(this::toConnectionInfo);
    }

    /**
     * OIDC連携を無効化する
     */
    @Transactional
    public void disableConnection(String username, OidcProvider provider) {
        UserOidcConnection connection = oidcConnectionRepository
                .findByUsernameAndProvider(username, provider.getValue())
                .orElseThrow(() -> new ResourceNotFoundException("連携が見つかりません"));

        connection.setEnabled(false);
        oidcConnectionRepository.save(connection);
    }

    /**
     * OIDC連携を削除する
     */
    @Transactional
    public void deleteConnection(String username, OidcProvider provider) {
        UserOidcConnection connection = oidcConnectionRepository
                .findByUsernameAndProvider(username, provider.getValue())
                .orElseThrow(() -> new ResourceNotFoundException("連携が見つかりません"));

        oidcConnectionRepository.delete(connection);
    }

    /**
     * エンティティをDTOに変換
     */
    private OidcConnectionInfo toConnectionInfo(UserOidcConnection connection) {
        return new OidcConnectionInfo(
                connection.getId(),
                connection.getProvider(),
                connection.getEmail(),
                connection.isEnabled(),
                connection.getCreatedAt()
        );
    }
}
