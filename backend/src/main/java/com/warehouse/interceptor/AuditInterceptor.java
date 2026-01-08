package com.warehouse.interceptor;

import com.warehouse.entity.BaseAuditEntity;
import com.warehouse.util.UserContext;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Hibernate审计拦截器
 * 自动设置实体的审计字段
 */
@Component
public class AuditInterceptor extends EmptyInterceptor {

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseAuditEntity) {
            BaseAuditEntity auditEntity = (BaseAuditEntity) entity;

            Integer currentUserId = getCurrentUserId();
            LocalDateTime now = LocalDateTime.now();

            // 设置创建人、创建时间、修改人、修改时间
            for (int i = 0; i < propertyNames.length; i++) {
                if ("creatorId".equals(propertyNames[i]) && auditEntity.getCreatorId() == null) {
                    state[i] = currentUserId;
                } else if ("createdAt".equals(propertyNames[i]) && auditEntity.getCreatedAt() == null) {
                    state[i] = now;
                } else if ("updaterId".equals(propertyNames[i])) {
                    state[i] = currentUserId;
                } else if ("updatedAt".equals(propertyNames[i])) {
                    state[i] = now;
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (entity instanceof BaseAuditEntity) {
            BaseAuditEntity auditEntity = (BaseAuditEntity) entity;

            Integer currentUserId = getCurrentUserId();
            LocalDateTime now = LocalDateTime.now();

            // 设置修改人、修改时间
            for (int i = 0; i < propertyNames.length; i++) {
                if ("updaterId".equals(propertyNames[i])) {
                    currentState[i] = currentUserId;
                } else if ("updatedAt".equals(propertyNames[i])) {
                    currentState[i] = now;
                }
            }

            return true;
        }
        return false;
    }

    /**
     * 获取当前用户ID
     */
    private Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            // 如果无法获取用户信息，返回系统用户ID
            return 1; // 系统用户ID
        }
    }
}