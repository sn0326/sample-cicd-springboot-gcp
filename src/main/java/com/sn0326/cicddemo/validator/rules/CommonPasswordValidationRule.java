package com.sn0326.cicddemo.validator.rules;

import com.sn0326.cicddemo.service.WeakPasswordCacheService;
import com.sn0326.cicddemo.validator.PasswordValidationRule;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 一般的な弱いパスワードを検証するルール
 * データベースに保存された弱いパスワードリストをチェック
 *
 * WeakPasswordCacheServiceがメモリキャッシュを管理し、
 * 高速な検証を実現します。
 *
 * 注: より強力な検証には Have I Been Pwned API を使用することを推奨
 */
@Component
public class CommonPasswordValidationRule implements PasswordValidationRule {

    private final WeakPasswordCacheService weakPasswordCacheService;

    public CommonPasswordValidationRule(WeakPasswordCacheService weakPasswordCacheService) {
        this.weakPasswordCacheService = weakPasswordCacheService;
    }

    @Override
    public Optional<String> validate(String password, String username) {
        if (password == null || password.isEmpty()) {
            return Optional.empty();
        }

        if (weakPasswordCacheService.isWeakPassword(password)) {
            return Optional.of("このパスワードは一般的すぎるため使用できません");
        }

        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return 40; // キャッシュから取得するので軽い
    }
}
