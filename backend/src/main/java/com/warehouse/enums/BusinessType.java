package com.warehouse.enums;

/**
 * 业务类型枚举
 */
public enum BusinessType {
    INBOUND("入库"),
    OUTBOUND("出库"),
    STOCK_ADJUSTMENT("库存调整");

    private final String description;

    BusinessType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static BusinessType fromString(String type) {
        if (type == null) {
            return null;
        }
        try {
            return BusinessType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}