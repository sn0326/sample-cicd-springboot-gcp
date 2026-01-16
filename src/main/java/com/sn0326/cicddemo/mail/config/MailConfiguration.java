package com.sn0326.cicddemo.mail.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * メール送信の設定
 */
@Configuration
@RequiredArgsConstructor
public class MailConfiguration {

    private final MailProperties mailProperties;

    /**
     * メール送信用の非同期Executor
     */
    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(mailProperties.getAsync().getThreadPoolSize());
        executor.setMaxPoolSize(mailProperties.getAsync().getThreadPoolSize() * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mail-sender-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
