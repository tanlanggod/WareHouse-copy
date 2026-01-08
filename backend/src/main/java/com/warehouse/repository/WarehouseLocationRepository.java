package com.warehouse.repository;

import com.warehouse.entity.WarehouseLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 仓库位置数据访问层
 */
@Repository
public interface WarehouseLocationRepository extends BaseRepository<WarehouseLocation, Integer>, JpaSpecificationExecutor<WarehouseLocation> {

    /**
     * 根据仓库ID查找位置列表
     */
    List<WarehouseLocation> findByWarehouseId(Integer warehouseId);

    @Query("SELECT wl FROM WarehouseLocation wl WHERE wl.warehouse.id = :warehouseId AND wl.isDeleted = 0")
    List<WarehouseLocation> findActiveByWarehouseId(@Param("warehouseId") Integer warehouseId);

    /**
     * 根据仓库ID和状态查找位置列表
     */
    List<WarehouseLocation> findByWarehouseIdAndStatus(Integer warehouseId, Integer status);

    @Query("SELECT wl FROM WarehouseLocation wl WHERE wl.warehouse.id = :warehouseId AND wl.status = :status AND wl.isDeleted = 0")
    List<WarehouseLocation> findActiveByWarehouseIdAndStatus(@Param("warehouseId") Integer warehouseId, @Param("status") Integer status);

    /**
     * 根据仓库ID分页查询位置列表
     */
    Page<WarehouseLocation> findByWarehouseId(Integer warehouseId, Pageable pageable);

    @Query("SELECT wl FROM WarehouseLocation wl WHERE wl.warehouse.id = :warehouseId AND wl.isDeleted = 0")
    Page<WarehouseLocation> findActiveByWarehouseId(@Param("warehouseId") Integer warehouseId, Pageable pageable);

    /**
     * 根据仓库ID、货架号、层号、货位号查找位置
     */
    Optional<WarehouseLocation> findByWarehouseIdAndRackNumberAndShelfLevelAndBinNumber(
            Integer warehouseId, String rackNumber, String shelfLevel, String binNumber);

    
    /**
     * 根据仓库ID和货架号查找位置列表
     */
    List<WarehouseLocation> findByWarehouseIdAndRackNumber(Integer warehouseId, String rackNumber);

    /**
     * 根据状态查找位置列表
     */
    List<WarehouseLocation> findByStatus(Integer status);

    /**
     * 根据仓库ID统计位置数量
     */
    @Query("SELECT COUNT(wl) FROM WarehouseLocation wl WHERE wl.warehouse.id = :warehouseId")
    long countByWarehouseId(@Param("warehouseId") Integer warehouseId);

    /**
     * 根据仓库ID和状态统计位置数量
     */
    @Query("SELECT COUNT(wl) FROM WarehouseLocation wl WHERE wl.warehouse.id = :warehouseId AND wl.status = :status")
    long countByWarehouseIdAndStatus(@Param("warehouseId") Integer warehouseId, @Param("status") Integer status);

    /**
     * 根据关键字搜索位置（货架号、层号、货位号、描述）
     */
    @Query("SELECT wl FROM WarehouseLocation wl WHERE " +
           "(wl.warehouse.id = :warehouseId OR :warehouseId IS NULL) AND " +
           "(wl.status = :status OR :status IS NULL) AND " +
           "(LOWER(wl.rackNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(wl.shelfLevel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(wl.binNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(wl.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<WarehouseLocation> findByConditions(@Param("warehouseId") Integer warehouseId,
                                            @Param("status") Integer status,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);

    /**
     * 检查位置编码是否已存在（在同一仓库内）
     */
    @Query("SELECT COUNT(wl) > 0 FROM WarehouseLocation wl WHERE " +
           "wl.warehouse.id = :warehouseId AND " +
           "wl.rackNumber = :rackNumber AND " +
           "wl.shelfLevel = :shelfLevel AND " +
           "wl.binNumber = :binNumber AND " +
           "(:id IS NULL OR wl.id != :id)")
    boolean existsByLocation(@Param("warehouseId") Integer warehouseId,
                           @Param("rackNumber") String rackNumber,
                           @Param("shelfLevel") String shelfLevel,
                           @Param("binNumber") String binNumber,
                           @Param("id") Integer id);
}