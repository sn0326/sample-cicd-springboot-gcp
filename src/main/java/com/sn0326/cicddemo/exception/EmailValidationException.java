package com.sn0326.cicddemo.exception;

/**
 * メール検証例外
 *
 * メールアドレスの検証エラー（形式不正、重複等）が発生した場合にスローされる。
 */
public class EmailValidationException extends BusinessException {

    /**
     * メッセージを指定してEmailValidationExceptionを構築する
     *
     * @param message エラーメッセージ
     */
    public EmailValidationException(String message) {
        super(message);
    }

    /**
     * メッセージと原因例外を指定してEmailValidationExceptionを構築する
     *
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public EmailValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
