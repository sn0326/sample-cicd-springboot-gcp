package com.sn0326.cicddemo.exception;

/**
 * パスワード検証例外
 *
 * パスワードが不正な場合（一致しない、強度不足等）にスローされる。
 */
public class InvalidPasswordException extends BusinessException {

    /**
     * メッセージを指定してInvalidPasswordExceptionを構築する
     *
     * @param message エラーメッセージ
     */
    public InvalidPasswordException(String message) {
        super(message);
    }

    /**
     * メッセージと原因例外を指定してInvalidPasswordExceptionを構築する
     *
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
