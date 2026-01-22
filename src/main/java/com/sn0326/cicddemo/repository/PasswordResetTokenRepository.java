package com.sn0326.cicddemo.repository;

import com.sn0326.cicddemo.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * パスワードリセットトークンのリポジトリ
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    /**
     * トークンハッシュでトークン情報を取得
     * @param tokenHash トークンのSHA-256ハッシュ値
     * @return パスワードリセットトークン
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * 指定ユーザーの未使用トークンを削除
     * @param username ユーザー名
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.username = :username AND t.usedAt IS NULL")
    void deleteUnusedTokensByUsername(@Param("username") String username);

    /**
     * 期限切れトークンを削除
     * @param dateTime 基準日時（これより古いexpiryDateを持つトークンを削除）
     * @return 削除された行数
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :dateTime")
    int deleteExpiredTokens(@Param("dateTime") LocalDateTime dateTime);
}
