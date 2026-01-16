package com.sn0326.cicddemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 認証失敗記録を保持するエンティティ
 * アカウントロックアウト機能で使用
 */
@Entity
@Table(name = "failed_authentications")
@IdClass(FailedAuthentication.FailedAuthenticationId.class)
@Getter
@Setter
@NoArgsConstructor
public class FailedAuthentication {

    @Id
    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Id
    @Column(name = "authentication_timestamp", nullable = false)
    private LocalDateTime authenticationTimestamp;

    public FailedAuthentication(String username, LocalDateTime authenticationTimestamp) {
        this.username = username;
        this.authenticationTimestamp = authenticationTimestamp;
    }

    /**
     * 複合主キークラス
     */
    public static class FailedAuthenticationId implements Serializable {
        private String username;
        private LocalDateTime authenticationTimestamp;

        public FailedAuthenticationId() {}

        public FailedAuthenticationId(String username, LocalDateTime authenticationTimestamp) {
            this.username = username;
            this.authenticationTimestamp = authenticationTimestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FailedAuthenticationId that = (FailedAuthenticationId) o;
            return Objects.equals(username, that.username) &&
                   Objects.equals(authenticationTimestamp, that.authenticationTimestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, authenticationTimestamp);
        }
    }
}
