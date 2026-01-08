package com.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仓库统计信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStatisticsDTO {

    /**
     * 仓库总数
     */
    private long totalCount;

    /**
     * 启用仓库数量
     */
    private long enabledCount;

    /**
     * 禁用仓库数量
     */
    private long disabledCount;

    /**
     * 启用仓库占比
     */
    private double enabledPercentage;

    /**
     * 禁用仓库占比
     */
    private double disabledPercentage;

    /**
     * 构造函数（计算百分比）
     */
    public WarehouseStatisticsDTO(long totalCount, long enabledCount, long disabledCount) {
        this.totalCount = totalCount;
        this.enabledCount = enabledCount;
        this.disabledCount = disabledCount;

        if (totalCount > 0) {
            this.enabledPercentage = Math.round((double) enabledCount / totalCount * 10000.0) / 100.0;
            this.disabledPercentage = Math.round((double) disabledCount / totalCount * 10000.0) / 100.0;
        } else {
            this.enabledPercentage = 0.0;
            this.disabledPercentage = 0.0;
        }
    }
}