package com.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.dto.WeChatLoginRequest;
import com.warehouse.dto.WeChatUserInfo;
import com.warehouse.entity.User;
import com.warehouse.enums.LoginType;
import com.warehouse.enums.UserRole;
import com.warehouse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * 微信登录服务
 */
@Service
public class WeChatService {

    @Value("${app.wechat.app-id:}")
    private String appId;

    @Value("${app.wechat.app-secret:}")
    private String appSecret;

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 微信登录
     */
    public User login(WeChatLoginRequest request) {
        try {
            // 1. 通过code获取access_token和openid
            WeChatAccessTokenResponse tokenResponse = getAccessToken(request.getCode());
            if (tokenResponse == null || tokenResponse.getOpenId() == null) {
                throw new RuntimeException("获取微信用户信息失败");
            }

            // 2. 根据openid查找用户
            User user = userRepository.findByWechatOpenId(tokenResponse.getOpenId());

            // 3. 用户不存在则自动注册
            if (user == null) {
                user = registerWeChatUser(tokenResponse);
            } else {
                // 4. 用户已存在，更新最后登录时间和登录方式
                user.setLoginType(LoginType.WECHAT);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }

            return user;
        } catch (Exception e) {
            throw new RuntimeException("微信登录失败：" + e.getMessage());
        }
    }

    /**
     * 通过微信code获取access_token
     */
    private WeChatAccessTokenResponse getAccessToken(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId, appSecret, code
        );

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                String errmsg = jsonNode.get("errmsg").asText();
                throw new RuntimeException("微信API错误：" + errcode + " - " + errmsg);
            }

            WeChatAccessTokenResponse tokenResponse = new WeChatAccessTokenResponse();
            tokenResponse.setOpenId(jsonNode.get("openid").asText());
            tokenResponse.setAccessToken(jsonNode.get("access_token").asText());
            tokenResponse.setExpiresIn(jsonNode.get("expires_in").asInt());
            tokenResponse.setRefreshToken(jsonNode.get("refresh_token").asText());
            tokenResponse.setScope(jsonNode.get("scope").asText());

            if (jsonNode.has("unionid")) {
                tokenResponse.setUnionId(jsonNode.get("unionid").asText());
            }

            return tokenResponse;
        } catch (Exception e) {
            throw new RuntimeException("获取微信access_token失败：" + e.getMessage());
        }
    }

    /**
     * 获取微信用户信息
     */
    private WeChatUserInfo getWeChatUserInfo(String accessToken, String openId) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s",
                accessToken, openId
        );

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                String errmsg = jsonNode.get("errmsg").asText();
                throw new RuntimeException("获取微信用户信息失败：" + errcode + " - " + errmsg);
            }

            WeChatUserInfo userInfo = new WeChatUserInfo();
            userInfo.setOpenId(jsonNode.get("openid").asText());
            userInfo.setNickname(jsonNode.get("nickname").asText());
            userInfo.setAvatarUrl(jsonNode.get("headimgurl").asText());
            userInfo.setSex(jsonNode.get("sex").asText());
            userInfo.setProvince(jsonNode.get("province").asText());
            userInfo.setCity(jsonNode.get("city").asText());
            userInfo.setCountry(jsonNode.get("country").asText());

            if (jsonNode.has("unionid")) {
                userInfo.setUnionId(jsonNode.get("unionid").asText());
            }

            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("获取微信用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 注册微信用户
     */
    private User registerWeChatUser(WeChatAccessTokenResponse tokenResponse) {
        try {
            // 获取用户详细信息
            WeChatUserInfo userInfo = getWeChatUserInfo(tokenResponse.getAccessToken(), tokenResponse.getOpenId());

            User user = new User();
            user.setUsername("wx_" + tokenResponse.getOpenId().substring(0, 8)); // 使用openid前8位作为用户名
            user.setPassword(""); // 微信用户无密码
            user.setRealName(userInfo.getNickname());
            user.setWechatOpenId(userInfo.getOpenId());
            user.setWechatUnionId(userInfo.getUnionId());
            user.setWechatNickname(userInfo.getNickname());
            user.setWechatAvatar(userInfo.getAvatarUrl());
            user.setAvatar(userInfo.getAvatarUrl()); // 同时设置头像
            user.setRole(UserRole.EMPLOYEE); // 默认角色
            user.setLoginType(LoginType.WECHAT);
            user.setPhoneVerified(false); // 微信用户需要绑定手机号后才验证

            return userRepository.save(user);
        } catch (Exception e) {
            // 如果获取用户信息失败，创建基本信息用户
            User user = new User();
            user.setUsername("wx_" + tokenResponse.getOpenId().substring(0, 8));
            user.setPassword("");
            user.setRealName("微信用户");
            user.setWechatOpenId(tokenResponse.getOpenId());
            user.setWechatUnionId(tokenResponse.getUnionId());
            user.setWechatNickname("微信用户");
            user.setRole(UserRole.EMPLOYEE);
            user.setLoginType(LoginType.WECHAT);
            user.setPhoneVerified(false);

            return userRepository.save(user);
        }
    }

    /**
     * 绑定微信账号到现有用户
     */
    public User bindWeChat(User user, String code) {
        if (user.getWechatOpenId() != null) {
            throw new RuntimeException("该账号已绑定微信");
        }

        WeChatAccessTokenResponse tokenResponse = getAccessToken(code);
        if (tokenResponse == null || tokenResponse.getOpenId() == null) {
            throw new RuntimeException("获取微信用户信息失败");
        }

        // 检查微信账号是否已被其他用户绑定
        User existingUser = userRepository.findByWechatOpenId(tokenResponse.getOpenId());
        if (existingUser != null) {
            throw new RuntimeException("该微信账号已被其他用户绑定");
        }

        user.setWechatOpenId(tokenResponse.getOpenId());
        user.setWechatUnionId(tokenResponse.getUnionId());
        user.setLoginType(LoginType.WECHAT);

        // 尝试获取微信用户信息
        try {
            WeChatUserInfo userInfo = getWeChatUserInfo(tokenResponse.getAccessToken(), tokenResponse.getOpenId());
            user.setWechatNickname(userInfo.getNickname());
            user.setWechatAvatar(userInfo.getAvatarUrl());
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                user.setAvatar(userInfo.getAvatarUrl());
            }
        } catch (Exception e) {
            System.err.println("获取微信用户信息失败：" + e.getMessage());
        }

        return userRepository.save(user);
    }

    /**
     * 解绑微信账号
     */
    public User unbindWeChat(User user) {
        if (user.getWechatOpenId() == null) {
            throw new RuntimeException("该账号未绑定微信");
        }

        user.setWechatOpenId(null);
        user.setWechatUnionId(null);
        user.setWechatNickname(null);
        user.setWechatAvatar(null);

        // 如果登录方式是微信，改回用户名登录
        if (user.getLoginType() == LoginType.WECHAT) {
            user.setLoginType(LoginType.USERNAME);
        }

        return userRepository.save(user);
    }

    /**
     * 微信access_token响应
     */
    private static class WeChatAccessTokenResponse {
        private String openId;
        private String accessToken;
        private String refreshToken;
        private int expiresIn;
        private String scope;
        private String unionId;

        // Getters and Setters
        public String getOpenId() { return openId; }
        public void setOpenId(String openId) { this.openId = openId; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public int getExpiresIn() { return expiresIn; }
        public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public String getUnionId() { return unionId; }
        public void setUnionId(String unionId) { this.unionId = unionId; }
    }
}