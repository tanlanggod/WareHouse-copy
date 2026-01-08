package com.warehouse.repository;

import com.warehouse.entity.SystemAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 系统公告数据访问层
 */
@Repository
public interface SystemAnnouncementRepository extends JpaRepository<SystemAnnouncement, Integer>,
        JpaSpecificationExecutor<SystemAnnouncement>, BaseRepository<SystemAnnouncement, Integer> {

    /**
     * 查找当前有效的公告（用于用户查看）
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND a.status = 'PUBLISHED' " +
           "AND (a.effectiveStartTime IS NULL OR a.effectiveStartTime <= :now) " +
           "AND (a.effectiveEndTime IS NULL OR a.effectiveEndTime >= :now) " +
           "ORDER BY a.isPinned DESC, a.priority DESC, a.publishTime DESC")
    List<SystemAnnouncement> findActiveAnnouncements(@Param("now") LocalDateTime now);

    /**
     * 根据目标受众查找公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND a.status = 'PUBLISHED' " +
           "AND (a.effectiveStartTime IS NULL OR a.effectiveStartTime <= :now) " +
           "AND (a.effectiveEndTime IS NULL OR a.effectiveEndTime >= :now) " +
           "AND (a.targetAudience = 'ALL' OR (:role IS NOT NULL AND a.targetRoles LIKE %:role%)) " +
           "ORDER BY a.isPinned DESC, a.priority DESC, a.publishTime DESC")
    List<SystemAnnouncement> findAnnouncementsForRole(@Param("now") LocalDateTime now, @Param("role") String role);

    /**
     * 查找置顶公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND a.isPinned = true AND a.status = 'PUBLISHED' " +
           "AND (a.effectiveStartTime IS NULL OR a.effectiveStartTime <= :now) " +
           "AND (a.effectiveEndTime IS NULL OR a.effectiveEndTime >= :now) " +
           "ORDER BY a.priority DESC, a.publishTime DESC")
    List<SystemAnnouncement> findPinnedAnnouncements(@Param("now") LocalDateTime now);

    /**
     * 根据状态查找公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 AND a.status = :status " +
           "ORDER BY a.createdAt DESC")
    List<SystemAnnouncement> findByStatus(@Param("status") SystemAnnouncement.AnnouncementStatus status);

    /**
     * 根据类型查找公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 AND a.type = :type " +
           "ORDER BY a.createdAt DESC")
    List<SystemAnnouncement> findByType(@Param("type") SystemAnnouncement.AnnouncementType type);

    /**
     * 分页查询公告（支持多条件筛选）
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND (:title IS NULL OR a.title LIKE %:title%) " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:priority IS NULL OR a.priority = :priority) " +
           "AND (:targetAudience IS NULL OR a.targetAudience = :targetAudience) " +
           "AND (:publisherId IS NULL OR a.publisherId = :publisherId) " +
           "ORDER BY a.isPinned DESC, a.priority DESC, a.createdAt DESC")
    Page<SystemAnnouncement> findAnnouncementsWithFilters(
            @Param("title") String title,
            @Param("type") SystemAnnouncement.AnnouncementType type,
            @Param("status") SystemAnnouncement.AnnouncementStatus status,
            @Param("priority") SystemAnnouncement.Priority priority,
            @Param("targetAudience") SystemAnnouncement.TargetAudience targetAudience,
            @Param("publisherId") Integer publisherId,
            Pageable pageable);

    /**
     * 查找已过期的公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND a.status = 'PUBLISHED' " +
           "AND a.effectiveEndTime IS NOT NULL AND a.effectiveEndTime < :now")
    List<SystemAnnouncement> findExpiredAnnouncements(@Param("now") LocalDateTime now);

    /**
     * 统计公告数量
     */
    @Query("SELECT COUNT(a) FROM SystemAnnouncement a WHERE a.isDeleted = 0")
    long countAllAnnouncements();

    /**
     * 统计各状态公告数量
     */
    @Query("SELECT a.status, COUNT(a) FROM SystemAnnouncement a WHERE a.isDeleted = 0 GROUP BY a.status")
    List<Object[]> countByStatus();

    /**
     * 统计各类型公告数量
     */
    @Query("SELECT a.type, COUNT(a) FROM SystemAnnouncement a WHERE a.isDeleted = 0 GROUP BY a.type")
    List<Object[]> countByType();

    /**
     * 统计各优先级公告数量
     */
    @Query("SELECT a.priority, COUNT(a) FROM SystemAnnouncement a WHERE a.isDeleted = 0 GROUP BY a.priority")
    List<Object[]> countByPriority();

    /**
     * 查找指定时间范围内发布的公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND a.publishTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.publishTime DESC")
    List<SystemAnnouncement> findByPublishTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查找需要自动标记为过期的公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND a.status = 'PUBLISHED' " +
           "AND a.effectiveEndTime IS NOT NULL AND a.effectiveEndTime <= :now")
    List<SystemAnnouncement> findAnnouncementsToExpire(@Param("now") LocalDateTime now);

    /**
     * 查找指定发布人发布的公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 AND a.publisherId = :publisherId " +
           "ORDER BY a.publishTime DESC")
    List<SystemAnnouncement> findByPublisherId(@Param("publisherId") Integer publisherId);

    /**
     * 查找阅读次数最多的公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 AND a.status = 'PUBLISHED' " +
           "ORDER BY a.readCount DESC, a.publishTime DESC")
    List<SystemAnnouncement> findTopReadAnnouncements(Pageable pageable);

    /**
     * 根据关键词搜索公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%) " +
           "ORDER BY a.priority DESC, a.publishTime DESC")
    List<SystemAnnouncement> findByKeyword(@Param("keyword") String keyword);

    /**
     * 检查公告标题是否已存在
     */
    boolean existsByTitleAndIsDeleted(String title, Integer isDeleted);

    /**
     * 获取最近发布的公告数量
     */
    @Query("SELECT COUNT(a) FROM SystemAnnouncement a WHERE a.isDeleted = 0 " +
           "AND a.publishTime >= :since")
    long countRecentAnnouncements(@Param("since") LocalDateTime since);
}