package com.sn0326.cicddemo.mail;

import com.sn0326.cicddemo.mail.exception.MailException;

/**
 * メール送信の共通インターフェース
 * 実装クラスは送信プロバイダー（Mock、SendGridなど）に依存します
 */
public interface MailSender {

    /**
     * メールを送信します
     *
     * @param message 送信するメールメッセージ
     * @return 送信結果
     * @throws MailException メール送信に失敗した場合
     */
    MailSendResult send(MailMessage message) throws MailException;
}
