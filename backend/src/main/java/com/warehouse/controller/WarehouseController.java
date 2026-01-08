package com.warehouse.controller;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.WarehouseStatisticsDTO;
import com.warehouse.entity.Warehouse;
import com.warehouse.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 仓库管理控制器
 * 权限要求：系统管理员(ADMIN)和库管员(WAREHOUSE_KEEPER)
 */
@RestController
@RequestMapping("/warehouses")
@CrossOrigin
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 获取所有启用的仓库
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER') or hasRole('EMPLOYEE')")
    public Result<List<Warehouse>> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    /**
     * 分页查询仓库
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER')")
    public Result<PageResult<Warehouse>> getWarehouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return warehouseService.getWarehouses(keyword, status, page, size);
    }

    /**
     * 根据ID获取仓库详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER') or hasRole('EMPLOYEE')")
    public Result<Warehouse> getWarehouseById(@PathVariable Integer id) {
        return warehouseService.getWarehouseById(id);
    }

    /**
     * 创建仓库
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER')")
    public Result<Warehouse> createWarehouse(@Valid @RequestBody Warehouse warehouse) {
        return warehouseService.createWarehouse(warehouse);
    }

    /**
     * 更新仓库
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER')")
    public Result<Warehouse> updateWarehouse(@PathVariable Integer id, @Valid @RequestBody Warehouse warehouse) {
        return warehouseService.updateWarehouse(id, warehouse);
    }

    /**
     * 删除仓库
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteWarehouse(@PathVariable Integer id) {
        return warehouseService.deleteWarehouse(id);
    }

    /**
     * 切换仓库状态
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER')")
    public Result<Warehouse> toggleWarehouseStatus(@PathVariable Integer id) {
        return warehouseService.toggleWarehouseStatus(id);
    }

    /**
     * 获取仓库统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER') or hasRole('EMPLOYEE')")
    public Result<WarehouseStatisticsDTO> getWarehouseStatistics() {
        return warehouseService.getWarehouseStatistics();
    }
}