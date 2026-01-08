package com.warehouse.repository;

import com.warehouse.entity.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 类别仓储接口，供商品分类管理使用。
 */
@Repository
public interface CategoryRepository extends BaseRepository<Category, Integer> {
    boolean existsByName(String name);

    Category findByName(String name);

    // 重写查询方法，添加逻辑删除过滤
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.isDeleted = 0")
    Category findActiveByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.name = :name AND c.isDeleted = 0")
    boolean existsActiveByName(@Param("name") String name);

    List<Category> findByNameContainingIgnoreCase(String name);
}

