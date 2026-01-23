package com.sn0326.cicddemo.exception;

/**
 * ユーザー検証例外
 *
 * ユーザーの検証エラー（存在しない、無効、権限不足等）が発生した場合にスローされる。
 */
public class UserValidationException extends BusinessException {

    /**
     * メッセージを指定してUserValidationExceptionを構築する
     *
     * @param message エラーメッセージ
     */
    public UserValidationException(String message) {
        super(message);
    }

    /**
     * メッセージと原因例外を指定してUserValidationExceptionを構築する
     *
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public UserValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
