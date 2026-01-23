package com.sn0326.cicddemo.exception;

/**
 * 無効なパスワードリセットトークンが使用された場合にスローされる例外
 *
 * 以下の場合に発生します：
 * - トークンが存在しない
 * - トークンの有効期限が切れている
 * - トークンが既に使用済み
 */
public class InvalidTokenException extends BusinessException {

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
