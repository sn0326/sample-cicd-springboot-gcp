package com.sn0326.cicddemo.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * OWASP準拠の暗号学的に安全なトークン生成ユーティリティ
 *
 * パスワードリセットトークンの生成に使用します。
 * - 256ビット（32バイト）の暗号学的に安全な乱数を生成
 * - Base64URLエンコーディング（URLセーフ）
 * - SHA-256ハッシュによる安全な保存
 */
@Component
public class TokenGenerator {

    /**
     * 暗号学的に安全な乱数生成器（CSPRNG）
     * SecureRandomはスレッドセーフ
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * トークンのバイト長（256ビット = 32バイト）
     * OWASP推奨の128ビット以上のエントロピーを確保
     */
    private static final int TOKEN_BYTE_LENGTH = 32;

    /**
     * 暗号学的に安全なランダムトークンを生成
     *
     * @return Base64URLエンコードされたトークン（44文字、パディングなし）
     */
    public String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);

        // Base64URLエンコーディング（URLセーフ、パディングなし）
        // 結果は44文字の文字列（32バイト * 8 / 6 = 42.67 → 切り上げで43文字 + 1文字）
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    /**
     * トークンのSHA-256ハッシュを生成
     *
     * データベースに保存する際は、元のトークンではなくハッシュ値を保存することで、
     * データベース漏洩時のセキュリティリスクを軽減します。
     *
     * @param token 元のトークン
     * @return SHA-256ハッシュ値（16進数文字列、64文字）
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256は標準アルゴリズムなので、この例外は通常発生しない
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * バイト配列を16進数文字列に変換
     *
     * @param bytes バイト配列
     * @return 16進数文字列（小文字、各バイトは2文字で表現）
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
