package com.warehouse.controller;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Supplier;
import com.warehouse.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 供应商管理控制器
 * 权限要求：系统管理员(ADMIN)和库管员(WAREHOUSE_KEEPER)
 */
@RestController
@RequestMapping("/suppliers")
@CrossOrigin
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 获取所有启用的供应商
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER') or hasRole('EMPLOYEE')")
    public Result<List<Supplier>> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    /**
     * 分页查询供应商
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER')")
    public Result<PageResult<Supplier>> getSuppliers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return supplierService.getSuppliers(keyword, status, page, size);
    }

    /**
     * 根据ID获取供应商详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER') or hasRole('EMPLOYEE')")
    public Result<Supplier> getSupplierById(@PathVariable Integer id) {
        return supplierService.getSupplierById(id);
    }

    /**
     * 创建供应商
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER')")
    public Result<Supplier> createSupplier(@Valid @RequestBody Supplier supplier) {
        return supplierService.createSupplier(supplier);
    }

    /**
     * 更新供应商
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER')")
    public Result<Supplier> updateSupplier(@PathVariable Integer id, @Valid @RequestBody Supplier supplier) {
        return supplierService.updateSupplier(id, supplier);
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteSupplier(@PathVariable Integer id) {
        return supplierService.deleteSupplier(id);
    }

    /**
     * 切换供应商状态
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER')")
    public Result<Supplier> toggleSupplierStatus(@PathVariable Integer id) {
        return supplierService.toggleSupplierStatus(id);
    }

    /**
     * 获取供应商统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE_KEEPER') or hasRole('EMPLOYEE')")
    public Result<Object> getSupplierStatistics() {
        return supplierService.getSupplierStatistics();
    }
}

