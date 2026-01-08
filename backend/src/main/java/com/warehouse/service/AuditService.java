package com.warehouse.service;

import com.warehouse.entity.BaseAuditEntity;
import com.warehouse.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 审计服务
 * 统一处理实体的审计字段设置
 */
@Service
public class AuditService {

    /**
     * 为新实体设置审计字段
     */
    @Transactional
    public void setCreateInfo(BaseAuditEntity entity) {
        if (entity != null) {
            Integer currentUserId = getCurrentUserId();
            entity.setCreatorId(currentUserId);
            entity.setUpdaterId(currentUserId);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * 为更新实体设置审计字段
     */
    @Transactional
    public void setUpdateInfo(BaseAuditEntity entity) {
        if (entity != null) {
            Integer currentUserId = getCurrentUserId();
            entity.setUpdaterId(currentUserId);
            entity.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * 执行逻辑删除
     */
    @Transactional
    public void softDelete(BaseAuditEntity entity) {
        if (entity != null) {
            Integer currentUserId = getCurrentUserId();
            entity.softDelete(currentUserId);
        }
    }

    /**
     * 恢复删除状态
     */
    @Transactional
    public void restore(BaseAuditEntity entity) {
        if (entity != null) {
            entity.restore();
        }
    }

    /**
     * 获取当前用户ID
     * 这里需要根据你的认证框架来实现
     */
    private Integer getCurrentUserId() {
        // 从Spring Security上下文获取当前用户
        // 这里需要根据你的具体实现来调整
        return UserContext.getCurrentUserId();
    }

    /**
     * 检查实体是否有权限操作
     */
    public boolean canOperate(BaseAuditEntity entity) {
        if (entity == null) {
            return false;
        }

        // 检查是否已删除
        if (entity.isDeleted()) {
            return false;
        }

        // 这里可以添加更多的权限检查逻辑
        // 例如：检查创建人、角色权限等

        return true;
    }

    /**
     * 检查实体是否有权限删除
     */
    public boolean canDelete(BaseAuditEntity entity) {
        if (!canOperate(entity)) {
            return false;
        }

        // 这里可以添加特定的删除权限检查
        // 例如：只有管理员或创建人可以删除

        return true;
    }
}