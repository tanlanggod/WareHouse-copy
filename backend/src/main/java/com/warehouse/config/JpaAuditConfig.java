package com.warehouse.config;

import com.warehouse.util.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA审计配置
 * 启用JPA审计功能，自动设置创建人、修改人等审计字段
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    /**
     * 审计信息提供者
     * 负责提供当前操作用户信息
     */
    @Bean
    public AuditorAware<Integer> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * 审计信息提供者实现类
     */
    public static class AuditorAwareImpl implements AuditorAware<Integer> {

        @Override
        public Optional<Integer> getCurrentAuditor() {
            // 从Spring Security上下文获取当前用户
            // 这里需要根据你的具体实现来调整
            try {
                Integer currentUserId = UserContext.getCurrentUserId();
                return Optional.ofNullable(currentUserId);
            } catch (Exception e) {
                // 如果无法获取用户信息，返回系统用户ID
                return Optional.of(1); // 系统用户ID
            }
        }
    }
}