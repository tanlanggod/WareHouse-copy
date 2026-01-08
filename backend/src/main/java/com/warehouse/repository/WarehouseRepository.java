package com.warehouse.repository;

import com.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 仓库数据访问接口
 */
@Repository
public interface WarehouseRepository extends BaseRepository<Warehouse, Integer>, JpaSpecificationExecutor<Warehouse> {

    /**
     * 根据名称查找仓库
     */
    Optional<Warehouse> findByName(String name);

    @Query("SELECT w FROM Warehouse w WHERE w.name = :name AND w.isDeleted = 0")
    Optional<Warehouse> findActiveByName(@Param("name") String name);

    /**
     * 根据编号查找仓库
     */
    Optional<Warehouse> findByCode(String code);

    @Query("SELECT w FROM Warehouse w WHERE w.code = :code AND w.isDeleted = 0")
    Optional<Warehouse> findActiveByCode(@Param("code") String code);

    /**
     * 根据状态查找仓库
     */
    List<Warehouse> findAllByStatus(Integer status);

    @Query("SELECT w FROM Warehouse w WHERE w.status = :status AND w.isDeleted = 0")
    List<Warehouse> findActiveByStatus(@Param("status") Integer status);

    /**
     * 统计指定状态的仓库数量
     */
    long countByStatus(Integer status);

    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.status = :status AND w.isDeleted = 0")
    long countActiveByStatus(@Param("status") Integer status);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.name = :name AND w.id != :id AND w.isDeleted = 0")
    long countActiveByNameAndIdNot(@Param("name") String name, @Param("id") Integer id);

    /**
     * 检查编号是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.code = :code AND w.id != :id AND w.isDeleted = 0")
    long countActiveByCodeAndIdNot(@Param("code") String code, @Param("id") Integer id);

    /**
     * 模糊搜索仓库
     */
    @Query("SELECT w FROM Warehouse w WHERE w.isDeleted = 0 AND (" +
           "LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Warehouse> findActiveByKeyword(@Param("keyword") String keyword);

    /**
     * 根据状态和关键字搜索
     */
    @Query("SELECT w FROM Warehouse w WHERE w.status = :status AND w.isDeleted = 0 AND (" +
           "LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Warehouse> findActiveByStatusAndKeyword(@Param("status") Integer status, @Param("keyword") String keyword);
}