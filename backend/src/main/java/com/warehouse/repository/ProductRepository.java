package com.warehouse.repository;

import com.warehouse.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 商品仓储接口，提供分页检索与库存预警查询。
 */
@Repository
public interface ProductRepository extends BaseRepository<Product, Integer> {

    // 基础查询方法
    Optional<Product> findByCode(String code);
    boolean existsByCode(String code);

    // 添加逻辑删除过滤的查询方法
    @Query("SELECT p FROM Product p WHERE p.code = :code AND p.isDeleted = 0")
    Optional<Product> findActiveByCode(@Param("code") String code);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.code = :code AND p.isDeleted = 0")
    boolean existsActiveByCode(@Param("code") String code);

    Page<Product> findByNameContaining(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = 0 AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findActiveByNameContaining(@Param("name") String name, Pageable pageable);

    // 根据条件查询（添加逻辑删除过滤）
    @Query("SELECT p FROM Product p WHERE p.isDeleted = 0 AND " +
           "(:code IS NULL OR p.code LIKE CONCAT('%', :code, '%')) AND " +
           "(:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Product> findActiveByConditions(@Param("code") String code,
                                   @Param("name") String name,
                                   @Param("categoryId") Integer categoryId,
                                   @Param("status") Integer status,
                                   Pageable pageable);

    // 库存预警查询
    @Query("SELECT p FROM Product p WHERE p.isDeleted = 0 AND p.stockQty <= p.minStock AND p.status = 1")
    List<Product> findActiveLowStockProducts();

    // 根据供应商查找商品
    @Query("SELECT p FROM Product p WHERE p.isDeleted = 0 AND p.supplier.id = :supplierId")
    List<Product> findActiveBySupplierId(@Param("supplierId") Integer supplierId);

    // 根据分类查找商品
    @Query("SELECT p FROM Product p WHERE p.isDeleted = 0 AND p.category.id = :categoryId")
    List<Product> findActiveByCategoryId(@Param("categoryId") Integer categoryId);

    // 根据状态查找商品
    @Query("SELECT p FROM Product p WHERE p.isDeleted = 0 AND p.status = :status")
    List<Product> findActiveByStatus(@Param("status") Integer status);

    /**
     * 更新商品图片URL（不触发乐观锁版本更新）
     * @param productId 商品ID
     * @param imageUrl 图片URL
     */
    @Modifying
    @Query(value = "UPDATE product SET image_url = :imageUrl WHERE id = :productId AND is_deleted = 0", nativeQuery = true)
    void updateImageUrl(@Param("productId") Integer productId, @Param("imageUrl") String imageUrl);
}

