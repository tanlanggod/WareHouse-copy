package com.warehouse.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.Size;

/**
 * 仓库位置更新DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLocationUpdateDTO {

    private Integer warehouseId;

    @Size(max = 50, message = "货架号长度不能超过50个字符")
    private String rackNumber;

    @Size(max = 20, message = "层号长度不能超过20个字符")
    private String shelfLevel;

    @Size(max = 50, message = "货位号长度不能超过50个字符")
    private String binNumber;

    @Size(max = 255, message = "位置描述长度不能超过255个字符")
    private String description;

    private Integer status;
}