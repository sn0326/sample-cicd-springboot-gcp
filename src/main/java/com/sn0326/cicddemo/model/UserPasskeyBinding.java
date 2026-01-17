package com.sn0326.cicddemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ユーザーとパスキーエンティティの紐付け情報を保持するエンティティ
 * 既存のusersテーブル（username）とpasskey_user_entities（id）を関連付ける
 */
@Entity
@Table(name = "user_passkey_bindings")
@Getter
@Setter
@NoArgsConstructor
@IdClass(UserPasskeyBinding.UserPasskeyBindingId.class)
public class UserPasskeyBinding {

    @Id
    @Column(nullable = false, length = 50)
    private String username;

    @Id
    @Column(name = "user_entity_id", nullable = false, length = 255)
    private String userEntityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UserPasskeyBinding(String username, String userEntityId) {
        this.username = username;
        this.userEntityId = userEntityId;
    }

    /**
     * 複合主キークラス
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserPasskeyBindingId implements Serializable {
        private String username;
        private String userEntityId;

        public UserPasskeyBindingId(String username, String userEntityId) {
            this.username = username;
            this.userEntityId = userEntityId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserPasskeyBindingId that)) return false;
            return username.equals(that.username) && userEntityId.equals(that.userEntityId);
        }

        @Override
        public int hashCode() {
            return username.hashCode() + userEntityId.hashCode();
        }
    }
}
