package com.warehouse.controller;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.WarehouseLocationStatisticsDTO;
import com.warehouse.entity.WarehouseLocation;
import com.warehouse.service.WarehouseLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 仓库位置控制器
 */
@RestController
@RequestMapping("/warehouse-locations")
@Validated
public class WarehouseLocationController {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseLocationController.class);

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    /**
     * 分页查询仓库位置
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER')")
    public Result<PageResult<WarehouseLocation>> getWarehouseLocations(
            @RequestParam(required = false) Integer warehouseId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        logger.info("分页查询仓库位置，仓库ID: {}, 状态: {}, 关键字: {}, 页码: {}, 大小: {}",
                   warehouseId, status, keyword, page, size);

        Result<PageResult<WarehouseLocation>> result = warehouseLocationService.getWarehouseLocations(
                warehouseId, status, keyword, page, size);

        logOperation("查询仓库位置列表", "warehouseId=" + warehouseId + ", status=" + status + ", keyword=" + keyword);
        return result;
    }

    /**
     * 根据仓库ID获取位置列表
     */
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER')")
    public Result<List<WarehouseLocation>> getLocationsByWarehouse(
            @PathVariable @NotNull Integer warehouseId) {

        logger.info("根据仓库ID获取位置列表，仓库ID: {}", warehouseId);

        Result<List<WarehouseLocation>> result = warehouseLocationService.getLocationsByWarehouse(warehouseId);

        logOperation("查询仓库位置列表", "warehouseId=" + warehouseId);
        return result;
    }

    /**
     * 根据ID获取仓库位置详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER')")
    public Result<WarehouseLocation> getWarehouseLocationById(
            @PathVariable @NotNull Integer id) {

        logger.info("获取仓库位置详情，ID: {}", id);

        Result<WarehouseLocation> result = warehouseLocationService.getWarehouseLocationById(id);

        logOperation("查询仓库位置详情", "id=" + id);
        return result;
    }

    /**
     * 创建仓库位置
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER')")
    public Result<WarehouseLocation> createWarehouseLocation(
            @Valid @RequestBody WarehouseLocation location) {

        logger.info("创建仓库位置，仓库ID: {}, 位置: {}",
                   location.getWarehouse() != null ? location.getWarehouse().getId() : null, location.getFullLocation());

        Result<WarehouseLocation> result = warehouseLocationService.createWarehouseLocation(location);

        if (result.getCode() == 200) {
            logOperation("创建仓库位置", "warehouseId=" + location.getWarehouse().getId() +
                        ", location=" + location.getFullLocation());
        }
        return result;
    }

    /**
     * 更新仓库位置
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER')")
    public Result<WarehouseLocation> updateWarehouseLocation(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody WarehouseLocation location) {

        logger.info("更新仓库位置，ID: {}, 新位置: {}", id, location.getFullLocation());

        Result<WarehouseLocation> result = warehouseLocationService.updateWarehouseLocation(id, location);

        if (result.getCode() == 200) {
            logOperation("更新仓库位置", "id=" + id + ", location=" + location.getFullLocation());
        }
        return result;
    }

    /**
     * 删除仓库位置
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteWarehouseLocation(
            @PathVariable @NotNull Integer id) {

        logger.info("删除仓库位置，ID: {}", id);

        Result<String> result = warehouseLocationService.deleteWarehouseLocation(id);

        if (result.getCode() == 200) {
            logOperation("删除仓库位置", "id=" + id);
        }
        return result;
    }

    /**
     * 切换仓库位置状态
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER')")
    public Result<WarehouseLocation> toggleWarehouseLocationStatus(
            @PathVariable @NotNull Integer id) {

        logger.info("切换仓库位置状态，ID: {}", id);

        Result<WarehouseLocation> result = warehouseLocationService.toggleWarehouseLocationStatus(id);

        if (result.getCode() == 200) {
            logOperation("切换仓库位置状态", "id=" + id + ", status=" + result.getData().getStatus());
        }
        return result;
    }

    /**
     * 批量创建仓库位置
     */
    @PostMapping("/batch/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER')")
    public Result<List<WarehouseLocation>> batchCreateLocations(
            @PathVariable @NotNull Integer warehouseId,
            @Valid @RequestBody List<WarehouseLocation> locations) {

        logger.info("批量创建仓库位置，仓库ID: {}, 数量: {}", warehouseId, locations.size());

        Result<List<WarehouseLocation>> result = warehouseLocationService.batchCreateLocations(warehouseId, locations);

        if (result.getCode() == 200) {
            logOperation("批量创建仓库位置", "warehouseId=" + warehouseId + ", count=" + result.getData().size());
        }
        return result;
    }

    /**
     * 获取仓库位置统计信息
     */
    @GetMapping("/statistics/{warehouseId}")
   @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER')")
    public Result<WarehouseLocationStatisticsDTO> getWarehouseLocationStatistics(
            @PathVariable @NotNull Integer warehouseId) {

        logger.info("获取仓库位置统计信息，仓库ID: {}", warehouseId);

        Result<WarehouseLocationStatisticsDTO> result = warehouseLocationService.getWarehouseLocationStatistics(warehouseId);

        logOperation("查询仓库位置统计", "warehouseId=" + warehouseId);
        return result;
    }

    /**
     * 记录操作日志
     */
    private void logOperation(String operation, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "unknown";

        logger.info("操作日志 - 用户: {}, 操作: {}, 详情: {}", username, operation, details);

        // 这里可以集成到操作日志记录服务中
        // operationLogService.log(username, operation, details);
    }
}