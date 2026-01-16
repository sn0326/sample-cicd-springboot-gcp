package com.sn0326.cicddemo.mail.sender;

import com.sn0326.cicddemo.mail.MailMessage;
import com.sn0326.cicddemo.mail.MailSendResult;
import com.sn0326.cicddemo.mail.MailSender;
import com.sn0326.cicddemo.mail.config.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * メール送信のモック実装
 * 実際には送信せず、ログに出力します
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.provider", havingValue = "mock", matchIfMissing = true)
public class MockMailSender implements MailSender {

    private final MailProperties mailProperties;

    @Override
    public MailSendResult send(MailMessage message) {
        log.info("==================== モックメール送信 ====================");
        log.info("宛先: {}", message.getTo());
        log.info("送信元: {}", message.getFrom() != null ? message.getFrom() : mailProperties.getFrom());
        log.info("件名: {}", message.getSubject());
        log.info("------------------------------------------------------------");
        log.info("本文:\n{}", message.getTextBody());
        log.info("============================================================");

        if (message.getMetadata() != null && !message.getMetadata().isEmpty()) {
            log.debug("メタデータ: {}", message.getMetadata());
        }

        // 遅延シミュレーション
        if (mailProperties.getMock().isSimulateDelay()) {
            try {
                Thread.sleep(mailProperties.getMock().getDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("遅延シミュレーション中に中断されました", e);
            }
        }

        String messageId = "mock-" + UUID.randomUUID();
        return MailSendResult.success(messageId, message.getTo());
    }
}
