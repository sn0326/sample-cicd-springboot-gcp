package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.service.PasskeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * パスキー（WebAuthn）の登録・管理を行うコントローラー
 */
@Slf4j
@Controller
@RequestMapping("/passkey")
@RequiredArgsConstructor
public class PasskeyController {

    private final PasskeyService passkeyService;

    /**
     * パスキー管理画面を表示
     *
     * @param authentication 認証情報
     * @param model          モデル
     * @return ビュー名
     */
    @GetMapping
    public String passkeyManagement(Authentication authentication, Model model) {
        String username = getUsername(authentication);
        boolean hasPasskey = passkeyService.hasPasskey(username);

        model.addAttribute("username", username);
        model.addAttribute("hasPasskey", hasPasskey);

        return "passkey/management";
    }

    /**
     * パスキー登録状態を確認するAPI
     *
     * @param authentication 認証情報
     * @return パスキー登録状態
     */
    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> getPasskeyStatus(Authentication authentication) {
        String username = getUsername(authentication);
        boolean hasPasskey = passkeyService.hasPasskey(username);

        Map<String, Object> response = new HashMap<>();
        response.put("hasPasskey", hasPasskey);
        response.put("username", username);

        return response;
    }

    /**
     * パスキー登録を開始するAPI
     * フロントエンドがWebAuthn APIを呼び出す前にユーザーエンティティIDを取得
     *
     * @param authentication 認証情報
     * @return ユーザーエンティティ情報
     */
    @PostMapping("/register/initiate")
    @ResponseBody
    public Map<String, Object> initiatePasskeyRegistration(Authentication authentication) {
        String username = getUsername(authentication);
        String displayName = getDisplayName(authentication);

        log.info("パスキー登録開始リクエスト: username={}", username);

        String userEntityId = passkeyService.initiatePasskeyRegistration(username, displayName);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userEntityId", userEntityId);
        response.put("username", username);
        response.put("displayName", displayName);

        log.info("パスキー登録開始成功: username={}, entityId={}", username, userEntityId);

        return response;
    }

    /**
     * パスキー削除API
     *
     * @param authentication 認証情報
     * @return 削除結果
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deletePasskey(Authentication authentication) {
        String username = getUsername(authentication);

        log.info("パスキー削除リクエスト: username={}", username);

        passkeyService.deletePasskey(username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "パスキーを削除しました");

        log.info("パスキー削除成功: username={}", username);

        return response;
    }

    /**
     * 認証情報からユーザー名を取得
     *
     * @param authentication 認証情報
     * @return ユーザー名
     */
    private String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        } else if (principal instanceof PublicKeyCredentialUserEntity userEntity) {
            return userEntity.getName();
        }

        return principal.toString();
    }

    /**
     * 認証情報から表示名を取得
     *
     * @param authentication 認証情報
     * @return 表示名
     */
    private String getDisplayName(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof PublicKeyCredentialUserEntity userEntity) {
            String displayName = userEntity.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                return displayName;
            }
        } else if (principal instanceof OidcUser oidcUser) {
            String name = oidcUser.getFullName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }

        return getUsername(authentication);
    }
}
