package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.model.PasswordResetAttempt;
import com.sn0326.cicddemo.repository.PasswordResetAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * パスワードリセット試行記録サービス
 *
 * 独立したトランザクションで試行記録を保存するため、
 * メインのトランザクションとは分離されています。
 * これにより、メイン処理が失敗しても試行記録は残ります。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetAttemptService {

    private final PasswordResetAttemptRepository attemptRepository;

    /**
     * パスワードリセット試行を記録（独立トランザクション）
     *
     * REQUIRES_NEWを使用することで、呼び出し元のトランザクションとは
     * 完全に独立したトランザクションで実行されます。
     * これにより、DB障害時も認証フローに影響を与えません。
     *
     * @param username ユーザー名
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAttempt(String username) {
        try {
            PasswordResetAttempt attempt = new PasswordResetAttempt(
                    username, LocalDateTime.now());
            attemptRepository.save(attempt);
            log.debug("Password reset attempt recorded for user: {}", username);
        } catch (Exception e) {
            // 試行記録の失敗はメイン処理に影響を与えない
            log.error("Failed to record password reset attempt for user: {}", username, e);
        }
    }

    /**
     * 指定時刻以降のユーザーの試行回数をカウント
     *
     * @param username ユーザー名
     * @param since 基準日時
     * @return 試行回数
     */
    @Transactional(readOnly = true)
    public long countAttemptsSince(String username, LocalDateTime since) {
        return attemptRepository.countByUsernameAndAttemptTimeSince(username, since);
    }

    /**
     * 古い試行記録を削除
     *
     * @param before この日時より前の記録を削除
     */
    @Transactional
    public void deleteOldAttempts(LocalDateTime before) {
        attemptRepository.deleteOldAttempts(before);
        log.debug("Deleted password reset attempts before: {}", before);
    }
}
