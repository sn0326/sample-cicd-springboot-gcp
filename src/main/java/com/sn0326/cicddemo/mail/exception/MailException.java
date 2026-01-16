package com.sn0326.cicddemo.mail.exception;

/**
 * メール送信に関する例外の基底クラス
 */
public class MailException extends RuntimeException {

    public MailException(String message) {
        super(message);
    }

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailException(Throwable cause) {
        super(cause);
    }
}
