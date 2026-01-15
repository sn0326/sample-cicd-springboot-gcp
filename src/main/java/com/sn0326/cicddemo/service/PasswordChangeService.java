package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.validator.PasswordValidationResult;
import com.sn0326.cicddemo.validator.PasswordValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class PasswordChangeService {

    private final JdbcUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public PasswordChangeService(
            JdbcUserDetailsManager userDetailsManager,
            PasswordEncoder passwordEncoder,
            PasswordValidator passwordValidator) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        // 新しいパスワードを検証
        PasswordValidationResult validationResult = passwordValidator.validate(newPassword, username);

        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(validationResult.getErrorMessage());
        }

        // 現在のパスワードを確認
        UserDetails user = userDetailsManager.loadUserByUsername(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("現在のパスワードが正しくありません");
        }

        // パスワードを変更
        userDetailsManager.changePassword(currentPassword, passwordEncoder.encode(newPassword));
    }
}
