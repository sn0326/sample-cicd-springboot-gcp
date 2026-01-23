package com.sn0326.cicddemo.service;

import com.sn0326.cicddemo.dto.UserInfo;
import com.sn0326.cicddemo.exception.InvalidPasswordException;
import com.sn0326.cicddemo.exception.UserValidationException;
import com.sn0326.cicddemo.service.notification.SecurityNotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementService {

    private final JdbcUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final SecurityNotificationService notificationService;
    private final ForcePasswordChangeService forcePasswordChangeService;
    private final AccountLockoutService lockoutService;

    public AdminUserManagementService(JdbcUserDetailsManager userDetailsManager,
                                      PasswordEncoder passwordEncoder,
                                      JdbcTemplate jdbcTemplate,
                                      SecurityNotificationService notificationService,
                                      ForcePasswordChangeService forcePasswordChangeService,
                                      AccountLockoutService lockoutService) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.notificationService = notificationService;
        this.forcePasswordChangeService = forcePasswordChangeService;
        this.lockoutService = lockoutService;
    }

    /**
     * ユーザー一覧を取得
     */
    public List<UserInfo> getAllUsers() {
        String sql = "SELECT username, enabled FROM users ORDER BY username";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String username = rs.getString("username");
            boolean enabled = rs.getBoolean("enabled");
            List<String> authorities = getAuthoritiesByUsername(username);
            return new UserInfo(username, enabled, authorities);
        });
    }

    /**
     * 特定のユーザー情報を取得
     */
    public UserInfo getUserInfo(String username) {
        if (!userDetailsManager.userExists(username)) {
            throw new UserValidationException("ユーザーが存在しません: " + username);
        }

        UserDetails userDetails = userDetailsManager.loadUserByUsername(username);
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new UserInfo(username, userDetails.isEnabled(), authorities);
    }

    /**
     * 新しいユーザーを作成
     */
    @Transactional
    public void createUser(String username, String password, List<String> roles, boolean enabled) {
        // バリデーション
        if (username == null || username.trim().isEmpty()) {
            throw new UserValidationException("ユーザー名は必須です");
        }
        if (password == null || password.length() < 8) {
            throw new InvalidPasswordException("パスワードは8文字以上必要です");
        }
        if (userDetailsManager.userExists(username)) {
            throw new UserValidationException("ユーザー名 '" + username + "' は既に存在します");
        }
        if (roles == null || roles.isEmpty()) {
            throw new UserValidationException("少なくとも1つのロールを選択してください");
        }

        // 権限リストを作成
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // UserDetailsオブジェクトを作成
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .disabled(!enabled)
                .authorities(authorities)
                .build();

        // ユーザーを作成
        userDetailsManager.createUser(user);

        // 初期パスワードのため、パスワード変更を要求
        forcePasswordChangeService.requirePasswordChange(username, "Initial password");
    }

    /**
     * ユーザーを削除
     */
    @Transactional
    public void deleteUser(String username, String currentUsername) {
        // 自分自身を削除できないようにする
        if (username.equals(currentUsername)) {
            throw new UserValidationException("自分自身を削除することはできません");
        }

        if (!userDetailsManager.userExists(username)) {
            throw new UserValidationException("ユーザーが存在しません: " + username);
        }

        userDetailsManager.deleteUser(username);
    }

    /**
     * パスワードをリセット（管理者用）
     */
    @Transactional
    public void resetPassword(String username, String newPassword) {
        if (!userDetailsManager.userExists(username)) {
            throw new UserValidationException("ユーザーが存在しません: " + username);
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new InvalidPasswordException("パスワードは8文字以上必要です");
        }

        // 既存のユーザー情報を取得
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);

        // パスワードのみを更新した新しいUserDetailsを作成
        UserDetails updatedUser = User.builder()
                .username(existingUser.getUsername())
                .password(passwordEncoder.encode(newPassword))
                .disabled(!existingUser.isEnabled())
                .authorities(existingUser.getAuthorities())
                .build();

        userDetailsManager.updateUser(updatedUser);

        // リセットされたパスワードは仮パスワードのため、パスワード変更を要求
        forcePasswordChangeService.requirePasswordChange(username, "Password reset by admin");

        // パスワードリセット通知を送信
        // TODO: 実際のメールアドレスを取得する（現在はusersテーブルにemailカラムがないため仮のアドレスを使用）
        String email = username + "@example.com";
        String resetBy = getCurrentUsername();
        notificationService.sendPasswordResetNotification(username, email, newPassword, resetBy);
    }

    /**
     * ユーザーを有効化
     */
    @Transactional
    public void enableUser(String username) {
        updateUserEnabledStatus(username, true);

        // アカウント有効化通知を送信
        // TODO: 実際のメールアドレスを取得する（現在はusersテーブルにemailカラムがないため仮のアドレスを使用）
        String email = username + "@example.com";
        String enabledBy = getCurrentUsername();
        notificationService.sendAccountEnabledNotification(username, email, enabledBy);
    }

    /**
     * ユーザーを無効化
     */
    @Transactional
    public void disableUser(String username, String currentUsername) {
        // 自分自身を無効化できないようにする
        if (username.equals(currentUsername)) {
            throw new UserValidationException("自分自身を無効化することはできません");
        }

        updateUserEnabledStatus(username, false);

        // アカウント無効化通知を送信
        // TODO: 実際のメールアドレスを取得する（現在はusersテーブルにemailカラムがないため仮のアドレスを使用）
        String email = username + "@example.com";
        String disabledBy = getCurrentUsername();
        notificationService.sendAccountDisabledNotification(username, email, "管理者により無効化されました", disabledBy);
    }

    /**
     * ユーザーの有効/無効状態を更新
     */
    private void updateUserEnabledStatus(String username, boolean enabled) {
        if (!userDetailsManager.userExists(username)) {
            throw new UserValidationException("ユーザーが存在しません: " + username);
        }

        // 既存のユーザー情報を取得
        UserDetails existingUser = userDetailsManager.loadUserByUsername(username);

        // 有効/無効状態のみを更新した新しいUserDetailsを作成
        UserDetails updatedUser = User.builder()
                .username(existingUser.getUsername())
                .password(existingUser.getPassword())
                .disabled(!enabled)
                .authorities(existingUser.getAuthorities())
                .build();

        userDetailsManager.updateUser(updatedUser);
    }

    /**
     * ユーザー名から権限リストを取得
     */
    private List<String> getAuthoritiesByUsername(String username) {
        String sql = "SELECT authority FROM authorities WHERE username = ? ORDER BY authority";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("authority"), username);
    }

    /**
     * アカウントのロックを解除（管理者用）
     */
    @Transactional
    public void unlockAccount(String username) {
        if (!userDetailsManager.userExists(username)) {
            throw new UserValidationException("ユーザーが存在しません: " + username);
        }

        lockoutService.unlockAccount(username);
    }

    /**
     * アカウントのロック状態を確認
     */
    public boolean isAccountLocked(String username) {
        if (!userDetailsManager.userExists(username)) {
            throw new UserValidationException("ユーザーが存在しません: " + username);
        }

        return lockoutService.isAccountLocked(username);
    }

    /**
     * 現在ログイン中のユーザー名を取得
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
