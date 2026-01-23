package com.sn0326.cicddemo.exception;

/**
 * リソース未検出例外
 *
 * 要求されたリソース（ユーザー、トークン、データ等）が見つからない場合にスローされる。
 * 404エラーの原因となる例外。
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * リソースタイプとIDを指定してResourceNotFoundExceptionを構築する
     *
     * @param resourceType リソースの種類（例: "User", "Token"）
     * @param resourceId リソースのID
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%sが見つかりません: %s", resourceType, resourceId));
    }

    /**
     * メッセージのみを指定してResourceNotFoundExceptionを構築する
     *
     * @param message エラーメッセージ
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
