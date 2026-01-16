package com.sn0326.cicddemo.security;

import com.sn0326.cicddemo.service.AccountLockoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Component;

/**
 * ログイン前のアカウントロック状態チェッカー
 * TERASOLUNAガイドラインに準拠した実装
 */
@Component
public class AccountLockoutUserDetailsChecker implements UserDetailsChecker {

    private static final Logger logger = LoggerFactory.getLogger(AccountLockoutUserDetailsChecker.class);

    private final AccountLockoutService lockoutService;

    public AccountLockoutUserDetailsChecker(AccountLockoutService lockoutService) {
        this.lockoutService = lockoutService;
    }

    /**
     * ユーザーのロック状態をチェック
     * ロックされている場合、一般的なエラーメッセージをスロー（ロックを明示しない）
     *
     * @param user チェック対象のユーザー
     * @throws LockedException アカウントがロックされている場合
     */
    @Override
    public void check(UserDetails user) {
        // ロック状態をチェック
        if (lockoutService.isAccountLocked(user.getUsername())) {
            // セキュリティのため、一般的なエラーメッセージをスロー
            // ユーザーにはロック状態を明示しない
            logger.info("Login attempt for locked account: {}", user.getUsername());
            throw new LockedException("Bad credentials");
        }
    }
}
