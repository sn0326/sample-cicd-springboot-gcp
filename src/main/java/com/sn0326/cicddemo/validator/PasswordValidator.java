package com.sn0326.cicddemo.validator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * パスワード検証を統合するクラス
 *
 * 全ての PasswordValidationRule を自動的に収集し、
 * order の順番に実行する
 */
@Component
public class PasswordValidator {

    private final List<PasswordValidationRule> rules;

    /**
     * Springが全てのPasswordValidationRuleを自動注入
     */
    public PasswordValidator(List<PasswordValidationRule> rules) {
        // orderでソート（小さい値ほど先に実行）
        this.rules = rules.stream()
            .sorted(Comparator.comparingInt(PasswordValidationRule::getOrder))
            .toList();
    }

    /**
     * パスワードを検証する
     *
     * @param password 検証対象のパスワード
     * @param username ユーザー名
     * @return 検証結果
     */
    public PasswordValidationResult validate(String password, String username) {
        List<String> errors = new ArrayList<>();

        // 全ルールを順番に実行
        for (PasswordValidationRule rule : rules) {
            Optional<String> error = rule.validate(password, username);
            error.ifPresent(errors::add);
        }

        // エラーがある場合は失敗
        if (!errors.isEmpty()) {
            return PasswordValidationResult.failure(errors);
        }

        // パスワード強度を評価
        PasswordStrength strength = evaluateStrength(password);

        return PasswordValidationResult.success(strength);
    }

    /**
     * パスワードの強度を評価する
     *
     * NIST SP 800-63Bでは長さが最も重要な要素
     */
    private PasswordStrength evaluateStrength(String password) {
        return PasswordStrength.fromLength(password.length());
    }
}
