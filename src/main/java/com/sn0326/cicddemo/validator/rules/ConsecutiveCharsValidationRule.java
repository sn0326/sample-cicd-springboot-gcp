package com.sn0326.cicddemo.validator.rules;

import com.sn0326.cicddemo.validator.PasswordValidationRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 連続した同じ文字が多く含まれていないかを検証するルール
 * 例: "aaaaaaa", "1111111" などを拒否
 */
@Component
public class ConsecutiveCharsValidationRule implements PasswordValidationRule {

    @Value("${password.max-consecutive-chars:3}")
    private int maxConsecutiveChars;

    @Override
    public Optional<String> validate(String password, String username) {
        if (password == null || password.isEmpty()) {
            return Optional.empty();
        }

        int consecutiveCount = 1;
        char previousChar = password.charAt(0);

        for (int i = 1; i < password.length(); i++) {
            char currentChar = password.charAt(i);
            if (currentChar == previousChar) {
                consecutiveCount++;
                if (consecutiveCount > maxConsecutiveChars) {
                    return Optional.of(String.format(
                        "同じ文字を%d文字以上連続して使用することはできません",
                        maxConsecutiveChars
                    ));
                }
            } else {
                consecutiveCount = 1;
                previousChar = currentChar;
            }
        }

        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 30; // 軽い処理
    }
}
