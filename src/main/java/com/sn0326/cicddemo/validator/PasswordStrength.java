package com.sn0326.cicddemo.validator;

/**
 * パスワード強度を表す列挙型
 * NIST SP 800-63Bでは長さが最も重要な要素
 */
public enum PasswordStrength {
    /**
     * 弱い: 8-9文字
     */
    WEAK,

    /**
     * まあまあ: 10-11文字
     */
    FAIR,

    /**
     * 良好: 12-15文字
     */
    GOOD,

    /**
     * 強い: 16文字以上
     */
    STRONG;

    /**
     * パスワードの長さから強度を判定
     */
    public static PasswordStrength fromLength(int length) {
        if (length >= 16) {
            return STRONG;
        } else if (length >= 12) {
            return GOOD;
        } else if (length >= 10) {
            return FAIR;
        } else {
            return WEAK;
        }
    }
}
