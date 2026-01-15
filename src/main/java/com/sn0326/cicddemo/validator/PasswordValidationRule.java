package com.sn0326.cicddemo.validator;

import java.util.Optional;

/**
 * パスワード検証ルールのインターフェイス
 *
 * 新しい検証ルールを追加する場合は、このインターフェイスを実装して
 * @Componentアノテーションを付けるだけで自動的に検証プロセスに組み込まれます
 *
 * 例: Have I Been Pwned APIによる漏洩チェック
 * @Component
 * public class PwnedPasswordValidationRule implements PasswordValidationRule {
 *     public Optional<String> validate(String password, String username) {
 *         // 外部API呼び出し
 *     }
 *     public int getOrder() { return 100; } // 重い処理なので後で実行
 * }
 */
public interface PasswordValidationRule {
    /**
     * パスワードを検証する
     *
     * @param password 検証対象のパスワード
     * @param username ユーザー名（ユーザー名を含むパスワードを拒否するため）
     * @return エラーメッセージ。検証成功の場合はOptional.empty()
     */
    Optional<String> validate(String password, String username);

    /**
     * 実行順序を返す
     *
     * 小さい値ほど先に実行される
     * 軽い処理（長さチェック等）は小さい値、
     * 重い処理（API呼び出し等）は大きい値を返すべき
     *
     * @return 実行順序（0-100を推奨）
     */
    int getOrder();
}
