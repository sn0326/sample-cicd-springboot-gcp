package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.exception.InvalidTokenException;
import com.sn0326.cicddemo.service.EmailChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * メールアドレス確認を行うコントローラー
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final EmailChangeService emailChangeService;

    /**
     * メールアドレス確認処理
     *
     * @param token 確認トークン
     * @param model モデル
     * @return ビュー名
     */
    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam(required = false) String token,
            Model model) {

        if (token == null || token.isBlank()) {
            model.addAttribute("error", "トークンが指定されていません");
            return "verify-email-error";
        }

        try {
            // メールアドレス変更を実行
            emailChangeService.verifyEmail(token);

            model.addAttribute("success", true);
            model.addAttribute("message", "メールアドレスの変更が完了しました");
            log.info("Email verification successful for token: {}", token.substring(0, 8) + "...");

            return "verify-email";

        } catch (InvalidTokenException e) {
            model.addAttribute("error", e.getMessage());
            log.warn("Email verification failed: {}", e.getMessage());
            return "verify-email-error";

        } catch (Exception e) {
            model.addAttribute("error", "メールアドレスの確認中にエラーが発生しました");
            log.error("Unexpected error during email verification", e);
            return "verify-email-error";
        }
    }
}
