package com.warehouse.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 数据库初始化工具类
 * 提供快速创建数据库和表的功能
 */
public class DatabaseInitUtil {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitUtil.class);

    // 数据库配置
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 3306;
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";
    private static final String DB_NAME = "warehouse_management";

    /**
     * 检查MySQL连接并创建数据库
     */
    public static void initializeDatabase() {
        try {
            logger.info("=== 开始数据库初始化 ===");

            // 1. 测试MySQL连接
            if (!testMySQLConnection()) {
                logger.error("MySQL连接失败，请检查MySQL服务是否启动");
                return;
            }

            // 2. 创建数据库
            if (!createDatabase()) {
                logger.error("数据库创建失败");
                return;
            }

            // 3. 验证数据库
            if (!verifyDatabase()) {
                logger.error("数据库验证失败");
                return;
            }

            logger.info("=== 数据库初始化成功 ===");

        } catch (Exception e) {
            logger.error("数据库初始化失败", e);
        }
    }

    /**
     * 测试MySQL连接
     */
    private static boolean testMySQLConnection() {
        logger.info("正在测试MySQL连接...");

        try {
            String url = String.format("jdbc:mysql://%s:%d?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                DB_HOST, DB_PORT);

            try (Connection connection = DriverManager.getConnection(url, DB_USERNAME, DB_PASSWORD)) {
                if (connection.isValid(5)) {
                    logger.info("✅ MySQL连接成功");

                    // 获取MySQL版本
                    try (java.sql.Statement statement = connection.createStatement();
                         java.sql.ResultSet resultSet = statement.executeQuery("SELECT VERSION()")) {

                        if (resultSet.next()) {
                            String version = resultSet.getString(1);
                            logger.info("MySQL版本: {}", version);
                        }
                    }

                    return true;
                }
            }

        } catch (Exception e) {
            logger.error("MySQL连接失败: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 创建数据库
     */
    private static boolean createDatabase() {
        logger.info("正在创建数据库: {}", DB_NAME);

        try {
            String url = String.format("jdbc:mysql://%s:%d?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                DB_HOST, DB_PORT);

            try (Connection connection = DriverManager.getConnection(url, DB_USERNAME, DB_PASSWORD);
                 Statement statement = connection.createStatement()) {

                // 创建数据库（如果不存在）
                String createDbSql = String.format(
                    "CREATE DATABASE IF NOT EXISTS %s CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                    DB_NAME
                );

                int result = statement.executeUpdate(createDbSql);

                if (result >= 0 || checkDatabaseExists()) {
                    logger.info("✅ 数据库 {} 创建成功或已存在", DB_NAME);
                    return true;
                }

            }

        } catch (Exception e) {
            logger.error("创建数据库失败: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 检查数据库是否存在
     */
    private static boolean checkDatabaseExists() {
        try {
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                DB_HOST, DB_PORT, DB_NAME);

            try (Connection connection = DriverManager.getConnection(url, DB_USERNAME, DB_PASSWORD)) {
                return connection.isValid(5);
            }

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证数据库
     */
    private static boolean verifyDatabase() {
        logger.info("正在验证数据库...");

        try {
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                DB_HOST, DB_PORT, DB_NAME);

            try (Connection connection = DriverManager.getConnection(url, DB_USERNAME, DB_PASSWORD);
                 Statement statement = connection.createStatement()) {

                // 测试简单查询
                try (java.sql.ResultSet resultSet = statement.executeQuery("SELECT DATABASE() as current_db")) {
                    if (resultSet.next()) {
                        String currentDb = resultSet.getString("current_db");
                        logger.info("✅ 数据库验证成功，当前数据库: {}", currentDb);
                        return DB_NAME.equals(currentDb);
                    }
                }

            }

        } catch (Exception e) {
            logger.error("数据库验证失败: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 生成SQL命令用于手动执行
     */
    public static String generateSQLCommands() {
        StringBuilder sb = new StringBuilder();
        sb.append("-- 仓库管理系统数据库初始化脚本\n");
        sb.append("-- 请在MySQL命令行中执行以下命令\n\n");

        sb.append("-- 1. 创建数据库\n");
        sb.append("CREATE DATABASE IF NOT EXISTS warehouse_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\n\n");

        sb.append("-- 2. 使用数据库\n");
        sb.append("USE warehouse_management;\n\n");

        sb.append("-- 3. 验证数据库\n");
        sb.append("SELECT DATABASE() as current_database;\n\n");

        sb.append("-- 4. 查看所有表（应用启动后会自动创建）\n");
        sb.append("SHOW TABLES;\n");

        return sb.toString();
    }

    /**
     * 打印系统信息
     */
    public static void printSystemInfo() {
        logger.info("=== 数据库连接信息 ===");
        logger.info("主机: {}:{}", DB_HOST, DB_PORT);
        logger.info("用户名: {}", DB_USERNAME);
        logger.info("数据库名: {}", DB_NAME);
        logger.info("======================");
    }

    /**
     * 主方法 - 可以单独运行进行数据库初始化
     */
    public static void main(String[] args) {
        logger.info("仓库管理系统 - 数据库初始化工具");
        printSystemInfo();

        System.out.println("\n" + generateSQLCommands());

        // 尝试自动初始化
        initializeDatabase();
    }
}