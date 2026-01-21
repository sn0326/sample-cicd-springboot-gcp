package com.sn0326.cicddemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * トランザクション設定
 *
 * TERASOLUNAガイドラインに従い、サービス間の直接呼び出しを避けるため、
 * TransactionTemplateを使用してプログラマティックなトランザクション制御を行います。
 */
@Configuration
public class TransactionConfig {

    /**
     * 新規トランザクションを開始するTransactionTemplate
     *
     * REQUIRES_NEW伝播属性を持つため、既存のトランザクションがある場合は
     * 一時停止して、新しいトランザクションを開始します。
     *
     * 用途：
     * - 監査ログの記録（メイン処理の成否に関わらず記録）
     * - レート制限の試行記録（メイン処理の成否に関わらず記録）
     *
     * @param transactionManager トランザクションマネージャー
     * @return REQUIRES_NEW伝播属性を持つTransactionTemplate
     */
    @Bean
    public TransactionTemplate requiresNewTransactionTemplate(
            PlatformTransactionManager transactionManager) {

        TransactionTemplate template = new TransactionTemplate(transactionManager);

        // REQUIRES_NEW: 常に新しいトランザクションを開始
        template.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);

        return template;
    }
}
