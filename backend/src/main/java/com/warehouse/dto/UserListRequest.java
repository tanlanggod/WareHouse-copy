package com.warehouse.dto;

import com.warehouse.entity.User;
import com.warehouse.enums.UserRole;

/**
 * 用户列表查询请求对象
 */
public class UserListRequest {

    private Integer page = 1;              // 页码，默认第1页
    private Integer size = 10;             // 每页条数，默认10条
    private String keyword;                // 搜索关键词（用户名、真实姓名）
    private UserRole role;            // 角色筛选
    private Integer status;                // 状态筛选（1:启用，0:禁用）
    private String sortBy = "createdAt";   // 排序字段
    private String sortDirection = "desc"; // 排序方向（asc/desc）

    // Getters and Setters
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}