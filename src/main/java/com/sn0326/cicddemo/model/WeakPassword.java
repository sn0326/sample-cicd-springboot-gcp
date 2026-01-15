package com.sn0326.cicddemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 弱いパスワードのエンティティ
 * NIST SP 800-63Bでは一般的な弱いパスワードをブラックリスト化することを推奨
 */
@Entity
@Table(name = "weak_passwords")
@Getter
@Setter
@NoArgsConstructor
public class WeakPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 弱いパスワード（小文字で保存）
     */
    @Column(nullable = false, unique = true)
    private String password;

    /**
     * 説明（例: "数字のみ", "キーボードパターン"）
     */
    @Column(length = 500)
    private String description;

    /**
     * 作成日時
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
