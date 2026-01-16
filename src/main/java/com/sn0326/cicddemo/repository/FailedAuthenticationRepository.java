package com.sn0326.cicddemo.repository;

import com.sn0326.cicddemo.model.FailedAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 認証失敗記録のリポジトリ
 */
@Repository
public interface FailedAuthenticationRepository
        extends JpaRepository<FailedAuthentication, FailedAuthentication.FailedAuthenticationId> {

    /**
     * 指定時刻以降の失敗レコードを取得（新しい順）
     * ロック判定に使用
     *
     * @param username 対象ユーザー名
     * @param since この時刻以降の失敗を取得
     * @return 失敗記録のリスト
     */
    @Query("SELECT f FROM FailedAuthentication f " +
           "WHERE f.username = :username " +
           "AND f.authenticationTimestamp >= :since " +
           "ORDER BY f.authenticationTimestamp DESC")
    List<FailedAuthentication> findRecentFailures(
            @Param("username") String username,
            @Param("since") LocalDateTime since
    );

    /**
     * 指定ユーザーの全失敗レコードを削除（認証成功時に使用）
     *
     * @param username 対象ユーザー名
     */
    @Modifying
    @Query("DELETE FROM FailedAuthentication f WHERE f.username = :username")
    void deleteByUsername(@Param("username") String username);

    /**
     * 古い失敗レコードを削除（クリーンアップ用）
     *
     * @param before この時刻より前のレコードを削除
     */
    @Modifying
    @Query("DELETE FROM FailedAuthentication f WHERE f.authenticationTimestamp < :before")
    void deleteOldRecords(@Param("before") LocalDateTime before);
}
