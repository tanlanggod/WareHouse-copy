package com.warehouse.service;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.Supplier;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.SupplierRepository;
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
 * 供应商服务
 */
@Service
@Transactional
public class SupplierService extends BaseService<Supplier, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    protected BaseRepository<Supplier, Integer> getRepository() {
        return supplierRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    /**
     * 获取所有启用的供应商
     */
    @Cacheable(value = "suppliers", key = "'all'")
    public Result<List<Supplier>> getAllSuppliers() {
        try {
            List<Supplier> suppliers = supplierRepository.findActiveByStatus(1);
            return Result.success("获取供应商列表成功", suppliers);
        } catch (Exception e) {
            logger.error("获取供应商列表失败: {}", e.getMessage(), e);
            return Result.error("获取供应商列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询供应商
     */
    public Result<PageResult<Supplier>> getSuppliers(String keyword, Integer status, int page, int size) {
        try {
            Specification<Supplier> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // 关键字搜索
                if (StringUtils.hasText(keyword)) {
                    String searchKeyword = "%" + keyword.toLowerCase() + "%";
                    predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchKeyword),
                        cb.like(cb.lower(root.get("contactPerson")), searchKeyword),
                        cb.like(cb.lower(root.get("address")), searchKeyword),
                        cb.like(cb.lower(root.get("email")), searchKeyword)
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

            Page<Supplier> pageResult = supplierRepository.findAll(spec, pageable);
            PageResult<Supplier> result = new PageResult<>(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()
            );

            return Result.success("查询供应商列表成功", result);
        } catch (Exception e) {
            logger.error("查询供应商列表失败: {}", e.getMessage(), e);
            return Result.error("查询供应商列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取供应商
     */
    @Cacheable(value = "supplier", key = "#id")
    public Result<Supplier> getSupplierById(Integer id) {
        try {
            Optional<Supplier> supplier = findActiveById(id);
            if (supplier.isPresent()) {
                return Result.success("获取供应商信息成功", supplier.get());
            } else {
                return Result.error("供应商不存在");
            }
        } catch (Exception e) {
            logger.error("获取供应商信息失败: {}", e.getMessage(), e);
            return Result.error("获取供应商信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建供应商
     */
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public Result<Supplier> createSupplier(Supplier supplier) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            // 验证供应商名称唯一性
            if (supplierRepository.findActiveByName(supplier.getName()).isPresent()) {
                return Result.error("供应商名称已存在");
            }

            // 设置默认状态
            if (supplier.getStatus() == null) {
                supplier.setStatus(1);
            }

            Supplier savedSupplier = create(supplier); // 使用BaseService的create方法
            logger.info("创建供应商成功，ID: {}, 名称: {}", savedSupplier.getId(), savedSupplier.getName());

            return Result.success("创建供应商成功", savedSupplier);
        } catch (Exception e) {
            logger.error("创建供应商失败: {}", e.getMessage(), e);
            return Result.error("创建供应商失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 更新供应商
     */
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public Result<Supplier> updateSupplier(Integer id, Supplier supplier) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Supplier> existingSupplier = findActiveById(id);
            if (!existingSupplier.isPresent()) {
                return Result.error("供应商不存在");
            }

            Supplier supplierToUpdate = existingSupplier.get();

            // 验证供应商名称唯一性（排除当前供应商）
            if (!supplier.getName().equals(supplierToUpdate.getName()) &&
                supplierRepository.findActiveByName(supplier.getName()).isPresent()) {
                return Result.error("供应商名称已存在");
            }

            // 更新字段
            supplierToUpdate.setName(supplier.getName());
            supplierToUpdate.setContactPerson(supplier.getContactPerson());
            supplierToUpdate.setPhone(supplier.getPhone());
            supplierToUpdate.setAddress(supplier.getAddress());
            supplierToUpdate.setEmail(supplier.getEmail());
            supplierToUpdate.setStatus(supplier.getStatus());

            Supplier savedSupplier = update(supplierToUpdate); // 使用BaseService的update方法
            logger.info("更新供应商成功，ID: {}, 名称: {}", savedSupplier.getId(), savedSupplier.getName());

            return Result.success("更新供应商成功", savedSupplier);
        } catch (Exception e) {
            logger.error("更新供应商失败: {}", e.getMessage(), e);
            return Result.error("更新供应商失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 删除供应商
     */
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public Result<String> deleteSupplier(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Supplier> supplier = findActiveById(id);
            if (!supplier.isPresent()) {
                return Result.error("供应商不存在");
            }

            // TODO: 检查是否有关联的产品
            // 这里可以添加业务逻辑，检查供应商是否有关联的产品

            softDelete(id); // 使用逻辑删除
            logger.info("删除供应商成功，ID: {}", id);

            return Result.success("删除供应商成功", "删除成功");
        } catch (Exception e) {
            logger.error("删除供应商失败: {}", e.getMessage(), e);
            return Result.error("删除供应商失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 恢复删除的供应商
     */
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public Result<String> restoreSupplier(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restore(id); // 使用BaseService的恢复方法
            logger.info("恢复供应商成功，ID: {}", id);

            return Result.success("恢复供应商成功", "恢复成功");
        } catch (Exception e) {
            logger.error("恢复供应商失败: {}", e.getMessage(), e);
            return Result.error("恢复供应商失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 获取已删除的供应商
     */
    public Result<List<Supplier>> getDeletedSuppliers() {
        try {
            List<Supplier> deletedSuppliers = findAllDeleted();
            return Result.success("获取已删除供应商列表成功", deletedSuppliers);
        } catch (Exception e) {
            logger.error("获取已删除供应商列表失败: {}", e.getMessage(), e);
            return Result.error("获取已删除供应商列表失败：" + e.getMessage());
        }
    }

    /**
     * 切换供应商状态
     */
    @CacheEvict(value = {"suppliers", "supplier"}, allEntries = true)
    public Result<Supplier> toggleSupplierStatus(Integer id) {
        try {
            Optional<Supplier> supplier = supplierRepository.findById(id);
            if (!supplier.isPresent()) {
                return Result.error("供应商不存在");
            }

            Supplier supplierToUpdate = supplier.get();
            int newStatus = supplierToUpdate.getStatus() == 1 ? 0 : 1;
            supplierToUpdate.setStatus(newStatus);

            Supplier savedSupplier = supplierRepository.save(supplierToUpdate);
            logger.info("切换供应商状态成功，ID: {}, 新状态: {}", id, newStatus);

            return Result.success("切换供应商状态成功", savedSupplier);
        } catch (Exception e) {
            logger.error("切换供应商状态失败: {}", e.getMessage(), e);
            return Result.error("切换供应商状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取供应商统计信息
     */
    @Cacheable(value = "supplier-stats", key = "'summary'")
    public Result<Object> getSupplierStatistics() {
        try {
            long totalCount = supplierRepository.count();
            long enabledCount = supplierRepository.countByStatus(1);
            long disabledCount = totalCount - enabledCount;

            Object stats = new Object() {
                public final long totalCount = SupplierService.this.supplierRepository.count();
                public final long enabledCount = SupplierService.this.supplierRepository.countByStatus(1);
                public final long disabledCount = totalCount - enabledCount;
            };

            return Result.success("获取供应商统计信息成功", stats);
        } catch (Exception e) {
            logger.error("获取供应商统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取供应商统计信息失败：" + e.getMessage());
        }
    }
}