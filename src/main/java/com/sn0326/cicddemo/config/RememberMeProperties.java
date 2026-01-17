package com.sn0326.cicddemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Remember Me機能の設定プロパティ
 *
 * application.yamlのsecurity.remember-me配下の設定を管理します。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security.remember-me")
public class RememberMeProperties {

    /**
     * トークンの有効期限（秒）
     * デフォルト: 1209600秒（14日間）
     */
    private int tokenValiditySeconds = 1209600;

    /**
     * クリーンアップ対象となる日数
     * この日数以上前のトークンを自動削除します。
     * デフォルト: 30日
     */
    private int cleanupDays = 30;
}
