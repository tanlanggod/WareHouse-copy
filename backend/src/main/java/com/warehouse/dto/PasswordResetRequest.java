package com.warehouse.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 密码重置请求对象
 */
public class PasswordResetRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String newPassword;

    private Boolean forceChangeOnNextLogin = true; // 是否强制下次登录修改密码

    // Getters and Setters
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Boolean getForceChangeOnNextLogin() {
        return forceChangeOnNextLogin;
    }

    public void setForceChangeOnNextLogin(Boolean forceChangeOnNextLogin) {
        this.forceChangeOnNextLogin = forceChangeOnNextLogin;
    }
}