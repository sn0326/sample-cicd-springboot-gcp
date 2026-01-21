package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.exception.InvalidTokenException;
import com.sn0326.cicddemo.exception.RateLimitExceededException;
import com.sn0326.cicddemo.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * パスワードリセット機能のコントローラー
 * OWASP準拠のパスワードリカバリ機能を提供
 */
@Controller
@RequestMapping("/reissue")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * パスワードリセット申請画面表示
     *
     * @return パスワードリセット申請画面のテンプレート名
     */
    @GetMapping("/forgotpassword")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    /**
     * パスワードリセット申請処理
     *
     * @param username ユーザー名
     * @param redirectAttributes リダイレクト時の属性
     * @return リダイレクト先
     */
    @PostMapping("/forgotpassword")
    public String processForgotPassword(
            @RequestParam String username,
            RedirectAttributes redirectAttributes) {

        try {
            passwordResetService.requestPasswordReset(username);

            // セキュリティのため、ユーザー存在有無に関わらず同じメッセージを表示
            redirectAttributes.addFlashAttribute("success",
                    "パスワードリセット用のメールを送信しました。" +
                    "メールに記載されたURLから手続きを進めてください。");

        } catch (RateLimitExceededException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reissue/forgotpassword";
        } catch (Exception e) {
            log.error("Error processing password reset request", e);
            // セキュリティのため、詳細なエラーは表示しない
            redirectAttributes.addFlashAttribute("success",
                    "パスワードリセット用のメールを送信しました。");
        }

        return "redirect:/login";
    }

    /**
     * パスワードリセット画面表示（トークン検証）
     *
     * @param token パスワードリセットトークン
     * @param model ビューモデル
     * @return パスワードリセット画面またはエラー画面のテンプレート名
     */
    @GetMapping("/resetpassword")
    public String showResetPasswordForm(
            @RequestParam String token,
            Model model) {

        try {
            // トークン検証（例外が発生しなければ有効）
            passwordResetService.validateToken(token);

            model.addAttribute("token", token);
            return "reset-password";

        } catch (InvalidTokenException e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password-error";
        }
    }

    /**
     * パスワードリセット実行
     *
     * @param token パスワードリセットトークン
     * @param newPassword 新しいパスワード
     * @param confirmPassword 確認用パスワード
     * @param redirectAttributes リダイレクト時の属性
     * @param model ビューモデル
     * @return リダイレクト先またはビューテンプレート名
     */
    @PostMapping("/resetpassword")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {

        // パスワード一致確認
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "パスワードが一致しません");
            return "reset-password";
        }

        try {
            passwordResetService.resetPassword(token, newPassword);

            redirectAttributes.addFlashAttribute("success",
                    "パスワードを変更しました。新しいパスワードでログインしてください。");
            return "redirect:/login";

        } catch (InvalidTokenException e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password-error";

        } catch (IllegalArgumentException e) {
            // パスワードバリデーションエラー
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }
}
