package com.warehouse.entity;

import com.warehouse.enums.ApprovalStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 出库记录实体，描述商品出库及客户信息。
 */
@Entity
@Table(name = "outbound")
@Data
@EqualsAndHashCode(callSuper = true)
public class Outbound extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "outbound_no", nullable = false, unique = true, length = 50)
    private String outboundNo;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "outbound_date", nullable = false)
    private LocalDateTime outboundDate;

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
        if (outboundDate == null) {
            outboundDate = LocalDateTime.now();
        }
    }
}

