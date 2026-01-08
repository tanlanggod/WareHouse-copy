package com.warehouse.exception;

import com.warehouse.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

/**
 * 仓库位置异常处理辅助类
 */
public class WarehouseLocationExceptionHelper {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseLocationExceptionHelper.class);

    /**
     * 处理仓库位置相关的数据完整性异常
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<T> handleDataIntegrityViolationException(DataIntegrityViolationException e, String operation) {
        logger.error("=== 数据库完整性约束违反 ===");
        logger.error("操作: {}, 异常消息: {}", operation, e.getMessage());
        logger.error("================================");

        // 检查是否是唯一约束冲突
        if (e.getMessage() != null && e.getMessage().contains("uk_location")) {
            return Result.error("位置编码已存在：同一仓库内货架号、层号、货位号的组合必须唯一");
        }
        return Result.error("数据冲突，请检查是否重复");
    }

    /**
     * 处理仓库位置相关的事务系统异常
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<T> handleTransactionSystemException(TransactionSystemException e, String operation) {
        Throwable rootCause = e.getRootCause();
        logger.error("=== 事务系统错误 ===");
        logger.error("操作: {}, 根本原因: {}", operation, rootCause);
        logger.error("================================");

        // 检查根本原因是否是约束冲突
        if (rootCause != null && rootCause.getMessage() != null
            && rootCause.getMessage().contains("uk_location")) {
            return Result.error("位置编码已存在：同一仓库内货架号、层号、货位号的组合必须唯一");
        }
        return Result.error("事务处理失败: " + (rootCause != null ? rootCause.getMessage() : e.getMessage()));
    }

    /**
     * 处理仓库位置相关的通用异常
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<T> handleGeneralException(Exception e, String operation, Object context) {
        logger.error("=== {}异常详细信息 ===", operation);
        logger.error("异常类型: {}", e.getClass().getSimpleName());
        logger.error("异常消息: {}", e.getMessage());
        logger.error("上下文信息: {}", context);
        logger.error("================================");

        // 检查是否是约束冲突
        if (e.getMessage() != null && e.getMessage().contains("uk_location")) {
            return Result.error("位置编码已存在：同一仓库内货架号、层号、货位号的组合必须唯一");
        }
        return Result.error(operation + "失败：" + e.getMessage());
    }

    /**
     * 记录成功操作日志
     */
    public static void logSuccess(Logger logger, String operation, Object details) {
        logger.info("{}成功: {}", operation, details);
    }

    /**
     * 记录操作开始日志
     */
    public static void logOperationStart(Logger logger, String operation, Object details) {
        logger.info("开始{}: {}", operation, details);
    }
}