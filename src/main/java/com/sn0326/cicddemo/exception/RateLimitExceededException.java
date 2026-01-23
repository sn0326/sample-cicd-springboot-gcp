package com.sn0326.cicddemo.exception;

/**
 * レート制限を超過した場合にスローされる例外
 *
 * パスワードリセット申請の試行回数が制限を超えた場合に発生します。
 * ブルートフォース攻撃対策として使用されます。
 */
public class RateLimitExceededException extends BusinessException {

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     */
    public RateLimitExceededException(String message) {
        super(message);
    }

    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
