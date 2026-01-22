package com.sn0326.cicddemo.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * メールアドレス変更試行記録を保持するエンティティ
 * レート制限（ブルートフォース攻撃対策）用
 */
@Entity
@Table(name = "email_change_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailChangeAttempt {

    /**
     * 複合主キー
     */
    @EmbeddedId
    private EmailChangeAttemptId id;

    /**
     * 複合主キークラス
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class EmailChangeAttemptId implements Serializable {

        /**
         * ユーザー名
         */
        @Column(length = 50)
        private String username;

        /**
         * 試行日時
         */
        @Column(name = "attempt_time")
        private LocalDateTime attemptTime;
    }

    /**
     * コンストラクタ（ユーザー名と試行日時から作成）
     * @param username ユーザー名
     * @param attemptTime 試行日時
     */
    public EmailChangeAttempt(String username, LocalDateTime attemptTime) {
        this.id = new EmailChangeAttemptId(username, attemptTime);
    }
}
