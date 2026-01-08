package com.warehouse.controller;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.ApprovalRecord;
import com.warehouse.entity.Inbound;
import com.warehouse.service.ApprovalService;
import com.warehouse.service.InboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库接口控制器，提供入库单的增删查操作。
 */
@RestController
@RequestMapping("/inbounds")
@CrossOrigin
public class InboundController {
    @Autowired
    private InboundService inboundService;

    @Autowired
    private ApprovalService approvalService;

    @GetMapping
    public Result<PageResult<Inbound>> getInbounds(
            @RequestParam(required = false)  LocalDateTime startDate,
            @RequestParam(required = false)  LocalDateTime endDate,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Inbound> result = inboundService.getInbounds(
                startDate, endDate, productId, supplierId, page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Inbound> getInboundById(@PathVariable Integer id) {
        Inbound inbound = inboundService.getInboundById(id);
        return Result.success(inbound);
    }

    @PostMapping
    public Result<Inbound> createInbound(@RequestBody Inbound inbound) {
        Inbound created = inboundService.createInbound(inbound);
        return Result.success(created);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteInbound(@PathVariable Integer id) {
        inboundService.deleteInbound(id);
        return Result.success(null);
    }

    /**
     * 导出入库记录到Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        try {
            byte[] data = inboundService.exportToExcel(startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "inbounds.xlsx");

            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 提交入库审批
     */
    @PostMapping("/{id}/submit-approval")
    public Result<Void> submitForApproval(
            @PathVariable Integer id,
            @RequestBody(required = false) String remark) {
        try {
            inboundService.submitForApproval(id, remark);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批通过入库
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approveInbound(
            @PathVariable Integer id,
            @RequestBody(required = false) String approvalRemark) {
        try {
            inboundService.approveInbound(id, approvalRemark);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批拒绝入库
     */
    @PostMapping("/{id}/reject")
    public Result<Void> rejectInbound(
            @PathVariable Integer id,
            @RequestBody(required = false) String approvalRemark) {
        try {
            inboundService.rejectInbound(id, approvalRemark);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消审批
     */
    @PostMapping("/{id}/cancel-approval")
    public Result<Void> cancelInboundApproval(
            @PathVariable Integer id,
            @RequestBody(required = false) String remark) {
        try {
            inboundService.cancelInboundApproval(id, remark);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取入库审批历史
     */
    @GetMapping("/{id}/approval-history")
    public Result<List<ApprovalRecord>> getApprovalHistory(@PathVariable Integer id) {
        try {
            List<ApprovalRecord> history = approvalService.getApprovalHistory(id, com.warehouse.enums.BusinessType.INBOUND);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

