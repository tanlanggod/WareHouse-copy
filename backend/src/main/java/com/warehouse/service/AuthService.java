package com.warehouse.service;

import com.warehouse.dto.*;
import com.warehouse.entity.User;
import com.warehouse.enums.LoginType;
import com.warehouse.enums.UserRole;
import com.warehouse.repository.UserRepository;
import com.warehouse.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证服务，负责登录校验与 JWT 生成。
 */
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private SmsService smsService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        // 如果loginType为null，设置为USERNAME作为默认值
        response.setLoginType(user.getLoginType() != null ? user.getLoginType().name() : LoginType.USERNAME.name());
        return response;
    }

    /**
     * 微信登录
     */
    public LoginResponse wechatLogin(WeChatLoginRequest request) {
        User user = weChatService.login(request);

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        // 如果loginType为null，设置为WECHAT作为默认值
        response.setLoginType(user.getLoginType() != null ? user.getLoginType().name() : LoginType.WECHAT.name());

        return response;
    }

    /**
     * 手机号登录
     */
    public LoginResponse phoneLogin(PhoneLoginRequest request) {
        // 验证验证码
        if (!smsService.verifyCode(request.getPhone(), request.getVerificationCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("该手机号尚未注册"));

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 更新手机验证状态和登录方式
        if (!Boolean.TRUE.equals(user.getPhoneVerified())) {
            user.setPhoneVerified(true);
            user.setPhoneVerificationTime(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
        }
        // 如果当前登录方式不是PHONE，更新为PHONE
        if (user.getLoginType() != LoginType.PHONE) {
            user.setLoginType(LoginType.PHONE);
            user.setUpdatedAt(LocalDateTime.now());
        }
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        // 设置登录方式为PHONE
        response.setLoginType(LoginType.PHONE.name());

        return response;
    }

    /**
     * 发送手机验证码
     */
    public void sendVerificationCode(SendVerificationCodeRequest request) {
        // 检查手机号是否已注册（如果需要注册功能的话）
        // User user = userRepository.findByPhone(request.getPhone())
        //         .orElseThrow(() -> new RuntimeException("该手机号尚未注册"));

        smsService.sendVerificationCode(request.getPhone());
    }

    /**
     * 绑定手机号
     */
    @Transactional
    public User bindPhone(User user, String phone, String verificationCode) {
        // 验证验证码
        if (!smsService.verifyCode(phone, verificationCode)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查手机号是否已被其他用户使用
        Optional<User> existingUserOptional = userRepository.findByPhone(phone);
        if (existingUserOptional.isPresent() && !existingUserOptional.get().getId().equals(user.getId())) {
            throw new RuntimeException("该手机号已被其他用户绑定");
        }

        // 绑定手机号
        user.setPhone(phone);
        user.setPhoneVerified(true);
        user.setPhoneVerificationTime(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 绑定微信账号
     */
    @Transactional
    public User bindWeChat(User user, String code) {
        return weChatService.bindWeChat(user, code);
    }

    /**
     * 解绑微信账号
     */
    @Transactional
    public User unbindWeChat(User user) {
        return weChatService.unbindWeChat(user);
    }

    /**
     * 用户注册
     */
    @Transactional
    public User register(UserRegisterRequest request) {
        // 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
        }

        // 检查手机号是否已存在（如果提供了手机号）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("手机号已被使用");
            }
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.EMPLOYEE); // 默认为普通员工
        user.setLoginType(LoginType.USERNAME); // 设置登录方式
        user.setStatus(1); // 启用状态
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 保存用户
        return userRepository.save(user);
    }
}

