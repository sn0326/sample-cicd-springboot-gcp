package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.service.ForcePasswordChangeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 強制パスワード変更のコントローラー
 */
@Controller
public class ForcePasswordChangeController {

    private final ForcePasswordChangeService forcePasswordChangeService;

    public ForcePasswordChangeController(ForcePasswordChangeService forcePasswordChangeService) {
        this.forcePasswordChangeService = forcePasswordChangeService;
    }

    @GetMapping("/force-change-password")
    public String showForceChangePasswordForm(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "force-change-password";
    }

    @PostMapping("/force-change-password")
    public String forceChangePassword(
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();

        // パスワード確認
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "新しいパスワードと確認パスワードが一致しません");
            return "redirect:/force-change-password";
        }

        // パスワード変更（現在のパスワード不要）
        forcePasswordChangeService.changePasswordWithoutCurrentPassword(username, newPassword);

        redirectAttributes.addFlashAttribute("success", "パスワードが正常に変更されました");
        return "redirect:/";
    }
}
