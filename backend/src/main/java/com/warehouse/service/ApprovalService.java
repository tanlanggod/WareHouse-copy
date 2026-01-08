package com.warehouse.service;

import com.warehouse.entity.ApprovalRecord;
import com.warehouse.entity.User;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import com.warehouse.repository.ApprovalRecordRepository;
import com.warehouse.repository.UserRepository;
import com.warehouse.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 审批服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApprovalService {

    private final ApprovalRecordRepository approvalRecordRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    /**
     * 提交审批
     */
    public ApprovalRecord submitForApproval(Integer businessId, BusinessType businessType, String remark) {
        Integer currentUserId = UserContext.getCurrentUserId();

        // 验证用户ID
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录或会话已过期，请重新登录");
        }

        // 验证业务ID
        if (businessId == null) {
            throw new RuntimeException("业务ID不能为空");
        }

        // 检查是否已有待审批记录
        if (approvalRecordRepository.existsByBusinessIdAndBusinessTypeAndApprovalStatus(
                businessId, businessType, ApprovalStatus.PENDING)) {
            throw new RuntimeException("该业务已提交审批，请勿重复提交");
        }

        // 创建审批记录
        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setBusinessId(businessId);
        approvalRecord.setBusinessType(businessType);
        approvalRecord.setApprovalStatus(ApprovalStatus.PENDING);
        approvalRecord.setSubmitterId(currentUserId);
        approvalRecord.setSubmitter(userRepository.findById(currentUserId).orElse(null));
        approvalRecord.setFlowId(generateFlowId());
        approvalRecord.setSubmitTime(LocalDateTime.now());
        approvalRecord.setApprovalRemark(remark);

        approvalRecord = approvalRecordRepository.save(approvalRecord);

        // 发送实时通知给所有有审批权限的用户
        webSocketService.notifyApprovalSubmission(approvalRecord);

        log.info("用户 {} 提交了 {} 业务 {} 的审批申请",
                currentUserId, businessType.getDescription(), businessId);

        return approvalRecord;
    }

    /**
     * 审批通过
     */
    public ApprovalRecord approve(Integer businessId, BusinessType businessType, String approvalRemark) {
        Integer currentUserId = UserContext.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));

        // 查找待审批记录
        ApprovalRecord approvalRecord = approvalRecordRepository
                .findByBusinessIdAndBusinessTypeOrderBySubmitTimeDesc(businessId, businessType)
                .stream()
                .filter(ar -> ar.getApprovalStatus() == ApprovalStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到待审批记录"));

        // 更新审批记录
        approvalRecord.setApprovalStatus(ApprovalStatus.APPROVED);
        approvalRecord.setApproverId(currentUserId);
        approvalRecord.setApprover(currentUser);
        approvalRecord.setApprovalTime(LocalDateTime.now());
        approvalRecord.setApprovalRemark(approvalRemark);

        approvalRecord = approvalRecordRepository.save(approvalRecord);

        // 发送审批完成通知给提交人
        webSocketService.notifyApprovalResult(approvalRecord);

        log.info("用户 {} 审批通过了 {} 业务 {}",
                currentUserId, businessType.getDescription(), businessId);

        return approvalRecord;
    }

    /**
     * 审批拒绝
     */
    public ApprovalRecord reject(Integer businessId, BusinessType businessType, String approvalRemark) {
        Integer currentUserId = UserContext.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));

        // 查找待审批记录
        ApprovalRecord approvalRecord = approvalRecordRepository
                .findByBusinessIdAndBusinessTypeOrderBySubmitTimeDesc(businessId, businessType)
                .stream()
                .filter(ar -> ar.getApprovalStatus() == ApprovalStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到待审批记录"));

        // 更新审批记录
        approvalRecord.setApprovalStatus(ApprovalStatus.REJECTED);
        approvalRecord.setApproverId(currentUserId);
        approvalRecord.setApprover(currentUser);
        approvalRecord.setApprovalTime(LocalDateTime.now());
        approvalRecord.setApprovalRemark(approvalRemark);

        approvalRecord = approvalRecordRepository.save(approvalRecord);

        // 发送审批完成通知给提交人
        webSocketService.notifyApprovalResult(approvalRecord);

        log.info("用户 {} 审批拒绝了 {} 业务 {}",
                currentUserId, businessType.getDescription(), businessId);

        return approvalRecord;
    }

    /**
     * 取消审批
     */
    public ApprovalRecord cancel(Integer businessId, BusinessType businessType, String remark) {
        Integer currentUserId = UserContext.getCurrentUserId();

        // 查找待审批记录
        ApprovalRecord approvalRecord = approvalRecordRepository
                .findByBusinessIdAndBusinessTypeOrderBySubmitTimeDesc(businessId, businessType)
                .stream()
                .filter(ar -> ar.getApprovalStatus() == ApprovalStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到待审批记录"));

        // 只有提交人可以取消
        if (!approvalRecord.getSubmitterId().equals(currentUserId)) {
            throw new RuntimeException("只有提交人可以取消审批");
        }

        // 更新审批记录
        approvalRecord.setApprovalStatus(ApprovalStatus.CANCELLED);
        approvalRecord.setApprovalRemark(remark);
        approvalRecord.setApprovalTime(LocalDateTime.now());

        approvalRecord = approvalRecordRepository.save(approvalRecord);

        log.info("用户 {} 取消了 {} 业务 {} 的审批申请",
                currentUserId, businessType.getDescription(), businessId);

        return approvalRecord;
    }

    /**
     * 获取待审批列表
     */
    @Transactional(readOnly = true)
    public Page<ApprovalRecord> getPendingApprovals(Pageable pageable) {
        List<BusinessType> businessTypes = Arrays.asList(
                BusinessType.INBOUND,
                BusinessType.OUTBOUND,
                BusinessType.STOCK_ADJUSTMENT
        );
        return approvalRecordRepository.findPendingApprovals(
                ApprovalStatus.PENDING, businessTypes, pageable);
    }

    /**
     * 获取用户的待审批列表
     */
    @Transactional(readOnly = true)
    public Page<ApprovalRecord> getPendingApprovalsForUser(Integer userId, Pageable pageable) {
        return approvalRecordRepository.findPendingApprovalsForUser(
                ApprovalStatus.PENDING, userId, pageable);
    }

    /**
     * 获取用户提交的审批记录
     */
    @Transactional(readOnly = true)
    public Page<ApprovalRecord> getUserSubmissions(Integer userId, Pageable pageable) {
        return approvalRecordRepository.findBySubmitterIdOrderBySubmitTimeDesc(userId, pageable);
    }

    /**
     * 获取审批历史记录
     */
    @Transactional(readOnly = true)
    public List<ApprovalRecord> getApprovalHistory(Integer businessId, BusinessType businessType) {
        return approvalRecordRepository.findByBusinessIdAndBusinessTypeOrderBySubmitTimeDesc(
                businessId, businessType);
    }

    /**
     * 统计待审批数量
     */
    @Transactional(readOnly = true)
    public Long getPendingApprovalCount() {
        List<BusinessType> businessTypes = Arrays.asList(
                BusinessType.INBOUND,
                BusinessType.OUTBOUND,
                BusinessType.STOCK_ADJUSTMENT
        );
        return approvalRecordRepository.countPendingApprovals(
                ApprovalStatus.PENDING, businessTypes);
    }

    /**
     * 统计用户待审批数量
     */
    @Transactional(readOnly = true)
    public Long getPendingApprovalCountForUser(Integer userId) {
        return approvalRecordRepository.countPendingApprovalsForUser(
                ApprovalStatus.PENDING, userId);
    }

    /**
     * 生成审批流程ID
     */
    private String generateFlowId() {
        return "FLOW-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 检查用户是否有审批权限
     */
    @Transactional(readOnly = true)
    public boolean hasApprovalPermission(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 管理员和库管员有审批权限
        return user.getRole() == com.warehouse.enums.UserRole.ADMIN ||
               user.getRole() == com.warehouse.enums.UserRole.WAREHOUSE_KEEPER;
    }

    /**
     * 检查用户是否可以提交审批
     */
    @Transactional(readOnly = true)
    public boolean canSubmitApproval(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 所有角色都可以提交审批
        return user.getRole() == com.warehouse.enums.UserRole.ADMIN ||
               user.getRole() == com.warehouse.enums.UserRole.WAREHOUSE_KEEPER ||
               user.getRole() == com.warehouse.enums.UserRole.EMPLOYEE;
    }
}