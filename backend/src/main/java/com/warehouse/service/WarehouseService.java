package com.warehouse.service;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.WarehouseStatisticsDTO;
import com.warehouse.entity.Warehouse;
import com.warehouse.entity.WarehouseLocation;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.WarehouseRepository;
import com.warehouse.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 仓库服务
 */
@Service
public class WarehouseService extends BaseService<Warehouse, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseService.class);

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    protected BaseRepository<Warehouse, Integer> getRepository() {
        return warehouseRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    /**
     * 获取所有仓库
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouses", key = "'all'")
    public Result<List<Warehouse>> getAllWarehouses() {
        try {
            List<Warehouse> warehouses = warehouseRepository.findActiveByStatus(1);
            return Result.success("获取仓库列表成功", warehouses);
        } catch (Exception e) {
            logger.error("获取仓库列表失败: {}", e.getMessage(), e);
            return Result.error("获取仓库列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询仓库
     */
    @Transactional(readOnly = true)
    public Result<PageResult<Warehouse>> getWarehouses(String keyword, Integer status, int page, int size) {
        try {
            Specification<Warehouse> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // 关键字搜索
                if (StringUtils.hasText(keyword)) {
                    String searchKeyword = "%" + keyword.toLowerCase() + "%";
                    predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchKeyword),
                        cb.like(cb.lower(root.get("code")), searchKeyword),
                        cb.like(cb.lower(root.get("address")), searchKeyword),
                        cb.like(cb.lower(root.get("contactPerson")), searchKeyword)
                    ));
                }

                // 状态过滤
                if (status != null) {
                    predicates.add(cb.equal(root.get("status"), status));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            Page<Warehouse> pageResult = warehouseRepository.findAll(spec, pageable);
            PageResult<Warehouse> result = new PageResult<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()
            );

            return Result.success("查询仓库列表成功", result);
        } catch (Exception e) {
            logger.error("查询仓库列表失败: {}", e.getMessage(), e);
            return Result.error("查询仓库列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取仓库
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouse", key = "#id")
    public Result<Warehouse> getWarehouseById(Integer id) {
        try {
            Optional<Warehouse> warehouse = findActiveById(id);
            if (warehouse.isPresent()) {
                return Result.success("获取仓库信息成功", warehouse.get());
            } else {
                return Result.error("仓库不存在");
            }
        } catch (Exception e) {
            logger.error("获取仓库信息失败: {}", e.getMessage(), e);
            return Result.error("获取仓库信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建仓库
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouses", "warehouse"}, allEntries = true)
    public Result<Warehouse> createWarehouse(Warehouse warehouse) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            // 验证仓库对象不为空
            if (warehouse == null) {
                return Result.error("仓库信息不能为空");
            }

            // 验证仓库名称不为空
            if (!StringUtils.hasText(warehouse.getName())) {
                return Result.error("仓库名称不能为空");
            }

            // 验证仓库地址不为空
            if (!StringUtils.hasText(warehouse.getAddress())) {
                return Result.error("仓库地址不能为空");
            }

            // 验证仓库名称唯一性
            if (warehouseRepository.findActiveByName(warehouse.getName()).isPresent()) {
                return Result.error("仓库名称已存在");
            }

            // 验证仓库编号唯一性
            if (StringUtils.hasText(warehouse.getCode()) &&
                warehouseRepository.findActiveByCode(warehouse.getCode()).isPresent()) {
                return Result.error("仓库编号已存在");
            }

            // 设置默认状态
            if (warehouse.getStatus() == null) {
                warehouse.setStatus(1);
            }

            Warehouse savedWarehouse = create(warehouse);
            logger.info("创建仓库成功，ID: {}, 名称: {}", savedWarehouse.getId(), savedWarehouse.getName());

            return Result.success("创建仓库成功", savedWarehouse);
        } catch (Exception e) {
            logger.error("创建仓库失败: {}", e.getMessage(), e);
            return Result.error("创建仓库失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 更新仓库
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouses", "warehouse"}, allEntries = true)
    public Result<Warehouse> updateWarehouse(Integer id, Warehouse warehouse) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Warehouse> existingWarehouse = findActiveById(id);
            if (!existingWarehouse.isPresent()) {
                return Result.error("仓库不存在");
            }

            Warehouse warehouseToUpdate = existingWarehouse.get();

            // 验证仓库名称唯一性（排除当前仓库）
            if (!warehouse.getName().equals(warehouseToUpdate.getName()) &&
                warehouseRepository.findActiveByName(warehouse.getName()).isPresent()) {
                return Result.error("仓库名称已存在");
            }

            // 验证仓库编号唯一性（排除当前仓库）
            if (StringUtils.hasText(warehouse.getCode()) &&
                !warehouse.getCode().equals(warehouseToUpdate.getCode()) &&
                warehouseRepository.findActiveByCode(warehouse.getCode()).isPresent()) {
                return Result.error("仓库编号已存在");
            }

            // 更新字段
            warehouseToUpdate.setName(warehouse.getName());
            warehouseToUpdate.setAddress(warehouse.getAddress());
            warehouseToUpdate.setCode(warehouse.getCode());
            warehouseToUpdate.setContactPerson(warehouse.getContactPerson());
            warehouseToUpdate.setPhone(warehouse.getPhone());
            warehouseToUpdate.setStatus(warehouse.getStatus());

            Warehouse savedWarehouse = update(warehouseToUpdate);
            logger.info("更新仓库成功，ID: {}, 名称: {}", savedWarehouse.getId(), savedWarehouse.getName());

            return Result.success("更新仓库成功", savedWarehouse);
        } catch (Exception e) {
            logger.error("更新仓库失败: {}", e.getMessage(), e);
            return Result.error("更新仓库失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 删除仓库
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouses", "warehouse"}, allEntries = true)
    public Result<String> deleteWarehouse(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Warehouse> warehouse = findActiveById(id);
            if (!warehouse.isPresent()) {
                return Result.error("仓库不存在");
            }

            // 检查是否有关联的位置
            List<WarehouseLocation> locations = warehouse.get().getLocations();
            if (locations != null && !locations.isEmpty()) {
                return Result.error("仓库下还有位置信息，无法删除");
            }

            softDelete(id);
            logger.info("删除仓库成功，ID: {}", id);

            return Result.success("删除仓库成功", "删除成功");
        } catch (Exception e) {
            logger.error("删除仓库失败: {}", e.getMessage(), e);
            return Result.error("删除仓库失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 切换仓库状态
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouses", "warehouse"}, allEntries = true)
    public Result<Warehouse> toggleWarehouseStatus(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Warehouse> warehouse = findActiveById(id);
            if (!warehouse.isPresent()) {
                return Result.error("仓库不存在");
            }

            Warehouse warehouseToUpdate = warehouse.get();
            int newStatus = warehouseToUpdate.getStatus() == 1 ? 0 : 1;
            warehouseToUpdate.setStatus(newStatus);

            Warehouse savedWarehouse = update(warehouseToUpdate);
            logger.info("切换仓库状态成功，ID: {}, 新状态: {}", id, newStatus);

            return Result.success("切换仓库状态成功", savedWarehouse);
        } catch (Exception e) {
            logger.error("切换仓库状态失败: {}", e.getMessage(), e);
            return Result.error("切换仓库状态失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 获取仓库统计信息
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "warehouse-stats", key = "'summary'")
    public Result<WarehouseStatisticsDTO> getWarehouseStatistics() {
        try {
            long totalCount = count();
            long enabledCount = warehouseRepository.countByStatus(1);
            long disabledCount = totalCount - enabledCount;

            WarehouseStatisticsDTO stats = new WarehouseStatisticsDTO(totalCount, enabledCount, disabledCount);

            return Result.success("获取仓库统计信息成功", stats);
        } catch (Exception e) {
            logger.error("获取仓库统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取仓库统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 恢复删除的仓库
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouses", "warehouse"}, allEntries = true)
    public Result<String> restoreWarehouse(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restore(id);
            logger.info("恢复仓库成功，ID: {}", id);

            return Result.success("恢复仓库成功", "恢复成功");
        } catch (Exception e) {
            logger.error("恢复仓库失败: {}", e.getMessage(), e);
            return Result.error("恢复仓库失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 获取已删除的仓库
     */
    @Transactional(readOnly = true)
    public Result<List<Warehouse>> getDeletedWarehouses() {
        try {
            List<Warehouse> deletedWarehouses = findAllDeleted();
            return Result.success("获取已删除仓库列表成功", deletedWarehouses);
        } catch (Exception e) {
            logger.error("获取已删除仓库列表失败: {}", e.getMessage(), e);
            return Result.error("获取已删除仓库列表失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除仓库
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouses", "warehouse"}, allEntries = true)
    public Result<String> batchDeleteWarehouses(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            softDeleteAll(ids);
            logger.info("批量删除仓库成功，ID数量: {}", ids.size());

            return Result.success("批量删除仓库成功", "删除成功");
        } catch (Exception e) {
            logger.error("批量删除仓库失败: {}", e.getMessage(), e);
            return Result.error("批量删除仓库失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 批量恢复仓库
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"warehouses", "warehouse"}, allEntries = true)
    public Result<String> batchRestoreWarehouses(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restoreAll(ids);
            logger.info("批量恢复仓库成功，ID数量: {}", ids.size());

            return Result.success("批量恢复仓库成功", "恢复成功");
        } catch (Exception e) {
            logger.error("批量恢复仓库失败: {}", e.getMessage(), e);
            return Result.error("批量恢复仓库失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }
}