package com.sn0326.cicddemo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Remember Meトークンの自動クリーンアップ機能を持つカスタムTokenRepository
 *
 * トークン取得/作成時に期限切れトークン（30日以上前）を自動削除します。
 * オンデマンド起動型アプリケーション向けの実装です。
 */
public class CleanupJdbcTokenRepository extends JdbcTokenRepositoryImpl {

    private static final Logger log = LoggerFactory.getLogger(CleanupJdbcTokenRepository.class);
    private static final int CLEANUP_DAYS = 30; // 30日以上前のトークンを削除

    /**
     * トークン取得時に期限切れトークンをクリーンアップ
     */
    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        cleanupExpiredTokens();
        return super.getTokenForSeries(seriesId);
    }

    /**
     * 新規トークン作成時に期限切れトークンをクリーンアップ
     */
    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        cleanupExpiredTokens();
        super.createNewToken(token);
    }

    /**
     * 期限切れトークンを削除
     *
     * last_usedが30日以上前のトークンを削除します。
     * Remember Me有効期限（14日）より余裕を持たせた期間を設定しています。
     */
    private void cleanupExpiredTokens() {
        try {
            String sql = "DELETE FROM persistent_logins WHERE last_used < ?";
            Timestamp cutoffTime = Timestamp.from(Instant.now().minus(CLEANUP_DAYS, ChronoUnit.DAYS));

            int deletedCount = getJdbcTemplate().update(sql, cutoffTime);

            if (deletedCount > 0) {
                log.info("Cleaned up {} expired remember-me tokens (older than {} days)",
                        deletedCount, CLEANUP_DAYS);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired remember-me tokens", e);
        }
    }
}
