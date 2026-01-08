package com.warehouse.service;

import com.warehouse.common.PageResult;
import com.warehouse.entity.Product;
import com.warehouse.entity.StockAdjustment;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.ProductRepository;
import com.warehouse.repository.StockAdjustmentRepository;
import com.warehouse.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 库存调整服务，处理增减库存及历史记录查询。
 */
@Service
public class StockAdjustmentService extends BaseService<StockAdjustment, Integer> {
    @Autowired
    private StockAdjustmentRepository stockAdjustmentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    protected BaseRepository<StockAdjustment, Integer> getRepository() {
        return stockAdjustmentRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public StockAdjustment createAdjustment(StockAdjustment adjustment) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Product product = productRepository.findActiveById(adjustment.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));

            int beforeQty = product.getStockQty();
            int afterQty;

            if (adjustment.getAdjustmentType() == StockAdjustment.AdjustmentType.INCREASE) {
                afterQty = beforeQty + adjustment.getQuantity();
            } else {
                afterQty = beforeQty - adjustment.getQuantity();
                if (afterQty < 0) {
                    throw new RuntimeException("调整后库存不能为负数");
                }
            }

            // 处理 operator：如果 operator 为空对象（只有实例但没有 ID），则设为 null
            if (adjustment.getOperator() != null && adjustment.getOperator().getId() == null) {
                adjustment.setOperator(null);
            }

            // 设置当前操作人
            if (adjustment.getOperator() == null) {
                // 如果没有指定操作人，使用当前用户
                // 这里可以根据需要创建一个简单的User对象或者只设置ID
            }

            adjustment.setBeforeQty(beforeQty);
            adjustment.setAfterQty(afterQty);

            // 更新商品库存
            product.setStockQty(afterQty);
            productRepository.save(product);

            StockAdjustment savedAdjustment = create(adjustment);
            return savedAdjustment;
        } finally {
            UserContext.clear();
        }
    }

    public PageResult<StockAdjustment> getAdjustments(Integer productId, StockAdjustment.AdjustmentType adjustmentType,
                                                      String startDate, String endDate, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // 转换时间字符串为LocalDateTime
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        // 支持多种时间格式
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        };

        if (startDate != null && !startDate.trim().isEmpty()) {
            startTime = parseDateTime(startDate, formatters);
            if (startTime == null) {
                System.err.println("开始时间格式错误: " + startDate + "，支持格式：yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm, yyyy-MM-dd");
            }
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            endTime = parseDateTime(endDate, formatters);
            if (endTime == null) {
                System.err.println("结束时间格式错误: " + endDate + "，支持格式：yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm, yyyy-MM-dd");
            }
        }

        // 如果结束时间只有日期，设置时间为23:59:59
        if (endTime != null && endDate != null && endDate.matches("\\d{4}-\\d{2}-\\d{2}$")) {
            endTime = endTime.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        }

        // 使用动态查询方法，支持按商品ID、调整类型和时间范围的灵活组合
        Page<StockAdjustment> adjustmentPage = stockAdjustmentRepository.findActiveByConditions(
                productId, adjustmentType, startTime, endTime, pageable);

        return new PageResult<>(adjustmentPage.getTotalElements(), adjustmentPage.getContent());
    }

    /**
     * 解析日期时间字符串，支持多种格式
     * @param dateTimeStr 日期时间字符串
     * @param formatters 格式化器数组
     * @return 解析后的LocalDateTime，失败返回null
     */
    private LocalDateTime parseDateTime(String dateTimeStr, DateTimeFormatter[] formatters) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }
        return null;
    }

    /**
     * 删除库存调整记录（逻辑删除）
     */
    @Transactional
    public boolean deleteAdjustment(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            if (stockAdjustmentRepository.existsActiveById(id)) {
                softDelete(id);
                return true;
            }
            return false;
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 恢复删除的库存调整记录
     */
    @Transactional
    public boolean restoreAdjustment(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restore(id);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 获取已删除的库存调整记录
     */
    public java.util.List<StockAdjustment> getDeletedAdjustments() {
        return findAllDeleted();
    }

    /**
     * 根据商品ID获取库存调整记录
     */
    public java.util.List<StockAdjustment> getAdjustmentsByProductId(Integer productId) {
        return stockAdjustmentRepository.findActiveByProductId(productId);
    }

    /**
     * 根据调整类型获取库存调整记录
     */
    public java.util.List<StockAdjustment> getAdjustmentsByType(StockAdjustment.AdjustmentType type) {
        return stockAdjustmentRepository.findActiveByAdjustmentType(type);
    }

    /**
     * 根据操作人获取库存调整记录
     */
    public java.util.List<StockAdjustment> getAdjustmentsByOperator(Integer operatorId) {
        return stockAdjustmentRepository.findActiveByOperatorId(operatorId);
    }

    /**
     * 批量删除库存调整记录
     */
    @Transactional
    public void batchDeleteAdjustments(java.util.List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            softDeleteAll(ids);
        } finally {
            UserContext.clear();
        }
    }
}

