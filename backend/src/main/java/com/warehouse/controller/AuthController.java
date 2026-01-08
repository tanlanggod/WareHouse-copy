package com.warehouse.controller;

import com.warehouse.common.Result;
import com.warehouse.dto.*;
import com.warehouse.entity.User;
import com.warehouse.service.AuthService;
import com.warehouse.service.CaptchaService;
import com.warehouse.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口控制器，提供登录功能。
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成验证码
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> generateCaptcha() {
        return captchaService.generateCaptcha();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        // 首先验证图片验证码
        if (!captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptchaCode())) {
            return Result.error("图片验证码错误或已过期");
        }

        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@Validated @RequestBody UserRegisterRequest request) {
        try {
            User user = authService.register(request);
            // 不返回密码信息
            user.setPassword(null);
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 微信登录
     */
    @PostMapping("/wechat-login")
    public Result<LoginResponse> wechatLogin(@Validated @RequestBody WeChatLoginRequest request) {
        try {
            LoginResponse response = authService.wechatLogin(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 手机号登录
     */
    @PostMapping("/phone-login")
    public Result<LoginResponse> phoneLogin(@Validated @RequestBody PhoneLoginRequest request) {
        try {
            // 首先验证图片验证码
            if (!captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptchaCode())) {
                return Result.error("图片验证码错误或已过期");
            }

            LoginResponse response = authService.phoneLogin(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("/send-verification-code")
    public Result<String> sendVerificationCode(@Validated @RequestBody SendVerificationCodeRequest request) {
        try {
            // 验证图片验证码（不删除，允许登录时再次使用）
            if (!captchaService.validateCaptchaWithoutDelete(request.getCaptchaId(), request.getCaptchaCode())) {
                return Result.error("图片验证码错误或已过期");
            }

            smsService.sendVerificationCode(request.getPhone());
            return Result.success("验证码发送成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

