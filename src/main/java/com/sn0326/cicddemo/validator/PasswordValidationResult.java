package com.sn0326.cicddemo.validator;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * パスワード検証結果を保持するクラス
 */
@Getter
public class PasswordValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final PasswordStrength strength;

    private PasswordValidationResult(boolean valid, List<String> errors, PasswordStrength strength) {
        this.valid = valid;
        this.errors = errors;
        this.strength = strength;
    }

    /**
     * 検証成功の結果を作成
     */
    public static PasswordValidationResult success(PasswordStrength strength) {
        return new PasswordValidationResult(true, new ArrayList<>(), strength);
    }

    /**
     * 検証失敗の結果を作成
     */
    public static PasswordValidationResult failure(List<String> errors) {
        return new PasswordValidationResult(false, errors, PasswordStrength.WEAK);
    }

    /**
     * エラーメッセージを1つの文字列に結合
     */
    public String getErrorMessage() {
        return String.join(", ", errors);
    }
}
