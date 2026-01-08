package com.warehouse.entity;

import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 审批记录实体，记录所有审批操作历史
 */
@Entity
@Table(name = "approval_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalRecord extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "business_id", nullable = false)
    private Integer businessId;

    @Column(name = "business_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(name = "approval_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @Column(name = "approver_id")
    private Integer approverId;

    @ManyToOne
    @JoinColumn(name = "approver_id", insertable = false, updatable = false)
    private User approver;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @Column(name = "approval_remark", length = 500)
    private String approvalRemark;

    @Column(name = "submitter_id", nullable = false)
    private Integer submitterId;

    @ManyToOne
    @JoinColumn(name = "submitter_id", insertable = false, updatable = false)
    private User submitter;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "flow_id", length = 50)
    private String flowId;

    @Column(name = "step_order")
    private Integer stepOrder;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (submitTime == null) {
            submitTime = LocalDateTime.now();
        }
        if (approvalTime == null && (approvalStatus == ApprovalStatus.APPROVED || approvalStatus == ApprovalStatus.REJECTED)) {
            approvalTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        if (approvalStatus == ApprovalStatus.APPROVED || approvalStatus == ApprovalStatus.REJECTED) {
            approvalTime = LocalDateTime.now();
        }
    }
}