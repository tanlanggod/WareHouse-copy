package com.warehouse.service;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.WarehouseLocationStatisticsDTO;
import com.warehouse.entity.Warehouse;
import com.warehouse.entity.WarehouseLocation;
import com.warehouse.exception.WarehouseLocationExceptionHelper;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.WarehouseLocationRepository;
import com.warehouse.repository.WarehouseRepository;
import com.warehouse.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 仓库位置服务
 */
@Service
public class WarehouseLocationService extends BaseService<WarehouseLocation, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseLocationService.class);

    @Autowired
    private WarehouseLocationRepository warehouseLocationRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    protected BaseRepository<WarehouseLocation, Integer> getRepository() {
        return warehouseLocationRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    /**
     * 分页查询仓库位置
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouseLocations", key = "'page:' + #page + ':size:' + #size + ':warehouseId:' + #warehouseId + ':status:' + #status + ':keyword:' + #keyword")
    public Result<PageResult<WarehouseLocation>> getWarehouseLocations(Integer warehouseId, Integer status, String keyword, int page, int size) {
        try {
            Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            // 使用简单的查询方法
            Page<WarehouseLocation> pageResult;
            if (warehouseId != null) {
                pageResult = warehouseLocationRepository.findActiveByWarehouseId(warehouseId, pageable);
            } else {
                pageResult = warehouseLocationRepository.findAll(pageable);
            }

            PageResult<WarehouseLocation> result = new PageResult<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()
            );

            return Result.success("查询仓库位置列表成功", result);
        } catch (Exception e) {
            logger.error("查询仓库位置列表失败: {}", e.getMessage(), e);
            return Result.error("查询仓库位置列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据仓库ID获取位置列表
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouseLocations", key = "'byWarehouse:' + #warehouseId")
    public Result<List<WarehouseLocation>> getLocationsByWarehouse(Integer warehouseId) {
        try {
            List<WarehouseLocation> locations = warehouseLocationRepository.findByWarehouseIdAndStatus(warehouseId, 1);
            return Result.success("获取仓库位置列表成功", locations);
        } catch (Exception e) {
            logger.error("获取仓库位置列表失败: {}", e.getMessage(), e);
            return Result.error("获取仓库位置列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取仓库位置
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouseLocation", key = "#id")
    public Result<WarehouseLocation> getWarehouseLocationById(Integer id) {
        try {
            Optional<WarehouseLocation> location = findActiveById(id);
            if (location.isPresent()) {
                return Result.success("获取仓库位置信息成功", location.get());
            } else {
                return Result.error("仓库位置不存在");
            }
        } catch (Exception e) {
            logger.error("获取仓库位置信息失败: {}", e.getMessage(), e);
            return Result.error("获取仓库位置信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建仓库位置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouseLocations", "warehouseLocation"}, allEntries = true)
    public Result<WarehouseLocation> createWarehouseLocation(WarehouseLocation location) {
        try {
            logger.info("=== 创建仓库位置调试信息 ===");
            logger.info("接收到的位置数据: {}", location);

            // 验证仓库对象是否存在
            Warehouse warehouse = location.getWarehouse();
            if (warehouse == null || warehouse.getId() == null) {
                logger.error("仓库对象为空或仓库ID为空");
                return Result.error("仓库不能为空");
            }

            // 验证仓库是否存在
            Optional<Warehouse> warehouseOpt = warehouseRepository.findById(warehouse.getId());
            if (!warehouseOpt.isPresent()) {
                logger.error("仓库ID {} 对应的仓库不存在", warehouse.getId());
                return Result.error("指定的仓库不存在");
            }
            warehouse = warehouseOpt.get();
            location.setWarehouse(warehouse);
            logger.info("找到仓库，ID: {}, 名称: {}", warehouse.getId(), warehouse.getName());

            // 验证位置编码唯一性
            String rackNumber = location.getRackNumber();
            String shelfLevel = location.getShelfLevel();
            String binNumber = location.getBinNumber();

            if (warehouseLocationRepository.existsByLocation(
                warehouse.getId(), rackNumber, shelfLevel, binNumber, null)) {
                logger.warn("位置编码已存在: 仓库ID={}, 位置={}{}{}",
                    warehouse.getId(), rackNumber, shelfLevel, binNumber);
                return Result.error("位置编码已存在");
            }

            // 设置默认状态
            if (location.getStatus() == null) {
                location.setStatus(1);
            }

            WarehouseLocation savedLocation = warehouseLocationRepository.save(location);
            logger.info("创建仓库位置成功，ID: {}, 仓库: {}, 位置: {}{}{}",
                savedLocation.getId(),
                warehouse.getName(),
                savedLocation.getRackNumber(),
                savedLocation.getShelfLevel(),
                savedLocation.getBinNumber());

            return Result.success("创建仓库位置成功", savedLocation);
        } catch (DataIntegrityViolationException e) {
            return WarehouseLocationExceptionHelper.handleDataIntegrityViolationException(e, "创建仓库位置");
        } catch (TransactionSystemException e) {
            return WarehouseLocationExceptionHelper.handleTransactionSystemException(e, "创建仓库位置");
        } catch (Exception e) {
            return WarehouseLocationExceptionHelper.handleGeneralException(e, "创建仓库位置", location);
        }
    }

    /**
     * 更新仓库位置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouseLocations", "warehouseLocation"}, allEntries = true, beforeInvocation = false)
    public Result<WarehouseLocation> updateWarehouseLocation(Integer id, WarehouseLocation location) {
        try {
            Optional<WarehouseLocation> existingLocation = warehouseLocationRepository.findById(id);
            if (!existingLocation.isPresent()) {
                return Result.error("仓库位置不存在");
            }

            WarehouseLocation locationToUpdate = existingLocation.get();

            // 验证仓库是否存在
            if (location.getWarehouse() != null && location.getWarehouse().getId() != null) {
                // 如果直接提供了warehouse对象
                Optional<Warehouse> warehouse = warehouseRepository.findById(location.getWarehouse().getId());
                if (!warehouse.isPresent()) {
                    return Result.error("仓库不存在");
                }
                locationToUpdate.setWarehouse(warehouse.get());
            }

            // 获取仓库ID用于验证
            Integer warehouseId = locationToUpdate.getWarehouse().getId();

            // 验证位置编码唯一性（排除当前记录）
            if (location.getRackNumber() != null && location.getShelfLevel() != null) {
                String binNumber = location.getBinNumber();
                if (warehouseLocationRepository.existsByLocation(
                    warehouseId,
                    location.getRackNumber(),
                    location.getShelfLevel(),
                    binNumber,
                    id)) {
                    return Result.error("位置编码已存在");
                }
            }

            // 更新字段（只更新非null字段）
            if (location.getRackNumber() != null) {
                locationToUpdate.setRackNumber(location.getRackNumber());
            }
            if (location.getShelfLevel() != null) {
                locationToUpdate.setShelfLevel(location.getShelfLevel());
            }
            if (location.getBinNumber() != null) {
                locationToUpdate.setBinNumber(location.getBinNumber());
            }
            if (location.getDescription() != null) {
                locationToUpdate.setDescription(location.getDescription());
            }
            if (location.getStatus() != null) {
                locationToUpdate.setStatus(location.getStatus());
            }

            // 重新刷新版本字段避免乐观锁冲突
            locationToUpdate.setVersion(System.currentTimeMillis());

            WarehouseLocation savedLocation = warehouseLocationRepository.save(locationToUpdate);
            logger.info("更新仓库位置成功，ID: {}, 位置: {}", savedLocation.getId(), savedLocation.getFullLocation());

            return Result.success("更新仓库位置成功", savedLocation);

        } catch (DataIntegrityViolationException e) {
            logger.error("数据库完整性约束违反: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("uk_location")) {
                return Result.error("位置编码已存在：同一仓库内货架号、层号、货位号的组合必须唯一");
            }
            return Result.error("数据冲突，请检查是否重复：" + e.getMessage());
        } catch (TransactionSystemException e) {
            Throwable rootCause = e.getRootCause();
            logger.error("事务系统错误: {}", rootCause != null ? rootCause.getMessage() : e.getMessage());
            return Result.error("事务处理失败：" + (rootCause != null ? rootCause.getMessage() : e.getMessage()));
        } catch (Exception e) {
            logger.error("更新仓库位置失败: {}", e.getMessage(), e);
            return Result.error("更新仓库位置失败：" + e.getMessage());
        }
    }

    /**
     * 删除仓库位置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouseLocations", "warehouseLocation"}, allEntries = true)
    public Result<String> deleteWarehouseLocation(Integer id) {
        try {
            Optional<WarehouseLocation> location = warehouseLocationRepository.findById(id);
            if (!location.isPresent()) {
                return Result.error("仓库位置不存在");
            }

            // TODO: 检查是否有关联的商品或库存记录
            // 这里可以添加业务逻辑，检查位置是否有关联数据

            warehouseLocationRepository.deleteById(id);
            logger.info("删除仓库位置成功，ID: {}", id);

            return Result.success("删除仓库位置成功", "删除成功");
        } catch (Exception e) {
            logger.error("删除仓库位置失败: {}", e.getMessage(), e);
            return Result.error("删除仓库位置失败：" + e.getMessage());
        }
    }

    /**
     * 切换仓库位置状态
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouseLocations", "warehouseLocation"}, allEntries = true, beforeInvocation = false)
    public Result<WarehouseLocation> toggleWarehouseLocationStatus(Integer id) {
        try {
            Optional<WarehouseLocation> location = warehouseLocationRepository.findById(id);
            if (!location.isPresent()) {
                return Result.error("仓库位置不存在");
            }

            WarehouseLocation locationToUpdate = location.get();
            int newStatus = locationToUpdate.getStatus() == 1 ? 0 : 1;
            locationToUpdate.setStatus(newStatus);

            // 重新刷新版本字段避免乐观锁冲突
            locationToUpdate.setVersion(System.currentTimeMillis());

            WarehouseLocation savedLocation = warehouseLocationRepository.save(locationToUpdate);
            logger.info("切换仓库位置状态成功，ID: {}, 新状态: {}", id, newStatus);

            return Result.success("切换仓库位置状态成功", savedLocation);
        } catch (Exception e) {
            logger.error("切换仓库位置状态失败: {}", e.getMessage(), e);
            return Result.error("切换仓库位置状态失败：" + e.getMessage());
        }
    }

    /**
     * 批量创建仓库位置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouseLocations", "warehouseLocation"}, allEntries = true)
    public Result<List<WarehouseLocation>> batchCreateLocations(Integer warehouseId, List<WarehouseLocation> locations) {
        try {
            // 验证仓库是否存在
            Optional<Warehouse> warehouse = warehouseRepository.findById(warehouseId);
            if (!warehouse.isPresent()) {
                return Result.error("仓库不存在");
            }

            List<WarehouseLocation> savedLocations = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // 先验证所有位置的唯一性
            for (WarehouseLocation location : locations) {
                if (warehouseLocationRepository.existsByLocation(
                    warehouseId,
                    location.getRackNumber(),
                    location.getShelfLevel(),
                    location.getBinNumber(),
                    null)) {
                    errors.add("位置编码已存在: " + location.getFullLocation());
                }
            }

            // 如果有重复的，直接返回错误
            if (!errors.isEmpty()) {
                logger.warn("批量创建位置检查发现重复: {}", errors);
                return Result.error("批量创建失败，存在重复的位置编码: " + String.join(", ", errors));
            }

            // 批量设置仓库和默认状态
            for (WarehouseLocation location : locations) {
                location.setWarehouse(warehouse.get());
                if (location.getStatus() == null) {
                    location.setStatus(1);
                }
            }

            try {
                // 使用JPA批量保存
                savedLocations = warehouseLocationRepository.saveAll(locations);
                logger.info("批量创建仓库位置成功，数量: {}", savedLocations.size());
            } catch (Exception e) {
                logger.error("批量保存仓库位置失败: {}", e.getMessage(), e);
                return WarehouseLocationExceptionHelper.handleGeneralException(e, "批量创建仓库位置", locations);
            }

            return Result.success("批量创建仓库位置成功", savedLocations);
        } catch (Exception e) {
            logger.error("批量创建仓库位置失败: {}", e.getMessage(), e);
            return Result.error("批量创建仓库位置失败：" + e.getMessage());
        }
    }

    /**
     * 获取仓库位置统计信息
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouseLocation-stats", key = "'warehouse:' + #warehouseId")
    public Result<WarehouseLocationStatisticsDTO> getWarehouseLocationStatistics(Integer warehouseId) {
        try {
            long totalCount = warehouseLocationRepository.countByWarehouseId(warehouseId);
            long enabledCount = warehouseLocationRepository.countByWarehouseIdAndStatus(warehouseId, 1);
            long disabledCount = totalCount - enabledCount;

            WarehouseLocationStatisticsDTO stats = new WarehouseLocationStatisticsDTO(totalCount, enabledCount, disabledCount);

            return Result.success("获取仓库位置统计信息成功", stats);
        } catch (Exception e) {
            logger.error("获取仓库位置统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取仓库位置统计信息失败：" + e.getMessage());
        }
    }
}