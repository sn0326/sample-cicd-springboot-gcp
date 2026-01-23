package com.sn0326.cicddemo.exception;

/**
 * システム例外クラス
 *
 * システムレベルのエラーを表す例外。
 * 例外コード（e.xx.xxxx形式）を持ち、ログレベルの判定に使用する。
 * ユーザーには詳細を表示せず、サポート用のエラーコードのみを表示する。
 * TERASOLUNAガイドラインに準拠。
 */
public class SystemException extends RuntimeException {

    /**
     * 例外コード（例: "e.db.0001", "w.ml.0001"）
     */
    private final String code;

    /**
     * メッセージと例外コードを指定してSystemExceptionを構築する
     *
     * @param code 例外コード（プレフィックス: i=info, w=warn, e=error）
     * @param message エラーメッセージ（ログ用）
     */
    public SystemException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * メッセージ、例外コード、原因例外を指定してSystemExceptionを構築する
     *
     * @param code 例外コード
     * @param message エラーメッセージ（ログ用）
     * @param cause 原因例外
     */
    public SystemException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 例外コードを取得する
     *
     * @return 例外コード
     */
    public String getCode() {
        return code;
    }

    /**
     * ログレベルを判定する
     *
     * @return ログレベル（INFO, WARN, ERROR）
     */
    public LogLevel getLogLevel() {
        if (code.startsWith("i.")) {
            return LogLevel.INFO;
        } else if (code.startsWith("w.")) {
            return LogLevel.WARN;
        }
        return LogLevel.ERROR;
    }

    /**
     * ログレベル列挙型
     */
    public enum LogLevel {
        INFO, WARN, ERROR
    }
}
