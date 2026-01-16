package com.sn0326.cicddemo.mail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * メール送信の設定プロパティ
 */
@Data
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    /**
     * メール送信プロバイダー（mock, sendgrid）
     */
    private String provider = "mock";

    /**
     * デフォルト送信元メールアドレス
     */
    private String from = "noreply@example.com";

    /**
     * SendGrid設定
     */
    private SendGrid sendgrid = new SendGrid();

    /**
     * モック設定
     */
    private Mock mock = new Mock();

    @Data
    public static class SendGrid {
        /**
         * SendGrid APIキー
         */
        private String apiKey;
    }

    @Data
    public static class Mock {
        /**
         * ログレベル（INFO, DEBUG）
         */
        private String logLevel = "INFO";

        /**
         * 遅延をシミュレートするか
         */
        private boolean simulateDelay = false;

        /**
         * 遅延時間（ミリ秒）
         */
        private long delayMs = 1000;
    }
}
