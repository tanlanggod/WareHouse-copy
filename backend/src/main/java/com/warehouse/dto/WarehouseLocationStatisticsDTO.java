package com.warehouse.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 仓库位置统计信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLocationStatisticsDTO {

    private long totalCount;
    private long enabledCount;
    private long disabledCount;
}