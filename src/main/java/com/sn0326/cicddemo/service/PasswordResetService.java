package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.exception.InvalidTokenException;
import com.sn0326.cicddemo.exception.RateLimitExceededException;
import com.sn0326.cicddemo.model.PasswordResetToken;
import com.sn0326.cicddemo.repository.PasswordResetTokenRepository;
import com.sn0326.cicddemo.service.notification.SecurityNotificationService;
import com.sn0326.cicddemo.util.TokenGenerator;
import com.sn0326.cicddemo.validator.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * パスワードリセット機能を提供するサービス
 * OWASP準拠のセキュアな実装
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetAttemptService attemptService;
    private final TokenGenerator tokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final SecurityNotificationService notificationService;
    private final JdbcUserDetailsManager userDetailsManager;
    private final JdbcTemplate jdbcTemplate;

    @Value("${security.password-reset.token-expiry-minutes:30}")
    private int tokenExpiryMinutes;

    @Value("${security.password-reset.max-attempts-per-hour:5}")
    private int maxAttemptsPerHour;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * パスワードリセット申請
     *
     * @param username ユーザー名
     * @throws RateLimitExceededException レート制限超過時
     */
    public void requestPasswordReset(String username) {
        // レート制限チェック
        checkRateLimit(username);

        // 試行記録（独立トランザクション、セキュリティのため、ユーザー存在有無に関わらず記録）
        attemptService.recordAttempt(username);

        // ユーザー存在確認（存在しなくてもエラーを出さない - ユーザー名列挙攻撃対策）
        UserDetails userDetails;
        try {
            userDetails = userDetailsManager.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.warn("Password reset requested for non-existent user: {}", username);
            return; // タイミング攻撃対策：同じ処理時間を保つ
        }

        // メールアドレス取得
        String email = getEmailForUser(username);
        if (email == null || email.isBlank()) {
            log.warn("User {} has no email address configured", username);
            return;
        }

        // 既存の未使用トークンを削除
        tokenRepository.deleteUnusedTokensByUsername(username);

        // 新しいトークンを生成（OWASP準拠）
        String rawToken = tokenGenerator.generateSecureToken();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        // トークン情報を保存
        PasswordResetToken resetToken = new PasswordResetToken(
                tokenHash,
                username,
                LocalDateTime.now().plusMinutes(tokenExpiryMinutes)
        );

        tokenRepository.save(resetToken);

        // メール送信（rawTokenをURLに含める）
        String resetUrl = String.format("%s/reissue/resetpassword?token=%s",
                baseUrl, rawToken);

        notificationService.sendPasswordResetNotification(
                username, email, resetUrl, tokenExpiryMinutes);

        log.info("Password reset token generated for user: {}", username);
    }

    /**
     * トークンの検証
     *
     * @param rawToken ユーザーが受け取った生のトークン
     * @return PasswordResetToken エンティティ
     * @throws InvalidTokenException トークンが無効な場合
     */
    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(String rawToken) {
        String tokenHash = tokenGenerator.hashToken(rawToken);

        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("無効なトークンです"));

        if (token.isExpired()) {
            throw new InvalidTokenException("トークンの有効期限が切れています");
        }

        if (token.isUsed()) {
            throw new InvalidTokenException("このトークンは既に使用されています");
        }

        return token;
    }

    /**
     * パスワードのリセット実行
     *
     * @param rawToken    ユーザーが受け取った生のトークン
     * @param newPassword 新しいパスワード
     */
    public void resetPassword(String rawToken, String newPassword) {
        // トークン検証
        PasswordResetToken token = validateToken(rawToken);

        // ユーザー取得
        String username = token.getUsername();
        UserDetails userDetails;
        try {
            userDetails = userDetailsManager.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new InvalidTokenException("ユーザーが見つかりません");
        }

        // パスワードバリデーション
        passwordValidator.validate(newPassword, username);

        // パスワード更新
        String encodedPassword = passwordEncoder.encode(newPassword);
        jdbcTemplate.update(
                "UPDATE users SET password = ?, password_must_change = false WHERE username = ?",
                encodedPassword, username
        );

        // トークンを使用済みにマーク
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        // パスワード変更完了通知
        String email = getEmailForUser(username);
        if (email != null && !email.isBlank()) {
            notificationService.sendPasswordChangedNotification(username, email);
        }

        log.info("Password successfully reset for user: {}", username);
    }

    /**
     * レート制限チェック
     *
     * @param username ユーザー名
     * @throws RateLimitExceededException レート制限超過時
     */
    private void checkRateLimit(String username) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long attemptCount = attemptService.countAttemptsSince(username, oneHourAgo);

        if (attemptCount >= maxAttemptsPerHour) {
            log.warn("Rate limit exceeded for user: {}", username);
            throw new RateLimitExceededException(
                    "パスワードリセットの試行回数が上限に達しました。1時間後に再試行してください");
        }
    }

    /**
     * ユーザーのメールアドレスを取得
     *
     * @param username ユーザー名
     * @return メールアドレス（見つからない場合はnull）
     */
    private String getEmailForUser(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT email FROM users WHERE username = ?",
                    String.class,
                    username
            );
        } catch (Exception e) {
            log.warn("Failed to retrieve email for user: {}", username, e);
            return null;
        }
    }

    /**
     * 期限切れトークンと古い試行記録のクリーンアップ
     * 毎時0分に実行
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredData() {
        LocalDateTime now = LocalDateTime.now();

        // 期限切れトークン削除
        tokenRepository.deleteExpiredTokens(now);

        // 7日以上前の試行記録を削除
        attemptService.deleteOldAttempts(now.minusDays(7));

        log.debug("Cleaned up expired password reset tokens and old attempts");
    }
}
