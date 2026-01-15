package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.dto.AdminResetPasswordRequest;
import com.sn0326.cicddemo.dto.CreateUserRequest;
import com.sn0326.cicddemo.dto.UserInfo;
import com.sn0326.cicddemo.service.AdminUserManagementService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    /**
     * ユーザー一覧ページ
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<UserInfo> users = adminUserManagementService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    /**
     * ユーザー追加フォーム
     */
    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("createUserRequest", new CreateUserRequest());
        return "admin/user-create";
    }

    /**
     * ユーザー作成処理
     */
    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute CreateUserRequest request,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // バリデーションエラーチェック
        if (bindingResult.hasErrors()) {
            model.addAttribute("createUserRequest", request);
            return "admin/user-create";
        }

        // パスワード一致確認
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "パスワードが一致しません");
            model.addAttribute("createUserRequest", request);
            return "admin/user-create";
        }

        try {
            adminUserManagementService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRoles(),
                    request.isEnabled()
            );

            redirectAttributes.addFlashAttribute("success",
                    "ユーザー '" + request.getUsername() + "' を作成しました");
            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("createUserRequest", request);
            return "admin/user-create";
        }
    }

    /**
     * ユーザー削除処理
     */
    @PostMapping("/users/{username}/delete")
    public String deleteUser(@PathVariable String username,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            String currentUsername = authentication.getName();
            adminUserManagementService.deleteUser(username, currentUsername);
            redirectAttributes.addFlashAttribute("success",
                    "ユーザー '" + username + "' を削除しました");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * パスワードリセットフォーム
     */
    @GetMapping("/users/{username}/reset-password")
    public String resetPasswordForm(@PathVariable String username, Model model) {
        try {
            UserInfo userInfo = adminUserManagementService.getUserInfo(username);
            model.addAttribute("userInfo", userInfo);
            model.addAttribute("resetPasswordRequest", new AdminResetPasswordRequest());
            return "admin/user-reset-password";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/users";
        }
    }

    /**
     * パスワードリセット処理
     */
    @PostMapping("/users/{username}/reset-password")
    public String resetPassword(@PathVariable String username,
                              @Valid @ModelAttribute AdminResetPasswordRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            UserInfo userInfo = adminUserManagementService.getUserInfo(username);
            model.addAttribute("userInfo", userInfo);

            // バリデーションエラーチェック
            if (bindingResult.hasErrors()) {
                model.addAttribute("resetPasswordRequest", request);
                return "admin/user-reset-password";
            }

            // パスワード一致確認
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "パスワードが一致しません");
                model.addAttribute("resetPasswordRequest", request);
                return "admin/user-reset-password";
            }

            adminUserManagementService.resetPassword(username, request.getNewPassword());
            redirectAttributes.addFlashAttribute("success",
                    "ユーザー '" + username + "' のパスワードをリセットしました");
            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("resetPasswordRequest", request);
            return "admin/user-reset-password";
        }
    }

    /**
     * ユーザー有効化処理
     */
    @PostMapping("/users/{username}/enable")
    public String enableUser(@PathVariable String username,
                           RedirectAttributes redirectAttributes) {
        try {
            adminUserManagementService.enableUser(username);
            redirectAttributes.addFlashAttribute("success",
                    "ユーザー '" + username + "' を有効化しました");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * ユーザー無効化処理
     */
    @PostMapping("/users/{username}/disable")
    public String disableUser(@PathVariable String username,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            String currentUsername = authentication.getName();
            adminUserManagementService.disableUser(username, currentUsername);
            redirectAttributes.addFlashAttribute("success",
                    "ユーザー '" + username + "' を無効化しました");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
