package com.sn0326.cicddemo.repository;

import com.sn0326.cicddemo.model.EmailChangeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * メールアドレス変更試行記録のリポジトリ
 * レート制限（ブルートフォース攻撃対策）用
 */
@Repository
public interface EmailChangeAttemptRepository
        extends JpaRepository<EmailChangeAttempt, EmailChangeAttempt.EmailChangeAttemptId> {

    /**
     * 指定時刻以降のユーザーの試行回数をカウント
     * @param username ユーザー名
     * @param since 基準日時
     * @return 試行回数
     */
    @Query("SELECT COUNT(a) FROM EmailChangeAttempt a " +
           "WHERE a.id.username = :username " +
           "AND a.id.attemptTime > :since")
    long countByUsernameAndAttemptTimeSince(@Param("username") String username,
                                            @Param("since") LocalDateTime since);

    /**
     * 古い試行記録を削除
     * @param dateTime 基準日時（これより古い記録を削除）
     * @return 削除された行数
     */
    @Modifying
    @Query("DELETE FROM EmailChangeAttempt a WHERE a.id.attemptTime < :dateTime")
    int deleteOldAttempts(@Param("dateTime") LocalDateTime dateTime);
}
