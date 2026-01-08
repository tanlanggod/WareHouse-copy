package com.warehouse.controller;

import com.warehouse.entity.OperationLog;
import com.warehouse.service.OperationLogService;
import com.warehouse.repository.OperationLogRepository;
import com.warehouse.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作日志管理控制器
 */
@RestController
@RequestMapping("/api/operation-logs")
// @PreAuthorize("hasRole('ADMIN')") // 暂时注释权限注解，待测试完成后启用
public class OperationLogController {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogController.class);

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private OperationLogRepository operationLogRepository;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public Result<Page<OperationLog>> getOperationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Boolean isSuccess,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        try {
            // 创建分页和排序参数
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            // 执行查询
            Page<OperationLog> logs = operationLogService.findByMultipleConditions(
                    username, operationType, resourceType, module,
                    isSuccess, ipAddress, startTime, endTime, pageable);

            return Result.success(logs);
        } catch (Exception e) {
            logger.error("查询操作日志失败", e);
            return Result.error("查询操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询操作日志详情
     */
    @GetMapping("/{id}")
    public Result<OperationLog> getOperationLogById(@PathVariable Integer id) {
        try {
            return Result.success(operationLogRepository.findById(id).orElse(null));
        } catch (Exception e) {
            logger.error("查询操作日志详情失败", e);
            return Result.error("查询操作日志详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据请求ID查询操作日志
     */
    @GetMapping("/request/{requestId}")
    public Result<OperationLog> getOperationLogByRequestId(@PathVariable String requestId) {
        try {
            OperationLog log = operationLogService.findByRequestId(requestId);
            if (log == null) {
                return Result.error("未找到对应的操作日志");
            }
            return Result.success(log);
        } catch (Exception e) {
            logger.error("根据请求ID查询操作日志失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOperationStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        try {
            // 如果没有指定时间范围，默认查询最近30天
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }

            Map<String, Object> statistics = operationLogService.getOperationStatistics(startTime, endTime);
            return Result.success(statistics);
        } catch (Exception e) {
            logger.error("获取操作统计数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 查询响应时间超过阈值的操作
     */
    @GetMapping("/slow-operations")
    public Result<Page<OperationLog>> getSlowOperations(
            @RequestParam(defaultValue = "5000") Long responseTimeThreshold, // 默认5秒
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "responseTime"));
            Page<OperationLog> logs = operationLogService.findSlowOperations(responseTimeThreshold, pageable);
            return Result.success(logs);
        } catch (Exception e) {
            logger.error("查询慢操作失败", e);
            return Result.error("查询慢操作失败: " + e.getMessage());
        }
    }

    /**
     * 查询错误操作日志
     */
    @GetMapping("/errors")
    public Result<Page<OperationLog>> getErrorOperations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<OperationLog> logs = operationLogService.findErrorOperations(pageable);
            return Result.success(logs);
        } catch (Exception e) {
            logger.error("查询错误操作日志失败", e);
            return Result.error("查询错误操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 查询最近的操作日志
     */
    @GetMapping("/recent")
    public Result<List<OperationLog>> getRecentOperations(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<OperationLog> logs = operationLogService.findRecentOperations(limit);
            return Result.success(logs);
        } catch (Exception e) {
            logger.error("查询最近操作日志失败", e);
            return Result.error("查询最近操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询操作日志
     */
    @GetMapping("/user/{userId}")
    public Result<Page<OperationLog>> getOperationLogsByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<OperationLog> logs = operationLogService.findByUserId(userId, pageable);
            return Result.success(logs);
        } catch (Exception e) {
            logger.error("根据用户ID查询操作日志失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 清理指定日期之前的日志
     */
    @DeleteMapping("/cleanup")
    public Result<String> cleanupOldLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) {

        try {
            operationLogService.cleanOldLogs(beforeDate);
            return Result.success("日志清理完成");
        } catch (Exception e) {
            logger.error("清理日志失败", e);
            return Result.error("清理日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作类型列表
     */
    @GetMapping("/operation-types")
    public Result<String[]> getOperationTypes() {
        String[] operationTypes = {
                OperationLog.OperationType.CREATE,
                OperationLog.OperationType.READ,
                OperationLog.OperationType.UPDATE,
                OperationLog.OperationType.DELETE,
                OperationLog.OperationType.EXPORT,
                OperationLog.OperationType.IMPORT,
                OperationLog.OperationType.LOGIN,
                OperationLog.OperationType.LOGOUT,
                OperationLog.OperationType.DOWNLOAD
        };
        return Result.success(operationTypes);
    }

    /**
     * 获取资源类型列表
     */
    @GetMapping("/resource-types")
    public Result<String[]> getResourceTypes() {
        String[] resourceTypes = {
                OperationLog.ResourceType.PRODUCT,
                OperationLog.ResourceType.CATEGORY,
                OperationLog.ResourceType.SUPPLIER,
                OperationLog.ResourceType.CUSTOMER,
                OperationLog.ResourceType.INBOUND,
                OperationLog.ResourceType.OUTBOUND,
                OperationLog.ResourceType.USER,
                OperationLog.ResourceType.REPORT,
                OperationLog.ResourceType.SYSTEM
        };
        return Result.success(resourceTypes);
    }

    /**
     * 获取模块列表
     */
    @GetMapping("/modules")
    public Result<String[]> getModules() {
        String[] modules = {
                "PRODUCT",    // 商品管理
                "INVENTORY",  // 库存管理
                "REPORT",     // 报表管理
                "SYSTEM",     // 系统管理
                "AUTH"        // 认证相关
        };
        return Result.success(modules);
    }
}