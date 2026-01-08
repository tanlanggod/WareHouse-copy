package com.warehouse.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求
 */
@Data
public class WeChatLoginRequest {

    @NotBlank(message = "微信授权码不能为空")
    private String code;

    private String state;
}