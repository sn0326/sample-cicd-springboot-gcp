package com.sn0326.cicddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminResetPasswordRequest {
    @NotBlank(message = "新しいパスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String newPassword;

    @NotBlank(message = "パスワード（確認）は必須です")
    private String confirmPassword;

    public AdminResetPasswordRequest() {
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
