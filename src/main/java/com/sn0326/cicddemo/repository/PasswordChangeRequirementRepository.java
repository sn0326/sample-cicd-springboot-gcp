package com.sn0326.cicddemo.repository;

/**
 * パスワード変更要求の永続化を担当するリポジトリ
 */
public interface PasswordChangeRequirementRepository {

    /**
     * ユーザーがパスワード変更を要求されているかチェック
     *
     * @param username ユーザー名
     * @return パスワード変更が必要な場合true
     */
    boolean isPasswordChangeRequired(String username);

    /**
     * ユーザーにパスワード変更を要求する
     *
     * @param username ユーザー名
     */
    void requirePasswordChange(String username);

    /**
     * パスワード変更要求をクリアする
     *
     * @param username ユーザー名
     */
    void clearPasswordChangeRequirement(String username);
}
