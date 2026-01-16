package com.sn0326.cicddemo.mail;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * メール送信結果を表すDTO
 */
@Data
@Builder
public class MailSendResult {

    /**
     * 送信が成功したかどうか
     */
    private boolean success;

    /**
     * メッセージID（送信プロバイダーから返される一意のID）
     */
    private String messageId;

    /**
     * 送信日時
     */
    private LocalDateTime timestamp;

    /**
     * エラーメッセージ（送信失敗時）
     */
    private String errorMessage;

    /**
     * 送信先メールアドレス
     */
    private String recipient;

    /**
     * 成功した送信結果を作成
     */
    public static MailSendResult success(String messageId, String recipient) {
        return MailSendResult.builder()
                .success(true)
                .messageId(messageId)
                .recipient(recipient)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失敗した送信結果を作成
     */
    public static MailSendResult failure(String errorMessage, String recipient) {
        return MailSendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .recipient(recipient)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
