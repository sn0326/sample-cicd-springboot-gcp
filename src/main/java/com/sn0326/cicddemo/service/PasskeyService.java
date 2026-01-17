package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.model.UserPasskeyBinding;
import com.sn0326.cicddemo.repository.UserPasskeyBindingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.UUID;

/**
 * パスキー（WebAuthn）の登録・管理サービス
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasskeyService {

    private final UserPasskeyBindingRepository userPasskeyBindingRepository;
    private final PublicKeyCredentialUserEntityRepository userEntityRepository;

    /**
     * ユーザーのパスキー登録を開始
     * 既存のパスキーエンティティがあれば再利用、なければ新規作成
     *
     * @param username    ユーザー名
     * @param displayName 表示名
     * @return パスキーユーザーエンティティID
     */
    @Transactional
    public String initiatePasskeyRegistration(String username, String displayName) {
        log.debug("パスキー登録開始: username={}", username);

        // 既存の紐付けを確認
        var existingBindings = userPasskeyBindingRepository.findByUsername(username);
        if (!existingBindings.isEmpty()) {
            String existingEntityId = existingBindings.get(0).getUserEntityId();
            log.debug("既存のパスキーエンティティを使用: entityId={}", existingEntityId);
            return existingEntityId;
        }

        // 新規パスキーエンティティを作成
        String userEntityId = generateUserEntityId();
        ImmutablePublicKeyCredentialUserEntity userEntity = ImmutablePublicKeyCredentialUserEntity.builder()
                .id(Bytes.fromBase64(userEntityId))
                .name(username)
                .displayName(displayName)
                .build();

        userEntityRepository.save(userEntity);
        log.debug("新規パスキーエンティティ作成: entityId={}", userEntityId);

        // ユーザーとパスキーエンティティを紐付け
        UserPasskeyBinding binding = new UserPasskeyBinding(username, userEntityId);
        userPasskeyBindingRepository.save(binding);
        log.info("パスキー登録準備完了: username={}, entityId={}", username, userEntityId);

        return userEntityId;
    }

    /**
     * ユーザーがパスキーを登録済みか確認
     *
     * @param username ユーザー名
     * @return 登録済みの場合true
     */
    @Transactional(readOnly = true)
    public boolean hasPasskey(String username) {
        return userPasskeyBindingRepository.existsByUsername(username);
    }

    /**
     * ユーザー名からパスキーエンティティIDを取得
     *
     * @param username ユーザー名
     * @return パスキーエンティティID（存在しない場合null）
     */
    @Transactional(readOnly = true)
    public String getUserEntityId(String username) {
        return userPasskeyBindingRepository.findByUsername(username)
                .stream()
                .findFirst()
                .map(UserPasskeyBinding::getUserEntityId)
                .orElse(null);
    }

    /**
     * パスキーエンティティIDからユーザー名を取得
     *
     * @param userEntityId パスキーエンティティID
     * @return ユーザー名（存在しない場合null）
     */
    @Transactional(readOnly = true)
    public String getUsernameByEntityId(String userEntityId) {
        return userPasskeyBindingRepository.findByUserEntityId(userEntityId)
                .map(UserPasskeyBinding::getUsername)
                .orElse(null);
    }

    /**
     * ユーザーのパスキー登録を削除
     *
     * @param username ユーザー名
     */
    @Transactional
    public void deletePasskey(String username) {
        log.info("パスキー削除開始: username={}", username);

        var bindings = userPasskeyBindingRepository.findByUsername(username);
        for (UserPasskeyBinding binding : bindings) {
            // パスキーエンティティを削除（CASCADE削除でクレデンシャルも削除される）
            userEntityRepository.delete(Bytes.fromBase64(binding.getUserEntityId()));
            // 紐付け情報を削除
            userPasskeyBindingRepository.delete(binding);
        }

        log.info("パスキー削除完了: username={}", username);
    }

    /**
     * ユーザーエンティティIDを生成
     * Base64エンコードされたUUIDを使用
     *
     * @return ユーザーエンティティID
     */
    private String generateUserEntityId() {
        UUID uuid = UUID.randomUUID();
        byte[] uuidBytes = new byte[16];
        System.arraycopy(
                java.nio.ByteBuffer.allocate(8).putLong(uuid.getMostSignificantBits()).array(),
                0, uuidBytes, 0, 8
        );
        System.arraycopy(
                java.nio.ByteBuffer.allocate(8).putLong(uuid.getLeastSignificantBits()).array(),
                0, uuidBytes, 8, 8
        );
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes);
    }
}
