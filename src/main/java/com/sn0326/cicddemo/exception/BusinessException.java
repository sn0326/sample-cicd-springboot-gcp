package com.sn0326.cicddemo.exception;

/**
 * ビジネス例外の基底クラス
 *
 * ユーザーに表示可能なメッセージを持つ例外。
 * ビジネスロジックで発生する予期された例外に使用する。
 * TERASOLUNAガイドラインに準拠。
 */
public class BusinessException extends RuntimeException {

    /**
     * メッセージのみを指定してBusinessExceptionを構築する
     *
     * @param message ユーザーに表示するエラーメッセージ
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * メッセージと原因例外を指定してBusinessExceptionを構築する
     *
     * @param message ユーザーに表示するエラーメッセージ
     * @param cause 原因例外
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
