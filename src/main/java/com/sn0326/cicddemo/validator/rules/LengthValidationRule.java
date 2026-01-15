package com.sn0326.cicddemo.validator.rules;

import com.sn0326.cicddemo.validator.PasswordValidationRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * パスワードの長さを検証するルール
 * NIST SP 800-63B: 最小8文字、最大64文字以上をサポート
 */
@Component
public class LengthValidationRule implements PasswordValidationRule {

    @Value("${password.min-length:8}")
    private int minLength;

    @Value("${password.max-length:64}")
    private int maxLength;

    @Override
    public Optional<String> validate(String password, String username) {
        if (password == null || password.isEmpty()) {
            return Optional.of("パスワードは必須です");
        }

        int length = password.length();

        if (length < minLength) {
            return Optional.of(String.format("パスワードは%d文字以上である必要があります", minLength));
        }

        if (length > maxLength) {
            return Optional.of(String.format("パスワードは%d文字以内である必要があります", maxLength));
        }

        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 10; // 軽い処理なので最初に実行
    }
}
