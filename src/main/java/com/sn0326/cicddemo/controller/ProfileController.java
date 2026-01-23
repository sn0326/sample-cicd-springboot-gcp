package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.dto.OidcConnectionInfo;
import com.sn0326.cicddemo.exception.InvalidPasswordException;
import com.sn0326.cicddemo.exception.RateLimitExceededException;
import com.sn0326.cicddemo.model.OidcProvider;
import com.sn0326.cicddemo.repository.UserRepository;
import com.sn0326.cicddemo.service.EmailChangeService;
import com.sn0326.cicddemo.service.LastLoginService;
import com.sn0326.cicddemo.service.OidcConnectionService;
import com.sn0326.cicddemo.service.PasswordChangeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ユーザープロフィールとOIDC連携管理を行うコントローラー
 */
@Controller
@RequestMapping("/profile")
@Slf4j
public class ProfileController {

    private final OidcConnectionService oidcConnectionService;
    private final LastLoginService lastLoginService;
    private final UserRepository userRepository;
    private final PasswordChangeService passwordChangeService;
    private final EmailChangeService emailChangeService;

    public ProfileController(
            OidcConnectionService oidcConnectionService,
            LastLoginService lastLoginService,
            UserRepository userRepository,
            PasswordChangeService passwordChangeService,
            EmailChangeService emailChangeService) {
        this.oidcConnectionService = oidcConnectionService;
        this.lastLoginService = lastLoginService;
        this.userRepository = userRepository;
        this.passwordChangeService = passwordChangeService;
        this.emailChangeService = emailChangeService;
    }

    /**
     * プロフィールページを表示
     */
    @GetMapping
    public String profile(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String connection_success,
            @RequestParam(required = false) String connection_error) {

        String username = authentication.getName();
        model.addAttribute("username", username);

        // 前回ログイン日時を取得
        Optional<LocalDateTime> lastLogin = lastLoginService.getLastLogin(username);
        lastLogin.ifPresent(dateTime -> model.addAttribute("lastLogin", dateTime));

        // OIDC連携情報を取得
        List<OidcConnectionInfo> connections = oidcConnectionService.getUserConnections(username);
        model.addAttribute("connections", connections);

        // Google連携状態を確認
        boolean googleConnected = oidcConnectionService.isConnected(username, OidcProvider.GOOGLE);
        model.addAttribute("googleConnected", googleConnected);

        // Google連携情報を取得
        if (googleConnected) {
            oidcConnectionService.getConnection(username, OidcProvider.GOOGLE)
                    .ifPresent(info -> model.addAttribute("googleEmail", info.getEmail()));
        }

        // メッセージ表示
        if (connection_success != null) {
            model.addAttribute("successMessage", "Googleアカウントの連携に成功しました");
        }
        if (connection_error != null) {
            model.addAttribute("errorMessage", "連携に失敗しました");
        }

        return "profile";
    }

    /**
     * Google連携を開始
     */
    @GetMapping("/connect/google")
    public String connectGoogle(
            Authentication authentication,
            HttpServletRequest request) {

        String username = authentication.getName();

        // 連携モードをセッションに保存
        HttpSession session = request.getSession();
        session.setAttribute("oidc_connection_mode", true);
        session.setAttribute("connecting_username", username);

        // Google認証へリダイレクト
        return "redirect:/oauth2/authorization/google";
    }

    /**
     * Google連携を解除
     */
    @PostMapping("/disconnect/google")
    public String disconnectGoogle(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();

        oidcConnectionService.deleteConnection(username, OidcProvider.GOOGLE);
        redirectAttributes.addFlashAttribute("successMessage", "Google連携を解除しました");

        return "redirect:/profile";
    }

    /**
     * メールアドレス変更ページを表示
     */
    @GetMapping("/edit/email")
    public String editEmail(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);

        // 現在のメールアドレスを取得
        String email = userRepository.findEmailByUsername(username);
        model.addAttribute("currentEmail", email != null ? email : "未設定");

        return "profile-edit-email";
    }

    /**
     * パスワード変更ページを表示
     */
    @GetMapping("/edit/password")
    public String editPassword(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);

        return "profile-edit-password";
    }

    /**
     * パスワード変更処理
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();

        // パスワード確認チェック
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "新しいパスワードが一致しません");
            return "redirect:/profile/edit/password";
        }

        // パスワード変更実行
        passwordChangeService.changePassword(username, currentPassword, newPassword);

        redirectAttributes.addFlashAttribute("passwordSuccess", "パスワードを変更しました");
        log.info("Password changed successfully for user: {}", username);

        return "redirect:/profile/edit/password";
    }

    /**
     * メールアドレス変更リクエスト処理
     */
    @PostMapping("/change-email")
    public String changeEmail(
            @RequestParam String newEmail,
            @RequestParam String currentPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();

        // メールアドレスのバリデーション
        if (newEmail == null || newEmail.isBlank()) {
            redirectAttributes.addFlashAttribute("emailError", "メールアドレスを入力してください");
            return "redirect:/profile/edit/email";
        }

        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectAttributes.addFlashAttribute("emailError", "有効なメールアドレスを入力してください");
            return "redirect:/profile/edit/email";
        }

        // 現在のメールアドレスと同じかチェック
        String currentEmail = userRepository.findEmailByUsername(username);
        if (newEmail.equals(currentEmail)) {
            redirectAttributes.addFlashAttribute("emailError", "現在のメールアドレスと同じです");
            return "redirect:/profile/edit/email";
        }

        // メールアドレス変更リクエスト実行
        emailChangeService.requestEmailChange(username, newEmail, currentPassword);

        redirectAttributes.addFlashAttribute("emailSuccess",
                "確認メールを " + newEmail + " に送信しました。メール内のリンクをクリックして変更を完了してください。");
        log.info("Email change requested for user: {} to {}", username, newEmail);

        return "redirect:/profile/edit/email";
    }
}
