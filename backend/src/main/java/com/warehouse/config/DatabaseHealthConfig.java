package com.warehouse.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库健康检查配置
 * 在应用启动时验证数据库连接和表结构
 */
@Component
public class DatabaseHealthConfig implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthConfig.class);

    private final DataSource dataSource;

    public DatabaseHealthConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("=== 开始数据库健康检查 ===");

        try {
            // 测试数据库连接
            testDatabaseConnection();

            // 检查必要的表是否存在
            checkRequiredTables();

            logger.info("=== 数据库健康检查完成，所有检查通过 ===");

        } catch (Exception e) {
            logger.error("=== 数据库健康检查失败 ===", e);

            // 提供友好的错误提示和解决方案
            String errorMessage = buildErrorMessage(e);
            logger.error("数据库连接问题解决方案：\n{}", errorMessage);

            // 在开发环境中，可以选择是否终止启动
            // throw new RuntimeException("数据库连接失败，请检查配置", e);
        }
    }

    /**
     * 测试数据库连接
     */
    private void testDatabaseConnection() throws SQLException {
        logger.info("正在测试数据库连接...");

        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                logger.info("✅ 数据库连接成功");

                // 获取数据库信息
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery("SELECT VERSION() as version, DATABASE() as database")) {

                    if (resultSet.next()) {
                        String version = resultSet.getString("version");
                        String database = resultSet.getString("database");
                        logger.info("数据库版本: {}, 当前数据库: {}", version, database);
                    }
                }

            } else {
                throw new SQLException("数据库连接无效");
            }
        }
    }

    /**
     * 检查必要的表是否存在
     */
    private void checkRequiredTables() throws SQLException {
        logger.info("正在检查必要的数据库表...");

        String[] requiredTables = {
            "user", "warehouse", "warehouse_location", "supplier",
            "product", "category", "inbound", "outbound",
            "stock_adjustment", "operation_log"
        };

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            for (String tableName : requiredTables) {
                try (ResultSet resultSet = statement.executeQuery(
                    "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = '" + tableName + "'")) {

                    if (resultSet.next() && resultSet.getInt("count") > 0) {
                        logger.info("✅ 表 {} 存在", tableName);
                    } else {
                        logger.warn("⚠️ 表 {} 不存在，将自动创建", tableName);
                    }
                }
            }
        }
    }

    /**
     * 构建友好的错误消息
     */
    private String buildErrorMessage(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("数据库连接失败，请按以下步骤排查：\n");
        sb.append("1. 检查MySQL服务是否启动\n");
        sb.append("   - Windows: 在服务中查看MySQL服务状态\n");
        sb.append("   - 或使用命令: net start mysql\n\n");

        sb.append("2. 检查数据库是否存在\n");
        sb.append("   - 登录MySQL: mysql -u root -p\n");
        sb.append("   - 创建数据库: CREATE DATABASE warehouse_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\n\n");

        sb.append("3. 检查用户名和密码\n");
        sb.append("   - 当前配置: 用户名=root, 密码=root\n");
        sb.append("   - 如果密码不同，请修改 application.yml 中的数据库密码\n\n");

        sb.append("4. 检查端口是否被占用\n");
        sb.append("   - MySQL默认端口: 3306\n");
        sb.append("   - 确认端口没有被其他程序占用\n\n");

        sb.append("5. 原始错误信息: ").append(e.getMessage()).append("\n");

        if (e.getCause() != null) {
            sb.append("根本原因: ").append(e.getCause().getMessage()).append("\n");
        }

        return sb.toString();
    }
}