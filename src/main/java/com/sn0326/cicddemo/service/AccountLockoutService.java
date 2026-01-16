package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.model.FailedAuthentication;
import com.sn0326.cicddemo.repository.FailedAuthenticationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * アカウントロックアウト機能を提供するサービス
 * TERASOLUNAガイドラインに準拠した実装
 */
@Service
public class AccountLockoutService {

    private static final Logger logger = LoggerFactory.getLogger(AccountLockoutService.class);

    private final FailedAuthenticationRepository failedAuthRepository;

    @Value("${security.account.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.account.lockout.duration-minutes:60}")
    private int lockoutDurationMinutes;

    public AccountLockoutService(FailedAuthenticationRepository failedAuthRepository) {
        this.failedAuthRepository = failedAuthRepository;
    }

    /**
     * アカウントがロックされているか判定
     * TERASOLUNAパターン：N回以上の失敗があり、最古の失敗がロック期間内
     *
     * @param username 対象ユーザー名
     * @return ロック状態の場合true
     */
    @Transactional(readOnly = true)
    public boolean isAccountLocked(String username) {
        LocalDateTime lockoutThreshold = LocalDateTime.now()
                .minusMinutes(lockoutDurationMinutes);

        // 閾値以降の失敗を取得
        List<FailedAuthentication> recentFailures =
                failedAuthRepository.findRecentFailures(username, lockoutThreshold);

        // 失敗回数が閾値以上ならロック状態
        boolean locked = recentFailures.size() >= maxAttempts;

        if (locked) {
            logger.debug("Account is locked: username={}, failureCount={}", username, recentFailures.size());
        }

        return locked;
    }

    /**
     * 失敗を記録
     * 独立したトランザクションで実行し、失敗しても認証プロセスに影響しない
     *
     * @param username 対象ユーザー名
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(String username) {
        FailedAuthentication failure = new FailedAuthentication(
                username,
                LocalDateTime.now()
        );
        failedAuthRepository.save(failure);
        logger.debug("Recorded failed authentication attempt: username={}", username);
    }

    /**
     * 認証成功時に失敗記録をクリア
     * 独立したトランザクションで実行し、失敗しても認証プロセスに影響しない
     *
     * @param username 対象ユーザー名
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(String username) {
        failedAuthRepository.deleteByUsername(username);
        logger.debug("Reset failed authentication attempts: username={}", username);
    }

    /**
     * 古い失敗記録を削除（クリーンアップ用）
     * スケジューラーから定期的に呼び出される
     */
    @Transactional
    public void cleanupOldRecords() {
        LocalDateTime threshold = LocalDateTime.now()
                .minusDays(7); // 1週間以上前のレコードを削除
        failedAuthRepository.deleteOldRecords(threshold);
        logger.info("Cleaned up old failed authentication records before {}", threshold);
    }

    /**
     * 管理者によるロック解除
     * 明示的なトランザクションで実行
     *
     * @param username 対象ユーザー名
     */
    @Transactional
    public void unlockAccount(String username) {
        failedAuthRepository.deleteByUsername(username);
        logger.info("Account unlocked by admin: username={}", username);
    }
}
