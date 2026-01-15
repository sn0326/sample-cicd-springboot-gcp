package com.sn0326.cicddemo.validator.rules;

import com.sn0326.cicddemo.validator.PasswordValidationRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * パスワードにユーザー名が含まれていないかを検証するルール
 * NIST SP 800-63B: ユーザー名やサービス名を含むパスワードは拒否すべき
 */
@Component
public class UsernameValidationRule implements PasswordValidationRule {

    @Override
    public Optional<String> validate(String password, String username) {
        if (username == null || username.isEmpty()) {
            return Optional.empty();
        }

        // 大文字小文字を区別せずにチェック
        String lowerPassword = password.toLowerCase();
        String lowerUsername = username.toLowerCase();

        if (lowerPassword.contains(lowerUsername)) {
            return Optional.of("パスワードにユーザー名を含めることはできません");
        }

        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 20; // 軽い処理
    }
}
