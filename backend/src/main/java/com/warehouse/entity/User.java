package com.warehouse.entity;

import com.warehouse.enums.LoginType;
import com.warehouse.enums.UserRole;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体，记录登录账号及角色权限信息。
 */
@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.EMPLOYEE;

    @Column(length = 50)
    private String realName;

    @Column(length = 100)
    private String email;

    @Column(length = 20, unique = true)
    private String phone;

    @Column(length = 255)
    private String avatar;

    // 微信相关字段
    @Column(name = "wechat_openid", length = 100, unique = true)
    private String wechatOpenId;

    @Column(name = "wechat_unionid", length = 100)
    private String wechatUnionId;

    @Column(name = "wechat_nickname", length = 100)
    private String wechatNickname;

    @Column(name = "wechat_avatar", length = 255)
    private String wechatAvatar;

    // 手机验证相关字段
    @Column(name = "phone_verified", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean phoneVerified = false;

    @Column(name = "phone_verification_time")
    private LocalDateTime phoneVerificationTime;

    // 登录方式（用于记录注册或最后登录方式）
    @Column(name = "login_type", length = 20)
    @Enumerated(EnumType.STRING)
    private LoginType loginType = LoginType.USERNAME;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 审计字段
    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "updater_id")
    private Integer updaterId;

    @Column(name = "is_deleted", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDeleted = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleter_id")
    private Integer deleterId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}

