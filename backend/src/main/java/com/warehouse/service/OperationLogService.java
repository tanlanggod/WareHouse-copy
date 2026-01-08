package com.warehouse.service;

import com.warehouse.entity.OperationLog;
import com.warehouse.repository.OperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 操作日志服务类
 */
@Service
public class OperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogService.class);

    @Autowired
    private OperationLogRepository operationLogRepository;

    /**
     * 记录操作日志
     */
    public void logOperation(OperationLog operationLog) {
        try {
            // 如果没有请求ID，生成一个
            if (operationLog.getRequestId() == null || operationLog.getRequestId().isEmpty()) {
                operationLog.setRequestId(generateRequestId());
            }

            // 设置创建时间
            if (operationLog.getCreatedAt() == null) {
                operationLog.setCreatedAt(LocalDateTime.now());
            }

            operationLogRepository.save(operationLog);
            logger.debug("操作日志记录成功: {}", operationLog.getRequestId());
        } catch (Exception e) {
            logger.error("保存操作日志失败", e);
        }
    }

    /**
     * 记录HTTP请求日志
     */
    public void logHttpRequest(HttpServletRequest request,
                             Integer userId,
                             String username,
                             String userRole,
                             String operationType,
                             String resourceType,
                             String resourceId,
                             String operationDesc,
                             String module,
                             boolean isSuccess,
                             String errorMessage,
                             int responseStatus,
                             long responseTime,
                             String responseResult) {

        try {
            OperationLog log = new OperationLog();
            log.setRequestId(generateRequestId());
            log.setUserId(userId);
            log.setUsername(username);
            log.setUserRole(userRole);
            log.setIpAddress(getClientIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURL().toString());
            log.setRequestParams(getRequestParams(request));
            log.setResponseStatus(responseStatus);
            log.setResponseTime(responseTime);
            log.setResponseResult(responseResult);
            log.setOperationType(operationType);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setOperationDesc(operationDesc);
            log.setModule(module);
            log.setIsSuccess(isSuccess);
            log.setErrorMessage(errorMessage);

            operationLogRepository.save(log);
            logger.debug("HTTP请求日志记录成功: {}", log.getRequestId());
        } catch (Exception e) {
            logger.error("保存HTTP请求日志失败", e);
        }
    }

    /**
     * 分页查询操作日志
     */
    public Page<OperationLog> findOperationLogs(Pageable pageable) {
        return operationLogRepository.findAll(pageable);
    }

    /**
     * 根据用户ID查询操作日志
     */
    public Page<OperationLog> findByUserId(Integer userId, Pageable pageable) {
        return operationLogRepository.findByUserId(userId, pageable);
    }

    /**
     * 根据多个条件查询操作日志
     */
    public Page<OperationLog> findByMultipleConditions(String username,
                                                      String operationType,
                                                      String resourceType,
                                                      String module,
                                                      Boolean isSuccess,
                                                      String ipAddress,
                                                      LocalDateTime startTime,
                                                      LocalDateTime endTime,
                                                      Pageable pageable) {
        return operationLogRepository.findByMultipleConditions(
                username, operationType, resourceType, module,
                isSuccess, ipAddress, startTime, endTime, pageable
        );
    }

    /**
     * 根据请求ID查询操作日志
     */
    public OperationLog findByRequestId(String requestId) {
        return operationLogRepository.findByRequestId(requestId);
    }

    /**
     * 获取操作统计数据
     */
    public Map<String, Object> getOperationStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 统计总操作次数
            Long totalCount = operationLogRepository.countAll(startTime, endTime);

            // 统计各操作类型数量
            List<Object[]> operationTypeStats = operationLogRepository.countByOperationType(startTime, endTime);
            Map<String, Long> operationTypeCount = new HashMap<>();
            for (Object[] stat : operationTypeStats) {
                String key = stat[0] != null ? (String) stat[0] : "未知操作";
                Long value = (Long) stat[1];
                operationTypeCount.put(key, value);
            }

            // 统计各资源类型数量
            List<Object[]> resourceTypeStats = operationLogRepository.countByResourceType(startTime, endTime);
            Map<String, Long> resourceTypeCount = new HashMap<>();
            for (Object[] stat : resourceTypeStats) {
                String key = stat[0] != null ? (String) stat[0] : "未知资源";
                Long value = (Long) stat[1];
                resourceTypeCount.put(key, value);
            }

            // 统计用户操作数量
            List<Object[]> userStats = operationLogRepository.countByUsername(startTime, endTime);
            Map<String, Long> userCount = new HashMap<>();
            for (Object[] stat : userStats) {
                String key = stat[0] != null ? (String) stat[0] : "未知用户";
                Long value = (Long) stat[1];
                userCount.put(key, value);
            }

            // 统计成功失败数量
            List<Object[]> successStats = operationLogRepository.countBySuccess(startTime, endTime);
            Map<Boolean, Long> successCount = new HashMap<>();
            for (Object[] stat : successStats) {
                Boolean key = stat[0] != null ? (Boolean) stat[0] : false;
                Long value = (Long) stat[1];
                successCount.put(key, value);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("operationTypeCount", operationTypeCount);
            result.put("resourceTypeCount", resourceTypeCount);
            result.put("userCount", userCount);
            result.put("successCount", successCount);
            return result;
        } catch (Exception e) {
            logger.error("获取操作统计数据失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 查询响应时间超过阈值的操作
     */
    public Page<OperationLog> findSlowOperations(Long responseTimeThreshold, Pageable pageable) {
        return operationLogRepository.findByResponseTimeGreaterThan(responseTimeThreshold, pageable);
    }

    /**
     * 查询错误操作日志
     */
    public Page<OperationLog> findErrorOperations(Pageable pageable) {
        return operationLogRepository.findWithError(pageable);
    }

    /**
     * 查询最近的操作日志
     */
    public List<OperationLog> findRecentOperations(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return operationLogRepository.findRecentLogs(pageable);
    }

    /**
     * 清理指定日期之前的日志
     */
    public void cleanOldLogs(LocalDateTime beforeDate) {
        try {
            operationLogRepository.deleteByCreatedAtBefore(beforeDate);
            logger.info("已清理{}之前的操作日志", beforeDate);
        } catch (Exception e) {
            logger.error("清理旧日志失败", e);
        }
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(HttpServletRequest request) {
        try {
            StringBuilder params = new StringBuilder();
            request.getParameterMap().forEach((key, values) -> {
                if (params.length() > 0) {
                    params.append("&");
                }
                params.append(key).append("=");
                if (values.length > 0) {
                    // 对于密码等敏感参数进行脱敏处理
                    if (key.toLowerCase().contains("password") ||
                        key.toLowerCase().contains("pwd") ||
                        key.toLowerCase().contains("token")) {
                        params.append("***");
                    } else {
                        params.append(String.join(",", values));
                    }
                }
            });
            return params.toString();
        } catch (Exception e) {
            logger.error("获取请求参数失败", e);
            return "";
        }
    }

    /**
     * 根据URL路径推断操作类型和资源类型
     */
    public Map<String, String> inferOperationAndResource(String requestUrl, String requestMethod) {
        String operationType = "READ";
        String resourceType = "SYSTEM";
        String module = "SYSTEM";

        // 根据HTTP方法推断操作类型
        switch (requestMethod.toUpperCase()) {
            case "POST":
                operationType = "CREATE";
                break;
            case "PUT":
            case "PATCH":
                operationType = "UPDATE";
                break;
            case "DELETE":
                operationType = "DELETE";
                break;
            case "GET":
                operationType = "READ";
                break;
            default:
                operationType = "OTHER";
        }

        // 根据URL路径推断资源类型和模块
        String lowerUrl = requestUrl.toLowerCase();
        if (lowerUrl.contains("/product")) {
            resourceType = "PRODUCT";
            module = "PRODUCT";
        } else if (lowerUrl.contains("/category")) {
            resourceType = "CATEGORY";
            module = "PRODUCT";
        } else if (lowerUrl.contains("/supplier")) {
            resourceType = "SUPPLIER";
            module = "PRODUCT";
        } else if (lowerUrl.contains("/customer")) {
            resourceType = "CUSTOMER";
            module = "PRODUCT";
        } else if (lowerUrl.contains("/inbound")) {
            resourceType = "INBOUND";
            module = "INVENTORY";
        } else if (lowerUrl.contains("/outbound")) {
            resourceType = "OUTBOUND";
            module = "INVENTORY";
        } else if (lowerUrl.contains("/stock")) {
            resourceType = "STOCK";
            module = "INVENTORY";
        } else if (lowerUrl.contains("/report")) {
            resourceType = "REPORT";
            module = "REPORT";
            if (lowerUrl.contains("/export") || lowerUrl.contains("/pdf")) {
                operationType = "EXPORT";
            }
        } else if (lowerUrl.contains("/user")) {
            resourceType = "USER";
            module = "SYSTEM";
        } else if (lowerUrl.contains("/auth") || lowerUrl.contains("/login")) {
            resourceType = "USER";
            module = "AUTH";
            operationType = "LOGIN";
        } else if (lowerUrl.contains("/upload")) {
            operationType = "IMPORT";
            resourceType = "FILE";
            module = "SYSTEM";
        } else if (lowerUrl.contains("/download")) {
            operationType = "DOWNLOAD";
            resourceType = "FILE";
            module = "SYSTEM";
        }

        Map<String, String> result = new HashMap<>();
            result.put("operationType", operationType);
            result.put("resourceType", resourceType);
            result.put("module", module);
            return result;
    }
}