package com.sn0326.cicddemo.mail;

import com.sn0326.cicddemo.mail.exception.MailException;

import java.util.concurrent.CompletableFuture;

/**
 * メール送信の共通インターフェース
 * 実装クラスは送信プロバイダー（Mock、SendGridなど）に依存します
 */
public interface MailSender {

    /**
     * メールを同期的に送信します
     *
     * @param message 送信するメールメッセージ
     * @return 送信結果
     * @throws MailException メール送信に失敗した場合
     */
    MailSendResult send(MailMessage message) throws MailException;

    /**
     * メールを非同期的に送信します
     *
     * @param message 送信するメールメッセージ
     * @return 送信結果を含むCompletableFuture
     */
    CompletableFuture<MailSendResult> sendAsync(MailMessage message);

    /**
     * メール送信サービスが利用可能かチェックします
     *
     * @return サービスが利用可能な場合はtrue
     */
    boolean isAvailable();
}
