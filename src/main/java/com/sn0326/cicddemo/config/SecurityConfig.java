package com.sn0326.cicddemo.config;

import com.sn0326.cicddemo.security.CustomOidcUserService;
import com.sn0326.cicddemo.security.FormAuthenticationSuccessHandler;
import com.sn0326.cicddemo.security.OidcAuthenticationFailureHandler;
import com.sn0326.cicddemo.security.OidcAuthenticationSuccessHandler;
import com.sn0326.cicddemo.security.PasswordChangeRequiredFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final FormAuthenticationSuccessHandler formAuthenticationSuccessHandler;
    private final OidcAuthenticationSuccessHandler oidcAuthenticationSuccessHandler;
    private final OidcAuthenticationFailureHandler oidcAuthenticationFailureHandler;
    private final PasswordChangeRequiredFilter passwordChangeRequiredFilter;

    public SecurityConfig(
            CustomOidcUserService customOidcUserService,
            FormAuthenticationSuccessHandler formAuthenticationSuccessHandler,
            OidcAuthenticationSuccessHandler oidcAuthenticationSuccessHandler,
            OidcAuthenticationFailureHandler oidcAuthenticationFailureHandler,
            PasswordChangeRequiredFilter passwordChangeRequiredFilter) {
        this.customOidcUserService = customOidcUserService;
        this.formAuthenticationSuccessHandler = formAuthenticationSuccessHandler;
        this.oidcAuthenticationSuccessHandler = oidcAuthenticationSuccessHandler;
        this.oidcAuthenticationFailureHandler = oidcAuthenticationFailureHandler;
        this.passwordChangeRequiredFilter = passwordChangeRequiredFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/force-change-password").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(formAuthenticationSuccessHandler)
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
