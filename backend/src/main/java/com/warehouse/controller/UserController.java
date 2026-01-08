package com.warehouse.controller;

import com.warehouse.common.Result;
import com.warehouse.dto.PasswordResetRequest;
import com.warehouse.dto.UserCreateRequest;
import com.warehouse.dto.UserListRequest;
import com.warehouse.entity.User;
import com.warehouse.enums.UserRole;
import com.warehouse.util.UserContext;
import com.warehouse.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        logger.info("=== 开始获取当前用户信息 ===");

        try {
            // 打印认证信息
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                logger.info("当前认证用户: {}, 权限: {}",
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            } else {
                logger.warn("SecurityContext中没有认证信息");
            }

            logger.info("调用UserService.getCurrentUser()");
            long startTime = System.currentTimeMillis();

            User user = userService.getCurrentUser();

            long endTime = System.currentTimeMillis();
            logger.info("用户信息获取完成，耗时: {}ms, 用户: {}, 状态: {}",
                endTime - startTime,
                user != null ? user.getUsername() : "null",
                user != null ? user.getStatus() : "null");

            return Result.success(user);
        } catch (Exception e) {
            logger.error("获取当前用户信息失败", e);
            logger.error("异常类型: {}, 异常消息: {}",
                e.getClass().getSimpleName(),
                e.getMessage());

            // 检查是否是认证相关异常
            if (e.getCause() != null) {
                logger.error("根本原因异常: {}", e.getCause().getMessage());
            }

            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/current")
    public Result<User> updateCurrentUser(@RequestBody User userInfo) {
        try {
            User updatedUser = userService.updateCurrentUser(userInfo);
            return Result.success(updatedUser);
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/current/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("请上传图片文件");
            }

            // 检查文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("图片大小不能超过5MB");
            }

            String avatarUrl = userService.uploadAvatar(file);
            return Result.success("头像上传成功", avatarUrl);
        } catch (Exception e) {
            logger.error("上传头像失败", e);
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }

    /**
     * 设置当前用户上下文
     */
    @PostMapping("/current/context")
    @PreAuthorize("isAuthenticated()")
    public Result<String> setUserContext(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            if (userId != null) {
                UserContext.setCurrentUserId(userId);
                return Result.success("用户上下文设置成功");
            } else {
                return Result.error("用户ID不能为空");
            }
        } catch (Exception e) {
            logger.error("设置用户上下文失败", e);
            return Result.error("设置用户上下文失败: " + e.getMessage());
        }
    }

    /**
     * 清除当前用户上下文
     */
    @DeleteMapping("/current/context")
    @PreAuthorize("isAuthenticated()")
    public Result<String> clearUserContext() {
        try {
            UserContext.clear();
            return Result.success("用户上下文清除成功");
        } catch (Exception e) {
            logger.error("清除用户上下文失败", e);
            return Result.error("清除用户上下文失败: " + e.getMessage());
        }
    }

    
    /**
     * 修改密码
     */
    @PutMapping("/current/password")
    public Result<String> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return Result.error("请提供原密码和新密码");
            }

            if (newPassword.length() < 6) {
                return Result.error("新密码长度不能少于6位");
            }

            userService.changePassword(oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            logger.error("修改密码失败", e);
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取用户信息 (管理员功能)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            return Result.success(user);
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息 (管理员功能)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Integer id, @RequestBody User userInfo) {
        try {
            User updatedUser = userService.updateUser(id, userInfo);
            return Result.success(updatedUser);
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    // ========== 管理员用户管理功能 ==========

    /**
     * 获取用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            UserListRequest request = new UserListRequest();
            request.setPage(page);
            request.setSize(size);
            request.setKeyword(keyword);
            if (role != null && !role.isEmpty()) {
                request.setRole(UserRole.valueOf(role));
            }
            request.setStatus(status);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            return Result.success(userService.getUsers(request));
        } catch (SecurityException e) {
            logger.warn("用户尝试访问无权限的资源: {}", e.getMessage());
            return Result.error("无权限访问用户列表");
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@Validated @RequestBody UserCreateRequest request) {
        try {
            User user = userService.createUser(request);
            // 不返回密码信息
            user.setPassword(null);
            return Result.success("用户创建成功", user);
        } catch (Exception e) {
            logger.error("创建用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> resetPassword(
            @PathVariable Integer id,
            @RequestBody(required = false) PasswordResetRequest request) {
        try {
            // 如果没有提供请求体，创建一个默认的
            if (request == null) {
                request = new PasswordResetRequest();
            }

            String newPassword = userService.resetUserPassword(id, request);
            return Result.success("密码重置成功", newPassword);
        } catch (Exception e) {
            logger.error("重置密码失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 切换用户状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> toggleUserStatus(@PathVariable Integer id) {
        try {
            User user = userService.toggleUserStatus(id);
            // 不返回密码信息
            user.setPassword(null);
            return Result.success("用户状态切换成功", user);
        } catch (Exception e) {
            logger.error("切换用户状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return Result.success("用户删除成功", null);
        } catch (Exception e) {
            logger.error("删除用户失败", e);
            return Result.error(e.getMessage());
        }
    }
}