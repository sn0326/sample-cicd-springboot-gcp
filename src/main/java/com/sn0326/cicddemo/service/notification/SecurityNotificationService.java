package com.sn0326.cicddemo.service.notification;

import com.sn0326.cicddemo.mail.MailMessage;
import com.sn0326.cicddemo.mail.MailSender;
import com.sn0326.cicddemo.mail.MailTemplate;
import com.sn0326.cicddemo.mail.template.MailTemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * セキュリティイベントに関する通知を行うサービス
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityNotificationService {

    private final MailSender mailSender;
    private final MailTemplateRenderer templateRenderer;

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

    /**
     * パスワードリセット通知を送信します
     *
     * @param username ユーザー名
     * @param email メールアドレス
     * @param tempPassword 仮パスワード
     * @param resetBy リセット実施者
     */
    public void sendPasswordResetNotification(String username, String email,
                                              String tempPassword, String resetBy) {
        try {
            log.info("パスワードリセット通知を送信: username={}, email={}", username, email);

            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("tempPassword", tempPassword);
            variables.put("resetBy", resetBy);

            String body = templateRenderer.render(MailTemplate.PASSWORD_RESET, variables);

            MailMessage message = MailMessage.builder()
                    .to(email)
                    .subject(MailTemplate.PASSWORD_RESET.getDefaultSubject())
                    .textBody(body)
                    .build();

            message.addMetadata("type", "password_reset");
            message.addMetadata("username", username);

            var result = mailSender.send(message);
            log.info("パスワードリセット通知の送信成功: messageId={}", result.getMessageId());

        } catch (Exception e) {
            log.error("パスワードリセット通知の送信中にエラーが発生しました", e);
        }
    }

    /**
     * パスワード変更完了通知を送信します
     *
     * @param username ユーザー名
     * @param email メールアドレス
     */
    public void sendPasswordChangedNotification(String username, String email) {
        try {
            log.info("パスワード変更通知を送信: username={}, email={}", username, email);

            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("changedAt", LocalDateTime.now().format(DATETIME_FORMATTER));

            String body = templateRenderer.render(MailTemplate.PASSWORD_CHANGED, variables);

            MailMessage message = MailMessage.builder()
                    .to(email)
                    .subject(MailTemplate.PASSWORD_CHANGED.getDefaultSubject())
                    .textBody(body)
                    .build();

            message.addMetadata("type", "password_changed");
            message.addMetadata("username", username);

            var result = mailSender.send(message);
            log.info("パスワード変更通知の送信成功: messageId={}", result.getMessageId());

        } catch (Exception e) {
            log.error("パスワード変更通知の送信中にエラーが発生しました", e);
        }
    }

    /**
     * 不審なログイン検知通知を送信します
     *
     * @param username ユーザー名
     * @param email メールアドレス
     * @param ipAddress IPアドレス
     * @param location 場所（オプション）
     */
    public void sendSuspiciousLoginNotification(String username, String email,
                                                String ipAddress, String location) {
        try {
            log.info("不審なログイン通知を送信: username={}, email={}, ip={}", username, email, ipAddress);

            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("ipAddress", ipAddress);
            variables.put("location", location != null ? location : "不明");
            variables.put("timestamp", LocalDateTime.now().format(DATETIME_FORMATTER));

            String body = templateRenderer.render(MailTemplate.SUSPICIOUS_LOGIN, variables);

            MailMessage message = MailMessage.builder()
                    .to(email)
                    .subject(MailTemplate.SUSPICIOUS_LOGIN.getDefaultSubject())
                    .textBody(body)
                    .build();

            message.addMetadata("type", "suspicious_login");
            message.addMetadata("username", username);
            message.addMetadata("ipAddress", ipAddress);

            var result = mailSender.send(message);
            log.info("不審なログイン通知の送信成功: messageId={}", result.getMessageId());

        } catch (Exception e) {
            log.error("不審なログイン通知の送信中にエラーが発生しました", e);
        }
    }

    /**
     * アカウント無効化通知を送信します
     *
     * @param username ユーザー名
     * @param email メールアドレス
     * @param reason 理由
     * @param disabledBy 無効化実施者
     */
    public void sendAccountDisabledNotification(String username, String email,
                                                String reason, String disabledBy) {
        try {
            log.info("アカウント無効化通知を送信: username={}, email={}", username, email);

            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("reason", reason != null ? reason : "管理者の判断により");
            variables.put("disabledBy", disabledBy);

            String body = templateRenderer.render(MailTemplate.ACCOUNT_DISABLED, variables);

            MailMessage message = MailMessage.builder()
                    .to(email)
                    .subject(MailTemplate.ACCOUNT_DISABLED.getDefaultSubject())
                    .textBody(body)
                    .build();

            message.addMetadata("type", "account_disabled");
            message.addMetadata("username", username);

            var result = mailSender.send(message);
            log.info("アカウント無効化通知の送信成功: messageId={}", result.getMessageId());

        } catch (Exception e) {
            log.error("アカウント無効化通知の送信中にエラーが発生しました", e);
        }
    }

    /**
     * アカウント有効化通知を送信します
     *
     * @param username ユーザー名
     * @param email メールアドレス
     * @param enabledBy 有効化実施者
     */
    public void sendAccountEnabledNotification(String username, String email, String enabledBy) {
        try {
            log.info("アカウント有効化通知を送信: username={}, email={}", username, email);

            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("enabledBy", enabledBy);

            String body = templateRenderer.render(MailTemplate.ACCOUNT_ENABLED, variables);

            MailMessage message = MailMessage.builder()
                    .to(email)
                    .subject(MailTemplate.ACCOUNT_ENABLED.getDefaultSubject())
                    .textBody(body)
                    .build();

            message.addMetadata("type", "account_enabled");
            message.addMetadata("username", username);

            var result = mailSender.send(message);
            log.info("アカウント有効化通知の送信成功: messageId={}", result.getMessageId());

        } catch (Exception e) {
            log.error("アカウント有効化通知の送信中にエラーが発生しました", e);
        }
    }

    /**
     * パスワード再発行URL通知を送信します（ユーザー自身によるリセット）
     *
     * @param username ユーザー名
     * @param email メールアドレス
     * @param resetUrl パスワードリセットURL
     * @param expiryMinutes トークンの有効期限（分）
     */
    public void sendPasswordResetNotification(String username, String email,
                                              String resetUrl, int expiryMinutes) {
        try {
            log.info("パスワード再発行URL通知を送信: username={}, email={}", username, email);

            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("resetUrl", resetUrl);
            variables.put("expiryMinutes", String.valueOf(expiryMinutes));

            String body = templateRenderer.render(MailTemplate.PASSWORD_REISSUE, variables);

            MailMessage message = MailMessage.builder()
                    .to(email)
                    .subject(MailTemplate.PASSWORD_REISSUE.getDefaultSubject())
                    .textBody(body)
                    .build();

            message.addMetadata("type", "password_reissue");
            message.addMetadata("username", username);

            var result = mailSender.send(message);
            log.info("パスワード再発行URL通知の送信成功: messageId={}", result.getMessageId());

        } catch (Exception e) {
            log.error("パスワード再発行URL通知の送信中にエラーが発生しました", e);
        }
    }
}
