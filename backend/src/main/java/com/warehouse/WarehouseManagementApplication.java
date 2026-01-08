package com.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系统入口类，负责启动 Spring Boot 应用。
 * JPA审计功能在JpaAuditConfig中配置。
 */
@SpringBootApplication
public class WarehouseManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseManagementApplication.class, args);
    }
}

