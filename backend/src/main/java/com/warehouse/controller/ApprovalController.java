package com.warehouse.controller;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.ApprovalRecord;
import com.warehouse.enums.ApprovalStatus;
import com.warehouse.enums.BusinessType;
import com.warehouse.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/approvals")
@CrossOrigin
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @GetMapping("/pending")
    public Result<PageResult<ApprovalRecord>> getPendingApprovals(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<ApprovalRecord> pageResult = approvalService.getPendingApprovals(pageable);
            PageResult<ApprovalRecord> result = new PageResult<>(
                    Long.valueOf(pageResult.getTotalElements()),
                    pageResult.getContent()
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to get pending approvals: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<List<ApprovalRecord>> getApprovalHistory(
            @RequestParam(required = false) Integer businessId,
            @RequestParam(required = false) BusinessType businessType) {
        try {
            List<ApprovalRecord> history = approvalService.getApprovalHistory(businessId, businessType);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("Failed to get approval history: " + e.getMessage());
        }
    }

    @GetMapping("/my-pending")
    public Result<PageResult<ApprovalRecord>> getMyPendingApprovals(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<ApprovalRecord> pageResult = approvalService.getPendingApprovalsForUser(
                    com.warehouse.util.UserContext.getCurrentUserId(), pageable);
            PageResult<ApprovalRecord> result = new PageResult<>(
                    Long.valueOf(pageResult.getTotalElements()),
                    pageResult.getContent()
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to get my pending approvals: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            Integer businessId = (Integer) request.get("businessId");
            String businessTypeStr = (String) request.get("businessType");
            String approvalRemark = (String) request.get("approvalRemark");
            
            BusinessType businessType = BusinessType.valueOf(businessTypeStr);
            approvalService.approve(businessId, businessType, approvalRemark);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Approval failed: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            Integer businessId = (Integer) request.get("businessId");
            String businessTypeStr = (String) request.get("businessType");
            String approvalRemark = (String) request.get("approvalRemark");
            
            BusinessType businessType = BusinessType.valueOf(businessTypeStr);
            approvalService.reject(businessId, businessType, approvalRemark);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Rejection failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<ApprovalRecord> getApprovalDetail(@PathVariable Integer id) {
        try {
            ApprovalRecord approval = approvalService.getApprovalHistory(id, null)
                    .stream()
                    .filter(ar -> ar.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Approval record not found"));
            return Result.success(approval);
        } catch (Exception e) {
            return Result.error("Failed to get approval details: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Long>> getApprovalStatistics() {
        try {
            Long pendingCount = approvalService.getPendingApprovalCount();
            Map<String, Long> statistics = new HashMap<>();
            statistics.put("pendingCount", pendingCount);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("Failed to get approval statistics: " + e.getMessage());
        }
    }
}