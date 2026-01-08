package com.warehouse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 系统公告实体
 */
@Entity
@Table(name = "system_announcement", indexes = {
    @Index(name = "idx_announcement_status", columnList = "status"),
    @Index(name = "idx_announcement_target", columnList = "target_audience"),
    @Index(name = "idx_announcement_effective_time", columnList = "effective_start_time, effective_end_time"),
    @Index(name = "idx_announcement_created_at", columnList = "created_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler"})
@EntityListeners(AuditingEntityListener.class)
public class SystemAnnouncement extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "announcement_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    @Column(name = "priority", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Column(name = "target_audience", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TargetAudience targetAudience = TargetAudience.ALL;

    @Column(name = "target_roles", length = 500)
    private String targetRoles; // JSON数组存储目标角色

    @Column(name = "display_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DisplayType displayType = DisplayType.NOTIFICATION;

    @Column(name = "is_pinned", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isPinned = false;

    @Column(name = "effective_start_time")
    private LocalDateTime effectiveStartTime;

    @Column(name = "effective_end_time")
    private LocalDateTime effectiveEndTime;

    @Column(name = "read_count", columnDefinition = "INT DEFAULT 0")
    private Integer readCount = 0;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "publisher_id")
    private Integer publisherId;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "attachment_name", length = 200)
    private String attachmentName;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (status == AnnouncementStatus.PUBLISHED && publishTime == null) {
            publishTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        // 当状态从非PUBLISHED变为PUBLISHED时，设置发布时间
        if (status == AnnouncementStatus.PUBLISHED && publishTime == null) {
            publishTime = LocalDateTime.now();
        }
    }

    /**
     * 检查公告当前是否有效
     * @return true表示当前有效，false表示无效
     */
    @JsonIgnore
    public boolean isCurrentlyEffective() {
        LocalDateTime now = LocalDateTime.now();

        // 检查状态
        if (status != AnnouncementStatus.PUBLISHED) {
            return false;
        }

        // 检查生效开始时间
        if (effectiveStartTime != null && now.isBefore(effectiveStartTime)) {
            return false;
        }

        // 检查生效结束时间
        if (effectiveEndTime != null && now.isAfter(effectiveEndTime)) {
            return false;
        }

        return true;
    }

    /**
     * 发布公告
     * @param publisherId 发布人ID
     */
    public void publish(Integer publisherId) {
        this.status = AnnouncementStatus.PUBLISHED;
        this.publisherId = publisherId;
        this.publishTime = LocalDateTime.now();
    }

    /**
     * 取消公告
     */
    public void cancel() {
        this.status = AnnouncementStatus.CANCELLED;
    }

    /**
     * 标记为已过期
     */
    public void markAsExpired() {
        this.status = AnnouncementStatus.EXPIRED;
    }

    /**
     * 增加阅读次数
     */
    public void incrementReadCount() {
        this.readCount = (this.readCount == null ? 0 : this.readCount) + 1;
    }

    /**
     * 获取目标角色列表
     * @return 目标角色列表
     */
    @JsonIgnore
    public List<String> getTargetRolesList() {
        if (targetRoles == null || targetRoles.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(targetRoles.split(","));
    }

    /**
     * 设置目标角色列表
     * @param roles 角色列表
     */
    public void setTargetRolesList(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            this.targetRoles = null;
        } else {
            this.targetRoles = String.join(",", roles);
        }
    }

    /**
     * 检查用户是否有权限查看此公告
     * @param userRole 用户角色
     * @return true表示有权限，false表示无权限
     */
    @JsonIgnore
    public boolean isAccessibleByRole(String userRole) {
        if (targetAudience == TargetAudience.ALL) {
            return true;
        }

        if (targetAudience == TargetAudience.ROLE_BASED) {
            List<String> targetRolesList = getTargetRolesList();
            return targetRolesList.contains(userRole);
        }

        return false;
    }

    /**
     * 获取优先级描述
     */
    @JsonIgnore
    public String getPriorityDescription() {
        switch (priority) {
            case URGENT: return "紧急";
            case HIGH: return "高";
            case NORMAL: return "普通";
            case LOW: return "低";
            default: return "普通";
        }
    }

    /**
     * 获取状态描述
     */
    @JsonIgnore
    public String getStatusDescription() {
        switch (status) {
            case PUBLISHED: return "已发布";
            case DRAFT: return "草稿";
            case EXPIRED: return "已过期";
            case CANCELLED: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 获取类型描述
     */
    @JsonIgnore
    public String getTypeDescription() {
        switch (type) {
            case SYSTEM: return "系统公告";
            case MAINTENANCE: return "维护通知";
            case FEATURE: return "功能更新";
            case HOLIDAY: return "节假日通知";
            case URGENT: return "紧急通知";
            default: return "其他";
        }
    }

    // ==================== 枚举定义 ====================

    /**
     * 公告类型
     */
    public enum AnnouncementType {
        SYSTEM,      // 系统公告
        MAINTENANCE, // 维护通知
        FEATURE,     // 功能更新
        HOLIDAY,     // 节假日通知
        URGENT       // 紧急通知
    }

    /**
     * 优先级
     */
    public enum Priority {
        LOW,      // 低优先级
        NORMAL,   // 普通优先级
        HIGH,     // 高优先级
        URGENT    // 紧急
    }

    /**
     * 公告状态
     */
    public enum AnnouncementStatus {
        DRAFT,    // 草稿
        PUBLISHED, // 已发布
        EXPIRED,  // 已过期
        CANCELLED  // 已取消
    }

    /**
     * 目标受众
     */
    public enum TargetAudience {
        ALL,           // 全体用户
        ROLE_BASED,    // 基于角色
        DEPARTMENT,    // 基于部门（扩展用）
        SPECIFIC_USERS // 特定用户（扩展用）
    }

    /**
     * 显示类型
     */
    public enum DisplayType {
        NOTIFICATION,  // 通知中心
        POPUP,         // 弹窗显示
        BANNER,        // 横幅
        SIDEBAR        // 侧边栏
    }
}