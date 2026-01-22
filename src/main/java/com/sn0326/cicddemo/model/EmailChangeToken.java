package com.sn0326.cicddemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * メールアドレス変更トークンを保持するエンティティ
 * OWASP準拠のメールアドレス変更機能用
 */
@Entity
@Table(name = "email_change_tokens")
@Getter
@Setter
@NoArgsConstructor
public class EmailChangeToken {

    /**
     * トークンのSHA-256ハッシュ値（Hex: 64文字）
     * セキュリティのため、元のトークンは保存せずハッシュのみを保存
     */
    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    /**
     * ユーザー名
     */
    @Column(nullable = false, length = 50)
    private String username;

    /**
     * 新しいメールアドレス
     */
    @Column(name = "new_email", nullable = false, length = 255)
    private String newEmail;

    /**
     * トークンの有効期限（デフォルト30分）
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * トークン作成日時
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * トークン使用日時（nullの場合は未使用）
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * エンティティ作成時にcreatedAtを設定
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * トークンが有効期限切れかどうかを判定
     * @return 有効期限切れの場合true
     */
    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * トークンが使用済みかどうかを判定
     * @return 使用済みの場合true
     */
    @Transient
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * トークンが有効かどうかを判定
     * @return 有効（未使用かつ有効期限内）の場合true
     */
    @Transient
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * コンストラクタ
     * @param tokenHash トークンのハッシュ値
     * @param username ユーザー名
     * @param newEmail 新しいメールアドレス
     * @param expiryDate 有効期限
     */
    public EmailChangeToken(String tokenHash, String username, String newEmail, LocalDateTime expiryDate) {
        this.tokenHash = tokenHash;
        this.username = username;
        this.newEmail = newEmail;
        this.expiryDate = expiryDate;
        this.createdAt = LocalDateTime.now();
    }
}
