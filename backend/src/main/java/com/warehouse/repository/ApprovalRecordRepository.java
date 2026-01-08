package com.warehouse.repository;

import com.warehouse.entity.ApprovalRecord;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 审批记录数据访问接口
 */
@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Integer> {

    /**
     * 根据业务ID和业务类型查找审批记录
     */
    List<ApprovalRecord> findByBusinessIdAndBusinessTypeOrderBySubmitTimeDesc(
            Integer businessId, BusinessType businessType);

    /**
     * 根据审批人ID和审批状态查找记录
     */
    Page<ApprovalRecord> findByApproverIdAndApprovalStatusOrderBySubmitTimeDesc(
            Integer approverId, ApprovalStatus approvalStatus, Pageable pageable);

    /**
     * 根据提交人ID查找记录
     */
    Page<ApprovalRecord> findBySubmitterIdOrderBySubmitTimeDesc(
            Integer submitterId, Pageable pageable);

    /**
     * 查找待审批记录（针对特定角色）
     */
    @Query("SELECT ar FROM ApprovalRecord ar " +
           "WHERE ar.approvalStatus = :status " +
           "AND ar.businessType IN :businessTypes " +
           "ORDER BY ar.submitTime DESC")
    Page<ApprovalRecord> findPendingApprovals(
            @Param("status") ApprovalStatus status,
            @Param("businessTypes") List<BusinessType> businessTypes,
            Pageable pageable);

    /**
     * 查找用户相关的待审批记录
     */
    @Query("SELECT ar FROM ApprovalRecord ar " +
           "WHERE ar.approvalStatus = :status " +
           "AND (ar.approverId IS NULL OR ar.approverId = :userId) " +
           "ORDER BY ar.submitTime DESC")
    Page<ApprovalRecord> findPendingApprovalsForUser(
            @Param("status") ApprovalStatus status,
            @Param("userId") Integer userId,
            Pageable pageable);

    /**
     * 统计待审批数量
     */
    @Query("SELECT COUNT(ar) FROM ApprovalRecord ar " +
           "WHERE ar.approvalStatus = :status " +
           "AND ar.businessType IN :businessTypes")
    Long countPendingApprovals(
            @Param("status") ApprovalStatus status,
            @Param("businessTypes") List<BusinessType> businessTypes);

    /**
     * 统计用户待审批数量
     */
    @Query("SELECT COUNT(ar) FROM ApprovalRecord ar " +
           "WHERE ar.approvalStatus = :status " +
           "AND (ar.approverId IS NULL OR ar.approverId = :userId)")
    Long countPendingApprovalsForUser(
            @Param("status") ApprovalStatus status,
            @Param("userId") Integer userId);

    /**
     * 查找指定时间范围内的审批记录
     */
    @Query("SELECT ar FROM ApprovalRecord ar " +
           "WHERE ar.approvalTime BETWEEN :startTime AND :endTime " +
           "ORDER BY ar.approvalTime DESC")
    List<ApprovalRecord> findByApprovalTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 检查是否存在待审批的记录
     */
    boolean existsByBusinessIdAndBusinessTypeAndApprovalStatus(
            Integer businessId, BusinessType businessType, ApprovalStatus approvalStatus);

    /**
     * 根据流程ID查找记录
     */
    List<ApprovalRecord> findByFlowIdOrderBySubmitTimeDesc(String flowId);
}