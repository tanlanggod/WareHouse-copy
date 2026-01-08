package com.warehouse.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 基础仓库接口，提供逻辑删除和审计相关的通用方法
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * 查找未删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = 0")
    List<T> findAllActive();

    /**
     * 分页查找未删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = 0")
    Page<T> findAllActive(Pageable pageable);

    /**
     * 根据ID查找未删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = 0")
    Optional<T> findActiveById(@Param("id") ID id);

    /**
     * 检查记录是否存在且未删除
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = 0")
    boolean existsActiveById(@Param("id") ID id);

    /**
     * 逻辑删除记录
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.isDeleted = 1, e.deletedAt = :deletedAt, e.deleterId = :deleterId WHERE e.id = :id")
    int softDelete(@Param("id") ID id, @Param("deleterId") Integer deleterId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 批量逻辑删除记录
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.isDeleted = 1, e.deletedAt = :deletedAt, e.deleterId = :deleterId WHERE e.id IN :ids")
    int softDeleteAll(@Param("ids") Iterable<ID> ids, @Param("deleterId") Integer deleterId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 恢复删除的记录
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.isDeleted = 0, e.deletedAt = NULL, e.deleterId = NULL WHERE e.id = :id")
    int restore(@Param("id") ID id);

    /**
     * 批量恢复删除的记录
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.isDeleted = 0, e.deletedAt = NULL, e.deleterId = NULL WHERE e.id IN :ids")
    int restoreAll(@Param("ids") Iterable<ID> ids);

    /**
     * 查找已删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = 1")
    List<T> findAllDeleted();

    /**
     * 分页查找已删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = 1")
    Page<T> findAllDeleted(Pageable pageable);

    /**
     * 根据创建人查找未删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.creatorId = :creatorId AND e.isDeleted = 0")
    List<T> findByCreatorId(@Param("creatorId") Integer creatorId);

    /**
     * 根据修改人查找未删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updaterId = :updaterId AND e.isDeleted = 0")
    List<T> findByUpdaterId(@Param("updaterId") Integer updaterId);

    /**
     * 根据创建时间范围查找未删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt BETWEEN :startTime AND :endTime AND e.isDeleted = 0")
    List<T> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据修改时间范围查找未删除的记录
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt BETWEEN :startTime AND :endTime AND e.isDeleted = 0")
    List<T> findByUpdatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}