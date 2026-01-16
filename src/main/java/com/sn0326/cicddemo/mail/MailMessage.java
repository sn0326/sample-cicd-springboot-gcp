package com.sn0326.cicddemo.mail;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * メール送信メッセージを表すDTO
 */
@Data
@Builder
public class MailMessage {

    /**
     * 宛先メールアドレス
     */
    private String to;

    /**
     * 件名
     */
    private String subject;

    /**
     * テキスト本文
     */
    private String textBody;

    /**
     * HTML本文（オプション）
     */
    private String htmlBody;

    /**
     * 送信元メールアドレス（オプション）
     * 設定されていない場合はデフォルト送信元が使用される
     */
    private String from;

    /**
     * メタデータ（追加情報、ログ記録などに使用）
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * メタデータを追加する
     */
    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
}
