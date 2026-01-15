package com.sn0326.cicddemo.repository;

import com.sn0326.cicddemo.model.UserOidcConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OIDC連携情報のリポジトリ
 */
@Repository
public interface UserOidcConnectionRepository extends JpaRepository<UserOidcConnection, Long> {

    /**
     * ユーザー名とプロバイダーで連携情報を検索
     */
    Optional<UserOidcConnection> findByUsernameAndProvider(String username, String provider);

    /**
     * プロバイダーとプロバイダーIDで連携情報を検索
     */
    Optional<UserOidcConnection> findByProviderAndProviderId(String provider, String providerId);

    /**
     * ユーザー名で全ての連携情報を取得
     */
    List<UserOidcConnection> findByUsername(String username);

    /**
     * ユーザー名とプロバイダーで連携が存在するか確認
     */
    boolean existsByUsernameAndProvider(String username, String provider);

    /**
     * プロバイダーとプロバイダーIDで連携が存在するか確認
     */
    boolean existsByProviderAndProviderId(String provider, String providerId);
}
