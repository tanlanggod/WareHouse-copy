package com.warehouse.service;

import com.warehouse.common.PageResult;
import com.warehouse.entity.ApprovalRecord;
import com.warehouse.entity.Inbound;
import com.warehouse.entity.Product;
import com.warehouse.entity.User;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import com.warehouse.repository.InboundRepository;
import com.warehouse.repository.ProductRepository;
import com.warehouse.util.UserContext;
import com.warehouse.util.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 入库业务服务，负责入库单操作及库存同步。
 */
@Service
public class InboundService {
    @Autowired
    private InboundRepository inboundRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ApprovalService approvalService;

    private static final Logger logger = LoggerFactory.getLogger(InboundService.class);

    /**
     * 创建入库单（草稿状态）
     */
    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public Inbound createInbound(Inbound inbound) {
        if (inbound.getProduct() == null) {
            throw new RuntimeException("商品信息不能为空");
        }
        Integer productId = inbound.getProduct().getId();
        if (productId == null) {
            throw new RuntimeException("商品ID不能为空");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 确保操作员正确设置：从 UserContext 获取当前用户ID
        Integer currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null && currentUserId > 0) {
            // 创建 User 实例并设置ID
            User operator = new User();
            operator.setId(currentUserId);
            inbound.setOperator(operator);

            // 记录操作员信息到日志
            logger.info("设置入库操作员：入库单ID={}, 操作员ID={}", inbound.getInboundNo(), currentUserId);
        } else {
            // 如果没有当前用户，清空操作员
            inbound.setOperator(null);
            logger.warn("未找到当前登录用户，入库操作员设置为空：入库单ID={}", inbound.getInboundNo());
        }

        // 处理供应商：如果 supplier 为空对象（只有实例但没有 ID），则设为 null
        if (inbound.getSupplier() != null && inbound.getSupplier().getId() == null) {
            inbound.setSupplier(null);
        }

        // 生成入库单号
        String inboundNo = "IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                          UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        inbound.setInboundNo(inboundNo);

        // 设置默认状态为草稿
        inbound.setApprovalStatus(ApprovalStatus.DRAFT);

        return inboundRepository.save(inbound);
    }

    /**
     * 提交入库审批
     */
    @Transactional
    public void submitForApproval(Integer inboundId, String remark) {
        // 验证入库单ID
        if (inboundId == null) {
            throw new RuntimeException("入库单ID不能为空");
        }

        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new RuntimeException("入库单不存在"));

        if (inbound.getApprovalStatus() != ApprovalStatus.DRAFT) {
            throw new RuntimeException("只有草稿状态的入库单可以提交审批");
        }

        // 更新状态为待审批
        inbound.setApprovalStatus(ApprovalStatus.PENDING);
        inboundRepository.save(inbound);

        // 创建审批记录
        approvalService.submitForApproval(inboundId, BusinessType.INBOUND, remark);

        logger.info("入库单 {} 已提交审批", inboundId);
    }

    /**
     * 审批通过入库单（执行实际入库）
     */
    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public void approveInbound(Integer inboundId, String approvalRemark) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new RuntimeException("入库单不存在"));

        if (inbound.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("只有待审批状态的入库单可以审批");
        }

        // 执行审批
        ApprovalRecord approvalRecord = approvalService.approve(inboundId, BusinessType.INBOUND, approvalRemark);

        // 更新入库单信息
        inbound.setApprovalStatus(ApprovalStatus.APPROVED);
        inbound.setApprover(approvalRecord.getApprover());
        inbound.setApprovalTime(approvalRecord.getApprovalTime());
        inbound.setApprovalRemark(approvalRecord.getApprovalRemark());

        // 实际更新库存
        Product product = inbound.getProduct();
        if (product != null) {
            product.setStockQty(product.getStockQty() + inbound.getQuantity());
            productRepository.save(product);
        }

        inboundRepository.save(inbound);

        logger.info("入库单 {} 审批通过并已执行入库", inboundId);
    }

    /**
     * 审批拒绝入库单
     */
    @Transactional
    public void rejectInbound(Integer inboundId, String approvalRemark) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new RuntimeException("入库单不存在"));

        if (inbound.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("只有待审批状态的入库单可以审批");
        }

        // 执行审批
        ApprovalRecord approvalRecord = approvalService.reject(inboundId, BusinessType.INBOUND, approvalRemark);

        // 更新入库单信息
        inbound.setApprovalStatus(ApprovalStatus.REJECTED);
        inbound.setApprover(approvalRecord.getApprover());
        inbound.setApprovalTime(approvalRecord.getApprovalTime());
        inbound.setApprovalRemark(approvalRecord.getApprovalRemark());

        inboundRepository.save(inbound);

        logger.info("入库单 {} 审批拒绝", inboundId);
    }

    /**
     * 取消审批
     */
    @Transactional
    public void cancelInboundApproval(Integer inboundId, String remark) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new RuntimeException("入库单不存在"));

        if (inbound.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("只有待审批状态的入库单可以取消审批");
        }

        // 执行取消
        approvalService.cancel(inboundId, BusinessType.INBOUND, remark);

        // 更新入库单状态
        inbound.setApprovalStatus(ApprovalStatus.CANCELLED);

        inboundRepository.save(inbound);

        logger.info("入库单 {} 取消审批", inboundId);
    }

    public PageResult<Inbound> getInbounds(LocalDateTime startDate, LocalDateTime endDate,
                                            Integer productId, Integer supplierId,
                                            Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Inbound> inboundPage = inboundRepository.findActiveByConditions(
                startDate, endDate, productId, supplierId, pageable);
        return new PageResult<>(inboundPage.getTotalElements(), inboundPage.getContent());
    }

    public Inbound getInboundById(Integer id) {
        if (id == null) {
            throw new RuntimeException("入库单ID不能为空");
        }
        return inboundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库单不存在"));
    }



    @Transactional
    @CacheEvict(value = {"products", "lowStockProducts"}, allEntries = true)
    public void deleteInbound(Integer id) {
        if (id == null) {
            throw new RuntimeException("入库单ID不能为空");
        }
        Inbound inbound = inboundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库单不存在"));

        // 恢复库存
        Product product = inbound.getProduct();
        if (product != null) {
            product.setStockQty(product.getStockQty() - inbound.getQuantity());
            productRepository.save(product);
        }

        inboundRepository.deleteById(id);
    }

    /**
     * 导出入库记录到Excel
     */
    public byte[] exportToExcel(LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        List<Inbound> inbounds;
        if (startDate != null && endDate != null) {
            inbounds = inboundRepository.findByInboundDateBetween(startDate, endDate);
        } else {
            inbounds = inboundRepository.findAll();
        }

        String[] headers = {"入库单号", "商品编号", "商品名称", "入库数量", "供应商", "入库日期", "操作员", "备注"};

        return ExcelUtil.exportExcel(inbounds, headers,
                Inbound::getInboundNo,
                i -> i.getProduct() != null ? i.getProduct().getCode() : "",
                i -> i.getProduct() != null ? i.getProduct().getName() : "",
                Inbound::getQuantity,
                i -> i.getSupplier() != null ? i.getSupplier().getName() : "",
                Inbound::getInboundDate,
                i -> i.getOperator() != null ? i.getOperator().getUsername() : "",
                i -> i.getRemark() != null ? i.getRemark() : ""
        );
    }
}

