package com.warehouse.repository;

import com.warehouse.entity.Supplier;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 供应商仓储接口
 */
@Repository
public interface SupplierRepository extends BaseRepository<Supplier, Integer>, JpaSpecificationExecutor<Supplier> {

    /**
     * 根据名称查找供应商
     */
    Optional<Supplier> findByName(String name);

    // 添加逻辑删除过滤的查询方法
    @Query("SELECT s FROM Supplier s WHERE s.name = :name AND s.isDeleted = 0")
    Optional<Supplier> findActiveByName(@Param("name") String name);

    /**
     * 根据状态查找供应商
     */
    List<Supplier> findAllByStatus(Integer status);

    @Query("SELECT s FROM Supplier s WHERE s.status = :status AND s.isDeleted = 0")
    List<Supplier> findActiveByStatus(@Param("status") Integer status);

    /**
     * 统计指定状态的供应商数量
     */
    long countByStatus(Integer status);

    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.status = :status AND s.isDeleted = 0")
    long countActiveByStatus(@Param("status") Integer status);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.name = :name AND s.id != :id AND s.isDeleted = 0")
    long countActiveByNameAndIdNot(@Param("name") String name, @Param("id") Integer id);

    /**
     * 模糊搜索供应商
     */
    @Query("SELECT s FROM Supplier s WHERE s.isDeleted = 0 AND (" +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Supplier> findActiveByKeyword(@Param("keyword") String keyword);

    /**
     * 根据状态和关键字搜索
     */
    @Query("SELECT s FROM Supplier s WHERE s.status = :status AND s.isDeleted = 0 AND (" +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Supplier> findActiveByStatusAndKeyword(@Param("status") Integer status, @Param("keyword") String keyword);
}

