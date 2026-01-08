package com.warehouse.dto;

import lombok.Data;

/**
 * 微信用户信息
 */
@Data
public class WeChatUserInfo {

    private String openId;
    private String unionId;
    private String nickname;
    private String avatarUrl;
    private String sex;
    private String province;
    private String city;
    private String country;
}