package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.exception.InvalidPasswordException;
import com.sn0326.cicddemo.repository.PasswordChangeRequirementRepository;
import com.sn0326.cicddemo.service.notification.SecurityNotificationService;
import com.sn0326.cicddemo.validator.PasswordValidationResult;
import com.sn0326.cicddemo.validator.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;

/**
 * 強制パスワード変更を管理するサービス
 */
@Service
public class ForcePasswordChangeService {

    private static final Logger log = LoggerFactory.getLogger(ForcePasswordChangeService.class);

    private final PasswordChangeRequirementRepository repository;
    private final JdbcUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final SecurityNotificationService notificationService;

    public ForcePasswordChangeService(
            PasswordChangeRequirementRepository repository,
            JdbcUserDetailsManager userDetailsManager,
            PasswordEncoder passwordEncoder,
            PasswordValidator passwordValidator,
            SecurityNotificationService notificationService) {
        this.repository = repository;
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
        this.notificationService = notificationService;
    }

    /**
     * ユーザーがパスワード変更を要求されているかチェック
     *
     * @param username ユーザー名
     * @return パスワード変更が必要な場合true
     */
    public boolean isPasswordChangeRequired(String username) {
        return repository.isPasswordChangeRequired(username);
    }

    /**
     * ユーザーにパスワード変更を要求する
     *
     * @param username ユーザー名
     * @param reason パスワード変更が必要な理由
     */
    public void requirePasswordChange(String username, String reason) {
        repository.requirePasswordChange(username);
        log.info("Password change required for user: {} (reason: {})", username, reason);
    }

    /**
     * パスワード変更要求をクリアする
     *
     * @param username ユーザー名
     */
    public void clearPasswordChangeRequirement(String username) {
        repository.clearPasswordChangeRequirement(username);
        log.info("Password change requirement cleared for user: {}", username);
    }

    /**
     * 現在のパスワードなしでパスワードを変更する（強制変更用）
     * 新パスワードの検証を行い、変更後にフラグをクリアする
     *
     * @param username ユーザー名
     * @param newPassword 新しいパスワード
     * @throws InvalidPasswordException パスワード検証に失敗した場合
     */
    public void changePasswordWithoutCurrentPassword(String username, String newPassword) {
        // 新しいパスワードを検証
        PasswordValidationResult validationResult = passwordValidator.validate(newPassword, username);

        if (!validationResult.isValid()) {
            throw new InvalidPasswordException(validationResult.getErrorMessage());
        }

        // ユーザー情報を取得
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);

        // パスワードを更新
        UserDetails updatedUser = User.builder()
                .username(existingUser.getUsername())
                .password(passwordEncoder.encode(newPassword))
                .disabled(!existingUser.isEnabled())
                .authorities(existingUser.getAuthorities())
                .build();

        userDetailsManager.updateUser(updatedUser);

        // パスワード変更要求フラグをクリア
        clearPasswordChangeRequirement(username);

        // パスワード変更通知を送信
        // TODO: 実際のメールアドレスを取得する（現在はusersテーブルにemailカラムがないため仮のアドレスを使用）
        String email = username + "@example.com";
        notificationService.sendPasswordChangedNotification(username, email);

        log.info("Password changed successfully for user: {} (forced change)", username);
    }
}
