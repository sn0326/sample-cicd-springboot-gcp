package com.sn0326.cicddemo.config;

import com.sn0326.cicddemo.security.AccountLockoutUserDetailsChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;

/**
 * ユーザー認証に関するBean定義を管理する設定クラス
 * SecurityConfigから分離することで循環参照を回避
 */
@Configuration
public class UserDetailsConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            AccountLockoutUserDetailsChecker accountLockoutChecker) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        // ログイン前のロック状態チェックを追加
        provider.setPreAuthenticationChecks(accountLockoutChecker);

        // エラーメッセージを隠蔽（ユーザー名列挙攻撃対策）
        provider.setHideUserNotFoundExceptions(true);

        return provider;
    }
}
