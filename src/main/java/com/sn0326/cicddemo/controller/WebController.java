package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.dto.ChangePasswordRequest;
import com.sn0326.cicddemo.service.LastLoginService;
import com.sn0326.cicddemo.service.PasswordChangeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class WebController {

    private final PasswordChangeService passwordChangeService;
    private final LastLoginService lastLoginService;

    public WebController(
            PasswordChangeService passwordChangeService,
            LastLoginService lastLoginService) {
        this.passwordChangeService = passwordChangeService;
        this.lastLoginService = lastLoginService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("username", username);
            model.addAttribute("authorities", authentication.getAuthorities());

            // 前回ログイン日時を取得
            Optional<LocalDateTime> lastLogin = lastLoginService.getLastLogin(username);
            lastLogin.ifPresent(dateTime -> model.addAttribute("lastLogin", dateTime));
        }
        return "home";
    }

    @GetMapping("/admin")
    public String admin(Model model, Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("username", username);

            // 前回ログイン日時を取得
            Optional<LocalDateTime> lastLogin = lastLoginService.getLastLogin(username);
            lastLogin.ifPresent(dateTime -> model.addAttribute("lastLogin", dateTime));
        }
        return "admin";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute ChangePasswordRequest request,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("error", "新しいパスワードと確認パスワードが一致しません");
                return "redirect:/change-password";
            }

            passwordChangeService.changePassword(
                    authentication.getName(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            redirectAttributes.addFlashAttribute("success", "パスワードが正常に変更されました");
            return "redirect:/home";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/change-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "パスワード変更中にエラーが発生しました");
            return "redirect:/change-password";
        }
    }
}
