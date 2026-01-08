package com.warehouse.entity;

import com.warehouse.enums.ApprovalStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 入库记录实体，描述单次入库操作及数量。
 */
@Entity
@Table(name = "inbound")
@Data
@EqualsAndHashCode(callSuper = true)
public class Inbound extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "inbound_no", nullable = false, unique = true, length = 50)
    private String inboundNo;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "inbound_date", nullable = false)
    private LocalDateTime inboundDate;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(length = 255)
    private String remark;

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
        if (inboundDate == null) {
            inboundDate = LocalDateTime.now();
        }
    }
}

