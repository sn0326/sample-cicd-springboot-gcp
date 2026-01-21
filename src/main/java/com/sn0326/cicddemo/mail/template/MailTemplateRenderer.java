package com.sn0326.cicddemo.mail.template;

import com.sn0326.cicddemo.mail.MailTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * メールテンプレートを描画するコンポーネント
 */
@Component
public class MailTemplateRenderer {

    /**
     * テンプレートを使用してメール本文を生成します
     *
     * @param template テンプレート種類
     * @param variables テンプレート変数
     * @return 生成されたメール本文
     */
    public String render(MailTemplate template, Map<String, String> variables) {
        return switch (template) {
            case PASSWORD_RESET -> renderPasswordReset(variables);
            case PASSWORD_REISSUE -> renderPasswordReissue(variables);
            case PASSWORD_CHANGED -> renderPasswordChanged(variables);
            case SUSPICIOUS_LOGIN -> renderSuspiciousLogin(variables);
            case ACCOUNT_DISABLED -> renderAccountDisabled(variables);
            case ACCOUNT_ENABLED -> renderAccountEnabled(variables);
        };
    }

    private String renderPasswordReset(Map<String, String> variables) {
        String username = variables.getOrDefault("username", "ユーザー");
        String tempPassword = variables.getOrDefault("tempPassword", "********");
        String resetBy = variables.getOrDefault("resetBy", "管理者");

        return String.format("""
                %s 様

                パスワードがリセットされました。

                リセット実施者: %s
                新しい仮パスワード: %s

                セキュリティのため、ログイン後すぐにパスワードを変更してください。

                ご不明な点がございましたら、システム管理者にお問い合わせください。

                ――――――――――――――――――――――――
                このメールは自動送信されています。
                返信はできませんのでご了承ください。
                """, username, resetBy, tempPassword);
    }

    private String renderPasswordReissue(Map<String, String> variables) {
        String username = variables.getOrDefault("username", "ユーザー");
        String resetUrl = variables.getOrDefault("resetUrl", "");
        String expiryMinutes = variables.getOrDefault("expiryMinutes", "30");

        return String.format("""
                %s 様

                パスワード再発行のリクエストを受け付けました。

                以下のURLにアクセスして、パスワードの再設定を行ってください。

                【パスワード再設定URL】
                %s

                ※このURLは発行から%s分間有効です。
                ※このURLは1回のみ使用可能です。

                ――――――――――――――――――――――――
                このメールに心当たりがない場合は、第三者がパスワード
                リセットを試みた可能性があります。このメールを破棄し、
                アカウントのセキュリティ状態を確認することをお勧めします。

                ――――――――――――――――――――――――
                このメールは自動送信されています。
                返信はできませんのでご了承ください。
                """, username, resetUrl, expiryMinutes);
    }

    private String renderPasswordChanged(Map<String, String> variables) {
        String username = variables.getOrDefault("username", "ユーザー");
        String changedAt = variables.getOrDefault("changedAt", "現在");

        return String.format("""
                %s 様

                パスワードが正常に変更されました。

                変更日時: %s

                もしこの変更にお心当たりがない場合は、
                すぐにシステム管理者にご連絡ください。

                ――――――――――――――――――――――――
                このメールは自動送信されています。
                返信はできませんのでご了承ください。
                """, username, changedAt);
    }

    private String renderSuspiciousLogin(Map<String, String> variables) {
        String username = variables.getOrDefault("username", "ユーザー");
        String ipAddress = variables.getOrDefault("ipAddress", "不明");
        String location = variables.getOrDefault("location", "不明");
        String timestamp = variables.getOrDefault("timestamp", "現在");

        return String.format("""
                %s 様

                新しい場所からのログインを検知しました。

                ログイン日時: %s
                IPアドレス: %s
                場所: %s

                このログインにお心当たりがない場合は、
                直ちにパスワードを変更し、システム管理者にご連絡ください。

                ――――――――――――――――――――――――
                このメールは自動送信されています。
                返信はできませんのでご了承ください。
                """, username, timestamp, ipAddress, location);
    }

    private String renderAccountDisabled(Map<String, String> variables) {
        String username = variables.getOrDefault("username", "ユーザー");
        String reason = variables.getOrDefault("reason", "管理者の判断により");
        String disabledBy = variables.getOrDefault("disabledBy", "管理者");

        return String.format("""
                %s 様

                アカウントが無効化されました。

                無効化実施者: %s
                理由: %s

                詳細についてはシステム管理者にお問い合わせください。

                ――――――――――――――――――――――――
                このメールは自動送信されています。
                返信はできませんのでご了承ください。
                """, username, disabledBy, reason);
    }

    private String renderAccountEnabled(Map<String, String> variables) {
        String username = variables.getOrDefault("username", "ユーザー");
        String enabledBy = variables.getOrDefault("enabledBy", "管理者");

        return String.format("""
                %s 様

                アカウントが有効化されました。

                有効化実施者: %s

                ログインして通常通りサービスをご利用いただけます。

                ――――――――――――――――――――――――
                このメールは自動送信されています。
                返信はできませんのでご了承ください。
                """, username, enabledBy);
    }
}
