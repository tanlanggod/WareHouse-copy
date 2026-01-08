package com.warehouse.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 仓库位置创建DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLocationCreateDTO {

    @NotNull(message = "仓库ID不能为空")
    private Integer warehouseId;

    @NotBlank(message = "货架号不能为空")
    @Size(max = 50, message = "货架号长度不能超过50个字符")
    private String rackNumber;

    @NotBlank(message = "层号不能为空")
    @Size(max = 20, message = "层号长度不能超过20个字符")
    private String shelfLevel;

    @Size(max = 50, message = "货位号长度不能超过50个字符")
    private String binNumber;

    @Size(max = 255, message = "位置描述长度不能超过255个字符")
    private String description;

    private Integer status;
}