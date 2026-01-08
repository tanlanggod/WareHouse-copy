package com.warehouse.util;

/**
 * 用户上下文
 * 用于获取当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<Integer> currentUserId = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Integer userId) {
        currentUserId.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Integer getCurrentUserId() {
        Integer userId = currentUserId.get();
        if (userId == null) {
            // 如果没有设置用户ID，返回null表示未登录
            return null;
        }
        return userId;
    }

    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        currentUserId.remove();
    }
}