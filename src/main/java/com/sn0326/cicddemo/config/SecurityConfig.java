package com.sn0326.cicddemo.config;

import com.sn0326.cicddemo.security.AccountLockoutUserDetailsChecker;
import com.sn0326.cicddemo.security.CustomOidcUserService;
import com.sn0326.cicddemo.security.FormAuthenticationFailureHandler;
import com.sn0326.cicddemo.security.FormAuthenticationSuccessHandler;
import com.sn0326.cicddemo.security.OidcAuthenticationFailureHandler;
import com.sn0326.cicddemo.security.OidcAuthenticationSuccessHandler;
import com.sn0326.cicddemo.security.PasswordChangeRequiredFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final FormAuthenticationSuccessHandler formAuthenticationSuccessHandler;
    private final FormAuthenticationFailureHandler formAuthenticationFailureHandler;
    private final OidcAuthenticationSuccessHandler oidcAuthenticationSuccessHandler;
    private final OidcAuthenticationFailureHandler oidcAuthenticationFailureHandler;
    private final PasswordChangeRequiredFilter passwordChangeRequiredFilter;
    private final AccountLockoutUserDetailsChecker accountLockoutChecker;

    public SecurityConfig(
            CustomOidcUserService customOidcUserService,
            FormAuthenticationSuccessHandler formAuthenticationSuccessHandler,
            FormAuthenticationFailureHandler formAuthenticationFailureHandler,
            OidcAuthenticationSuccessHandler oidcAuthenticationSuccessHandler,
            OidcAuthenticationFailureHandler oidcAuthenticationFailureHandler,
            PasswordChangeRequiredFilter passwordChangeRequiredFilter,
            AccountLockoutUserDetailsChecker accountLockoutChecker) {
        this.customOidcUserService = customOidcUserService;
        this.formAuthenticationSuccessHandler = formAuthenticationSuccessHandler;
        this.formAuthenticationFailureHandler = formAuthenticationFailureHandler;
        this.oidcAuthenticationSuccessHandler = oidcAuthenticationSuccessHandler;
        this.oidcAuthenticationFailureHandler = oidcAuthenticationFailureHandler;
        this.passwordChangeRequiredFilter = passwordChangeRequiredFilter;
        this.accountLockoutChecker = accountLockoutChecker;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        // Spring Security 7では、コンストラクタでUserDetailsServiceを渡す
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        // ログイン前のロック状態チェックを追加
        provider.setPreAuthenticationChecks(accountLockoutChecker);

        // エラーメッセージを隠蔽（ユーザー名列挙攻撃対策）
        provider.setHideUserNotFoundExceptions(true);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/force-change-password").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(daoAuthenticationProvider)
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(formAuthenticationSuccessHandler)
                .failureHandler(formAuthenticationFailureHandler)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
                .successHandler(oidcAuthenticationSuccessHandler)
                .failureHandler(oidcAuthenticationFailureHandler)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .addFilterAfter(passwordChangeRequiredFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
