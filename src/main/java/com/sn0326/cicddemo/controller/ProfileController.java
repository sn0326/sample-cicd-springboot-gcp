package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.dto.OidcConnectionInfo;
import com.sn0326.cicddemo.model.OidcProvider;
import com.sn0326.cicddemo.service.OidcConnectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ユーザープロフィールとOIDC連携管理を行うコントローラー
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final OidcConnectionService oidcConnectionService;

    public ProfileController(OidcConnectionService oidcConnectionService) {
        this.oidcConnectionService = oidcConnectionService;
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

        try {
            oidcConnectionService.deleteConnection(username, OidcProvider.GOOGLE);
            redirectAttributes.addFlashAttribute("successMessage", "Google連携を解除しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "連携解除に失敗しました: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}
