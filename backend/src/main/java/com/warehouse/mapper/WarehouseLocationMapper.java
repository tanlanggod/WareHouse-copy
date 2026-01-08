package com.warehouse.mapper;

import com.warehouse.dto.WarehouseLocationCreateDTO;
import com.warehouse.dto.WarehouseLocationUpdateDTO;
import com.warehouse.entity.Warehouse;
import com.warehouse.entity.WarehouseLocation;
import org.springframework.stereotype.Component;

/**
 * 仓库位置DTO转换器
 */
@Component
public class WarehouseLocationMapper {

    /**
     * 从创建DTO转换为实体
     */
    public WarehouseLocation toEntity(WarehouseLocationCreateDTO dto, Warehouse warehouse) {
        if (dto == null) {
            return null;
        }

        WarehouseLocation location = new WarehouseLocation();
        location.setWarehouse(warehouse);
        location.setRackNumber(dto.getRackNumber());
        location.setShelfLevel(dto.getShelfLevel());
        location.setBinNumber(dto.getBinNumber());
        location.setDescription(dto.getDescription());
        location.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        return location;
    }

    /**
     * 从更新DTO更新到现有实体
     */
    public void updateEntity(WarehouseLocationUpdateDTO dto, WarehouseLocation location, Warehouse warehouse) {
        if (dto == null || location == null) {
            return;
        }

        // 更新仓库（如果提供）
        if (warehouse != null) {
            location.setWarehouse(warehouse);
        }

        // 只更新非null字段
        if (dto.getRackNumber() != null) {
            location.setRackNumber(dto.getRackNumber());
        }
        if (dto.getShelfLevel() != null) {
            location.setShelfLevel(dto.getShelfLevel());
        }
        if (dto.getBinNumber() != null) {
            location.setBinNumber(dto.getBinNumber());
        }
        if (dto.getDescription() != null) {
            location.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            location.setStatus(dto.getStatus());
        }
    }
}