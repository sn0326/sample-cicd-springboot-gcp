package com.sn0326.cicddemo.mail;

import lombok.Getter;

/**
 * メールテンプレートの種類
 */
@Getter
public enum MailTemplate {

    /**
     * パスワードリセット通知
     */
    PASSWORD_RESET(
            "パスワードリセットのお知らせ",
            "password_reset"
    ),

    /**
     * パスワード再発行URL通知
     */
    PASSWORD_REISSUE(
            "【重要】パスワード再発行のご案内",
            "password_reissue"
    ),

    /**
     * パスワード変更完了通知
     */
    PASSWORD_CHANGED(
            "パスワード変更完了のお知らせ",
            "password_changed"
    ),

    /**
     * 不審なログイン検知通知
     */
    SUSPICIOUS_LOGIN(
            "【重要】新しい場所からのログインを検知しました",
            "suspicious_login"
    ),

    /**
     * アカウント無効化通知
     */
    ACCOUNT_DISABLED(
            "アカウント無効化のお知らせ",
            "account_disabled"
    ),

    /**
     * アカウント有効化通知
     */
    ACCOUNT_ENABLED(
            "アカウント有効化のお知らせ",
            "account_enabled"
    ),

    /**
     * メールアドレス変更確認
     */
    EMAIL_CHANGE_VERIFICATION(
            "メールアドレス変更のご確認",
            "email_change_verification"
    ),

    /**
     * メールアドレス変更完了通知
     */
    EMAIL_CHANGED(
            "メールアドレスが変更されました",
            "email_changed"
    );

    /**
     * デフォルトの件名
     */
    private final String defaultSubject;

    /**
     * テンプレート識別子
     */
    private final String templateId;

    MailTemplate(String defaultSubject, String templateId) {
        this.defaultSubject = defaultSubject;
        this.templateId = templateId;
    }
}
