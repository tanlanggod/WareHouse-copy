package com.warehouse.repository;

import com.warehouse.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志数据访问接口
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Integer> {

    /**
     * 根据用户ID查询操作日志
     */
    Page<OperationLog> findByUserId(Integer userId, Pageable pageable);

    /**
     * 根据用户名查询操作日志
     */
    Page<OperationLog> findByUsernameContaining(String username, Pageable pageable);

    /**
     * 根据操作类型查询操作日志
     */
    Page<OperationLog> findByOperationType(String operationType, Pageable pageable);

    /**
     * 根据资源类型查询操作日志
     */
    Page<OperationLog> findByResourceType(String resourceType, Pageable pageable);

    /**
     * 根据模块查询操作日志
     */
    Page<OperationLog> findByModule(String module, Pageable pageable);

    /**
     * 根据操作状态查询操作日志
     */
    Page<OperationLog> findByIsSuccess(Boolean isSuccess, Pageable pageable);

    /**
     * 根据IP地址查询操作日志
     */
    Page<OperationLog> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * 根据时间范围查询操作日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.createdAt BETWEEN :startTime AND :endTime")
    Page<OperationLog> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            Pageable pageable);

    /**
     * 根据多个条件查询操作日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE " +
           "(:username IS NULL OR ol.username LIKE %:username%) AND " +
           "(:operationType IS NULL OR ol.operationType = :operationType) AND " +
           "(:resourceType IS NULL OR ol.resourceType = :resourceType) AND " +
           "(:module IS NULL OR ol.module = :module) AND " +
           "(:isSuccess IS NULL OR ol.isSuccess = :isSuccess) AND " +
           "(:ipAddress IS NULL OR ol.ipAddress = :ipAddress) AND " +
           "(:startTime IS NULL OR ol.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR ol.createdAt <= :endTime)")
    Page<OperationLog> findByMultipleConditions(@Param("username") String username,
                                               @Param("operationType") String operationType,
                                               @Param("resourceType") String resourceType,
                                               @Param("module") String module,
                                               @Param("isSuccess") Boolean isSuccess,
                                               @Param("ipAddress") String ipAddress,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               Pageable pageable);

    /**
     * 统计各操作类型的数量
     */
    @Query("SELECT ol.operationType, COUNT(ol) FROM OperationLog ol " +
           "WHERE (:startTime IS NULL OR ol.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR ol.createdAt <= :endTime) " +
           "GROUP BY ol.operationType")
    List<Object[]> countByOperationType(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各资源类型的数量
     */
    @Query("SELECT ol.resourceType, COUNT(ol) FROM OperationLog ol " +
           "WHERE (:startTime IS NULL OR ol.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR ol.createdAt <= :endTime) " +
           "GROUP BY ol.resourceType")
    List<Object[]> countByResourceType(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各用户的操作数量
     */
    @Query("SELECT ol.username, COUNT(ol) FROM OperationLog ol " +
           "WHERE (:startTime IS NULL OR ol.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR ol.createdAt <= :endTime) " +
           "GROUP BY ol.username ORDER BY COUNT(ol) DESC")
    List<Object[]> countByUsername(@Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计成功与失败的操作数量
     */
    @Query("SELECT ol.isSuccess, COUNT(ol) FROM OperationLog ol " +
           "WHERE (:startTime IS NULL OR ol.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR ol.createdAt <= :endTime) " +
           "GROUP BY ol.isSuccess")
    List<Object[]> countBySuccess(@Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询响应时间超过指定毫秒数的操作日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.responseTime > :responseTime")
    Page<OperationLog> findByResponseTimeGreaterThan(@Param("responseTime") Long responseTime,
                                                    Pageable pageable);

    /**
     * 查询包含错误信息的操作日志
     */
    @Query("SELECT ol FROM OperationLog ol WHERE ol.errorMessage IS NOT NULL AND ol.errorMessage != ''")
    Page<OperationLog> findWithError(Pageable pageable);

    /**
     * 根据请求ID查询操作日志
     */
    OperationLog findByRequestId(String requestId);

    /**
     * 删除指定日期之前的操作日志
     */
    @Query("DELETE FROM OperationLog ol WHERE ol.createdAt < :beforeDate")
    void deleteByCreatedAtBefore(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 统计总操作次数
     */
    @Query("SELECT COUNT(ol) FROM OperationLog ol " +
           "WHERE (:startTime IS NULL OR ol.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR ol.createdAt <= :endTime)")
    Long countAll(@Param("startTime") LocalDateTime startTime,
                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近的操作日志
     */
    @Query("SELECT ol FROM OperationLog ol ORDER BY ol.createdAt DESC")
    List<OperationLog> findRecentLogs(Pageable pageable);
}