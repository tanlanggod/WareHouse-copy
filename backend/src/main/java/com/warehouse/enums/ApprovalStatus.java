package com.warehouse.enums;

/**
 * 审批状态枚举
 */
public enum ApprovalStatus {
    DRAFT("草稿"),
    PENDING("待审批"),
    APPROVED("已批准"),
    REJECTED("已拒绝"),
    CANCELLED("已取消");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ApprovalStatus fromString(String status) {
        if (status == null) {
            return DRAFT;
        }
        try {
            return ApprovalStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DRAFT;
        }
    }
}