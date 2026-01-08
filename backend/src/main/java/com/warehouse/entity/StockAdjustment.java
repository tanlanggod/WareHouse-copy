package com.warehouse.entity;

import com.warehouse.enums.ApprovalStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 库存调整记录，追踪手工增减库存的原因与数量。
 */
@Entity
@Table(name = "stock_adjustment")
@Data
@EqualsAndHashCode(callSuper = true)
public class StockAdjustment extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "adjustment_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdjustmentType adjustmentType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "before_qty", nullable = false)
    private Integer beforeQty;

    @Column(name = "after_qty", nullable = false)
    private Integer afterQty;

    @Column(nullable = false, length = 255)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(name = "approval_status", length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.DRAFT;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @Column(name = "approval_remark", length = 500)
    private String approvalRemark;

    @Column(name = "flow_id", length = 50)
    private String flowId;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
    }

    public enum AdjustmentType {
        INCREASE,  // 增加
        DECREASE   // 减少
    }
}

