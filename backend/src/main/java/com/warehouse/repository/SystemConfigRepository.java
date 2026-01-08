package com.warehouse.repository;

import com.warehouse.entity.SystemConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置数据访问接口
 */
@Repository
public interface SystemConfigRepository extends BaseRepository<SystemConfig, Integer> {

    /**
     * 根据配置键查找未删除的配置
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configKey = :configKey AND sc.isDeleted = 0")
    Optional<SystemConfig> findActiveByKey(@Param("configKey") String configKey);

    /**
     * 根据配置键模糊查找未删除的配置
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configKey LIKE %:keyword% AND sc.isDeleted = 0")
    List<SystemConfig> findActiveByKeyContaining(@Param("keyword") String keyword);

    /**
     * 根据模块查找未删除的配置（通过配置键前缀）
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configKey LIKE :module% AND sc.isDeleted = 0")
    List<SystemConfig> findActiveByModule(@Param("module") String module);

    /**
     * 根据描述模糊查找未删除的配置
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configDesc LIKE %:keyword% AND sc.isDeleted = 0")
    List<SystemConfig> findActiveByDescContaining(@Param("keyword") String keyword);

    /**
     * 分页查询未删除的系统配置
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isDeleted = 0 ORDER BY sc.updatedAt DESC")
    Page<SystemConfig> findActiveConfigs(Pageable pageable);

    /**
     * 检查配置键是否存在且未删除
     */
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SystemConfig sc WHERE sc.configKey = :configKey AND sc.isDeleted = 0")
    boolean existsActiveByKey(@Param("configKey") String configKey);

    /**
     * 根据配置值模糊查找未删除的配置
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configValue LIKE %:keyword% AND sc.isDeleted = 0")
    List<SystemConfig> findActiveByValueContaining(@Param("keyword") String keyword);

    /**
     * 获取所有活跃的系统配置（按配置键排序）
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isDeleted = 0 ORDER BY sc.configKey")
    List<SystemConfig> findAllActiveOrderByKey();
}