package com.sn0326.cicddemo.validator.rules;

import com.sn0326.cicddemo.validator.PasswordValidationRule;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * 一般的な弱いパスワードを検証するルール
 * 静的なブラックリストで最も一般的な弱いパスワードをチェック
 *
 * 注: より強力な検証には Have I Been Pwned API を使用することを推奨
 */
@Component
public class CommonPasswordValidationRule implements PasswordValidationRule {

    /**
     * 最も一般的な弱いパスワードのリスト
     * 出典: Top 100 most common passwords
     */
    private static final Set<String> COMMON_PASSWORDS = Set.of(
        // 数字のみ
        "12345678", "123456789", "1234567890", "00000000", "11111111",
        "password", "password123", "password1",
        // キーボードパターン
        "qwerty", "qwertyui", "qwertyuiop", "asdfghjk", "asdfghjkl",
        "zxcvbnm", "qweasd", "qweasdzxc",
        // 一般的な単語
        "welcome", "welcome1", "admin", "admin123", "administrator",
        "root", "root123", "test", "test123", "user", "user123",
        "guest", "guest123", "login", "login123",
        // 日本語由来
        "password", "passw0rd", "p@ssw0rd", "p@ssword",
        "sakura", "yamada", "tanaka", "suzuki",
        // 季節・月
        "spring", "summer", "autumn", "winter",
        "january", "february", "march", "april",
        // その他
        "letmein", "monkey", "dragon", "master",
        "sunshine", "princess", "football", "iloveyou",
        "trustno1", "baseball", "superman", "batman"
    );

    @Override
    public Optional<String> validate(String password, String username) {
        if (password == null || password.isEmpty()) {
            return Optional.empty();
        }

        // 大文字小文字を区別せずにチェック
        String lowerPassword = password.toLowerCase();

        if (COMMON_PASSWORDS.contains(lowerPassword)) {
            return Optional.of("このパスワードは一般的すぎるため使用できません");
        }

        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 40; // 静的チェックなので軽い
    }
}
