package com.sn0326.cicddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以内で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "ユーザー名は半角英数字、アンダースコア、ハイフンのみ使用できます")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String password;

    @NotBlank(message = "パスワード（確認）は必須です")
    private String confirmPassword;

    @NotEmpty(message = "少なくとも1つのロールを選択してください")
    private List<String> roles;

    private boolean enabled = true;
}
