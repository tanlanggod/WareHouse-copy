package com.warehouse.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.entity.User;
import com.warehouse.service.OperationLogService;
import com.warehouse.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 操作日志拦截器
 */
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogInterceptor.class);

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 不需要记录日志的路径
    private final List<String> excludePaths = Arrays.asList(
            "/api/actuator",
            "/api/health",
            "/api/info",
            "/api/metrics",
            "/favicon.ico",
            "/error",
            "/static",
            "/css",
            "/js",
            "/images",
            "/fonts"
    );

    // 不需要记录日志的文件扩展名
    private final List<String> excludeExtensions = Arrays.asList(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".svg", ".woff", ".woff2", ".ttf"
    );

    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static final ThreadLocal<String> requestId = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        startTime.set(System.currentTimeMillis());

        // 生成请求ID
        requestId.set(java.util.UUID.randomUUID().toString().replace("-", ""));

        // 检查是否需要记录日志
        if (shouldSkipLogging(request)) {
            return true;
        }

        logger.debug("开始记录请求日志: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 在这里可以添加一些后处理逻辑
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 检查是否需要记录日志
            if (shouldSkipLogging(request)) {
                return;
            }

            // 计算响应时间
            Long responseTime = startTime.get();
            if (responseTime != null) {
                responseTime = System.currentTimeMillis() - responseTime;
            }

            // 获取用户信息
            User currentUser = getCurrentUser(request);
            Integer userId = null;
            String username = null;
            String userRole = null;

            if (currentUser != null) {
                userId = currentUser.getId();
                username = currentUser.getUsername();
                userRole = currentUser.getRole() != null ? currentUser.getRole().toString() : null;
            }

            // 推断操作类型和资源类型
            Map<String, String> operationInfo = operationLogService.inferOperationAndResource(
                    request.getRequestURL().toString(), request.getMethod());
            String operationType = operationInfo.get("operationType");
            String resourceType = operationInfo.get("resourceType");
            String module = operationInfo.get("module");

            // 判断操作是否成功
            boolean isSuccess = response.getStatus() < 400;
            String errorMessage = null;

            if (ex != null) {
                isSuccess = false;
                errorMessage = ex.getMessage();
            } else if (response.getStatus() >= 400) {
                isSuccess = false;
                errorMessage = "HTTP Status: " + response.getStatus();
            }

            // 生成操作描述
            String operationDesc = generateOperationDescription(request, operationType, resourceType);

            // 获取响应结果摘要
            String responseResult = generateResponseResult(response, isSuccess);

            // 记录操作日志
            operationLogService.logHttpRequest(
                    request,
                    userId,
                    username,
                    userRole,
                    operationType,
                    resourceType,
                    null, // resourceId 可以在具体的Controller中设置
                    operationDesc,
                    module,
                    isSuccess,
                    errorMessage,
                    response.getStatus(),
                    responseTime != null ? responseTime : 0L,
                    responseResult
            );

            logger.debug("请求日志记录完成: {} {} ({}ms)", request.getMethod(), request.getRequestURI(), responseTime);

        } catch (Exception e) {
            logger.error("记录操作日志失败", e);
        } finally {
            // 清理ThreadLocal
            startTime.remove();
            requestId.remove();
        }
    }

    /**
     * 检查是否应该跳过日志记录
     */
    private boolean shouldSkipLogging(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // 检查排除路径
        for (String excludePath : excludePaths) {
            if (requestURI.contains(excludePath)) {
                return true;
            }
        }

        // 检查排除文件扩展名
        for (String excludeExt : excludeExtensions) {
            if (requestURI.endsWith(excludeExt)) {
                return true;
            }
        }

        // 只记录API请求
        if (!requestURI.startsWith("/api/")) {
            return true;
        }

        return false;
    }

    /**
     * 获取当前用户信息
     */
    private User getCurrentUser(HttpServletRequest request) {
        try {
            // 首先尝试从SecurityContext获取
            org.springframework.security.core.Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }

            // 如果SecurityContext中没有，尝试从JWT Token获取
            String token = extractJwtFromRequest(request);
            if (token != null && !token.isEmpty()) {
                String username = jwtUtil.getUsernameFromToken(token);
                // 这里可以进一步获取完整的用户信息
                // 为了简化，这里只返回用户名
                User user = new User();
                user.setUsername(username);
                return user;
            }

        } catch (Exception e) {
            logger.debug("获取用户信息失败", e);
        }

        return null;
    }

    /**
     * 从请求中提取JWT Token
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 生成操作描述
     */
    private String generateOperationDescription(HttpServletRequest request, String operationType, String resourceType) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        String action = "";
        switch (operationType) {
            case "CREATE":
                action = "创建";
                break;
            case "READ":
                action = "查询";
                break;
            case "UPDATE":
                action = "更新";
                break;
            case "DELETE":
                action = "删除";
                break;
            case "EXPORT":
                action = "导出";
                break;
            case "IMPORT":
                action = "导入";
                break;
            case "DOWNLOAD":
                action = "下载";
                break;
            case "LOGIN":
                action = "登录";
                break;
            case "LOGOUT":
                action = "登出";
                break;
            default:
                action = "操作";
        }

        String resource = "";
        switch (resourceType) {
            case "PRODUCT":
                resource = "商品";
                break;
            case "CATEGORY":
                resource = "分类";
                break;
            case "SUPPLIER":
                resource = "供应商";
                break;
            case "CUSTOMER":
                resource = "客户";
                break;
            case "INBOUND":
                resource = "入库";
                break;
            case "OUTBOUND":
                resource = "出库";
                break;
            case "STOCK":
                resource = "库存";
                break;
            case "REPORT":
                resource = "报表";
                break;
            case "USER":
                resource = "用户";
                break;
            default:
                resource = "系统";
        }

        return action + resource + " - " + method + " " + uri;
    }

    /**
     * 生成响应结果摘要
     */
    private String generateResponseResult(HttpServletResponse response, boolean isSuccess) {
        if (isSuccess) {
            return "操作成功";
        } else {
            return "操作失败 - HTTP Status: " + response.getStatus();
        }
    }

    /**
     * 获取当前请求ID
     */
    public static String getCurrentRequestId() {
        return requestId.get();
    }

    /**
     * 获取当前请求开始时间
     */
    public static Long getCurrentStartTime() {
        return startTime.get();
    }
}