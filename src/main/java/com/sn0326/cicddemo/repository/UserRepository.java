package com.sn0326.cicddemo.repository;

/**
 * ユーザー情報へのデータアクセスを提供するリポジトリインターフェース
 *
 * Spring SecurityのJdbcUserDetailsManagerが提供しない、
 * カスタムなユーザー情報へのアクセスを提供します。
 */
public interface UserRepository {

    /**
     * ユーザー名からメールアドレスを取得
     *
     * @param username ユーザー名
     * @return メールアドレス（見つからない場合はnull）
     */
    String findEmailByUsername(String username);

    /**
     * パスワードを更新し、パスワード変更必須フラグをクリア
     *
     * @param username ユーザー名
     * @param encodedPassword エンコード済みパスワード
     * @return 更新された行数
     */
    int updatePasswordAndClearMustChangeFlag(String username, String encodedPassword);
}
