package com.warehouse.repository;

import com.warehouse.entity.StockAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存调整记录仓储接口。
 */
@Repository
public interface StockAdjustmentRepository extends BaseRepository<StockAdjustment, Integer> {

    /**
     * 动态查询库存调整记录，支持按商品ID、调整类型和时间范围筛选
     * @param productId 商品ID（可选）
     * @param adjustmentType 调整类型（可选）
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT sa FROM StockAdjustment sa WHERE sa.isDeleted = 0 AND " +
           "(:productId IS NULL OR sa.product.id = :productId) AND " +
           "(:adjustmentType IS NULL OR sa.adjustmentType = :adjustmentType) AND " +
           "(:startDate IS NULL OR sa.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR sa.createdAt <= :endDate) " +
           "ORDER BY sa.createdAt DESC")
    Page<StockAdjustment> findActiveByConditions(
            @Param("productId") Integer productId,
            @Param("adjustmentType") StockAdjustment.AdjustmentType adjustmentType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate")  LocalDateTime endDate,
            Pageable pageable);

    /**
     * 按商品ID查询
     */
    Page<StockAdjustment> findByProductId(Integer productId, Pageable pageable);

    @Query("SELECT sa FROM StockAdjustment sa WHERE sa.isDeleted = 0 AND sa.product.id = :productId")
    Page<StockAdjustment> findActiveByProductId(@Param("productId") Integer productId, Pageable pageable);

    @Query("SELECT sa FROM StockAdjustment sa WHERE sa.isDeleted = 0 AND sa.product.id = :productId")
    List<StockAdjustment> findActiveByProductId(@Param("productId") Integer productId);

    // 根据调整类型查找
    List<StockAdjustment> findByAdjustmentType(StockAdjustment.AdjustmentType adjustmentType);

    @Query("SELECT sa FROM StockAdjustment sa WHERE sa.isDeleted = 0 AND sa.adjustmentType = :adjustmentType")
    List<StockAdjustment> findActiveByAdjustmentType(@Param("adjustmentType") StockAdjustment.AdjustmentType adjustmentType);

    // 根据操作人查找
    List<StockAdjustment> findByOperatorId(Integer operatorId);

    @Query("SELECT sa FROM StockAdjustment sa WHERE sa.isDeleted = 0 AND sa.operator.id = :operatorId")
    List<StockAdjustment> findActiveByOperatorId(@Param("operatorId") Integer operatorId);
}

