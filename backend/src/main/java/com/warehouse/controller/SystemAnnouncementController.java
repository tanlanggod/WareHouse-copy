package com.warehouse.controller;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.SystemAnnouncement;
import com.warehouse.service.SystemAnnouncementService;
import com.warehouse.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统公告控制器
 */
@RestController
@RequestMapping("/announcements")
@CrossOrigin
public class SystemAnnouncementController {

    private static final Logger logger = LoggerFactory.getLogger(SystemAnnouncementController.class);

    @Autowired
    private SystemAnnouncementService announcementService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户可见的公告列表
     */
    @GetMapping("/visible")
    public Result<List<SystemAnnouncement>> getVisibleAnnouncements() {
        try {
            // 获取当前用户角色
            String userRole = userService.getCurrentUserRole();
            List<SystemAnnouncement> announcements = announcementService.getVisibleAnnouncements(userRole);
            return Result.success(announcements);
        } catch (Exception e) {
            logger.error("获取可见公告失败", e);
            return Result.error("获取公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取置顶公告
     */
    @GetMapping("/pinned")
    public Result<List<SystemAnnouncement>> getPinnedAnnouncements() {
        try {
            List<SystemAnnouncement> announcements = announcementService.getPinnedAnnouncements();
            return Result.success(announcements);
        } catch (Exception e) {
            logger.error("获取置顶公告失败", e);
            return Result.error("获取置顶公告失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取公告列表（管理员功能）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<SystemAnnouncement>> getAnnouncements(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(required = false) SystemAnnouncement.AnnouncementType type,
            @RequestParam(required = false) SystemAnnouncement.AnnouncementStatus status,
            @RequestParam(required = false) SystemAnnouncement.Priority priority,
            @RequestParam(required = false) SystemAnnouncement.TargetAudience targetAudience,
            @RequestParam(required = false) Integer publisherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            SystemAnnouncementService.AnnouncementQueryRequest request = new SystemAnnouncementService.AnnouncementQueryRequest();
            request.setTitle(title.isEmpty() ? null : title);
            request.setType(type);
            request.setStatus(status);
            request.setPriority(priority);
            request.setTargetAudience(targetAudience);
            request.setPublisherId(publisherId);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            PageResult<SystemAnnouncement> announcements = announcementService.getAnnouncements(request);
            return Result.success(announcements);
        } catch (Exception e) {
            logger.error("获取公告列表失败", e);
            return Result.error("获取公告列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取公告详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemAnnouncement> getAnnouncementById(@PathVariable Integer id) {
        try {
            return announcementService.getAnnouncementById(id);
        } catch (Exception e) {
            logger.error("获取公告详情失败: ID={}", id, e);
            return Result.error("获取公告详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建公告草稿
     */
    @PostMapping("/draft")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemAnnouncement> createAnnouncementDraft(@Valid @RequestBody SystemAnnouncementService.AnnouncementCreateRequest request) {
        try {
            return announcementService.createDraft(request);
        } catch (Exception e) {
            logger.error("创建公告草稿失败", e);
            return Result.error("创建公告草稿失败: " + e.getMessage());
        }
    }

    /**
     * 直接发布公告
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemAnnouncement> publishAnnouncement(@Valid @RequestBody SystemAnnouncementService.AnnouncementCreateRequest request) {
        try {
            return announcementService.publishAnnouncement(request);
        } catch (Exception e) {
            logger.error("发布公告失败", e);
            return Result.error("发布公告失败: " + e.getMessage());
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemAnnouncement> updateAnnouncement(@PathVariable Integer id, @Valid @RequestBody SystemAnnouncementService.AnnouncementUpdateRequest request) {
        try {
            return announcementService.updateAnnouncement(id, request);
        } catch (Exception e) {
            logger.error("更新公告失败: ID={}", id, e);
            return Result.error("更新公告失败: " + e.getMessage());
        }
    }

    /**
     * 发布草稿公告
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemAnnouncement> publishAnnouncement(@PathVariable Integer id) {
        try {
            return announcementService.publishAnnouncement(id);
        } catch (Exception e) {
            logger.error("发布公告失败: ID={}", id, e);
            return Result.error("发布公告失败: " + e.getMessage());
        }
    }

    /**
     * 取消公告
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SystemAnnouncement> cancelAnnouncement(@PathVariable Integer id) {
        try {
            return announcementService.cancelAnnouncement(id);
        } catch (Exception e) {
            logger.error("取消公告失败: ID={}", id, e);
            return Result.error("取消公告失败: " + e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteAnnouncement(@PathVariable Integer id) {
        try {
            announcementService.softDelete(id);
            return Result.success("公告删除成功");
        } catch (Exception e) {
            logger.error("删除公告失败: ID={}", id, e);
            return Result.error("删除公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getAnnouncementStatistics() {
        try {
            Map<String, Object> statistics = announcementService.getAnnouncementStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            logger.error("获取公告统计失败", e);
            return Result.error("获取公告统计失败: " + e.getMessage());
        }
    }

    /**
     * 搜索公告
     */
    @GetMapping("/search")
    public Result<List<SystemAnnouncement>> searchAnnouncements(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("搜索关键词不能为空");
            }

            List<SystemAnnouncement> announcements = announcementService.searchAnnouncements(keyword.trim());
            return Result.success(announcements);
        } catch (Exception e) {
            logger.error("搜索公告失败: keyword={}", keyword, e);
            return Result.error("搜索公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门公告
     */
    @GetMapping("/popular")
    public Result<List<SystemAnnouncement>> getPopularAnnouncements(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<SystemAnnouncement> announcements = announcementService.getPopularAnnouncements(limit);
            return Result.success(announcements);
        } catch (Exception e) {
            logger.error("获取热门公告失败", e);
            return Result.error("获取热门公告失败: " + e.getMessage());
        }
    }

    /**
     * 批量操作公告状态
     */
    @PutMapping("/batch/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> batchUpdateStatus(
            @RequestParam SystemAnnouncement.AnnouncementStatus status,
            @RequestParam List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的公告");
            }

            int successCount = 0;
            int totalCount = ids.size();
            List<String> failedItems = new ArrayList<>();

            for (Integer id : ids) {
                try {
                    switch (status) {
                        case PUBLISHED:
                            // 如果是发布操作，接收并处理返回结果
                            Result<SystemAnnouncement> publishResult = announcementService.publishAnnouncement(id);
                            if (publishResult.getCode() == 200) {
                                logger.info("发布公告成功: ID={}", id);
                            } else {
                                logger.error("发布公告失败: ID={}, 错误={}", id, publishResult.getMessage());
                                failedItems.add("ID " + id + ": " + publishResult.getMessage());
                                continue;
                            }
                            break;
                        case CANCELLED:
                            // 如果是取消操作，接收并处理返回结果
                            Result<SystemAnnouncement> cancelResult = announcementService.cancelAnnouncement(id);
                            if (cancelResult.getCode() == 200) {
                                logger.info("取消公告成功: ID={}", id);
                            } else {
                                logger.error("取消公告失败: ID={}, 错误={}", id, cancelResult.getMessage());
                                failedItems.add("ID " + id + ": " + cancelResult.getMessage());
                                continue;
                            }
                            break;
                        case EXPIRED:
                            // 如果是标记过期
                            Optional<SystemAnnouncement> announcementOpt = announcementService.findActiveById(id);
                            if (announcementOpt.isPresent()) {
                                SystemAnnouncement announcement = announcementOpt.get();
                                announcement.markAsExpired();
                                announcementService.update(announcement);
                                logger.info("标记公告过期成功: ID={}", id);
                            } else {
                                logger.error("标记公告过期失败: ID={}, 公告不存在", id);
                                failedItems.add("ID " + id + ": 公告不存在");
                                continue;
                            }
                            break;
                        default:
                            // 其他状态暂时不支持批量操作
                            logger.warn("不支持的批量操作状态: {}, ID={}", status, id);
                            failedItems.add("ID " + id + ": 不支持的状态 " + status);
                            continue;
                    }
                    successCount++;
                } catch (Exception e) {
                    logger.error("批量更新公告状态失败: ID={}, {}", id, e.getMessage());
                    failedItems.add("ID " + id + ": " + e.getMessage());
                }
            }

            String message;
            if (successCount == totalCount) {
                message = "成功更新 " + successCount + " 个公告状态";
            } else if (successCount > 0) {
                message = "部分成功：" + successCount + " 个成功，" + failedItems.size() + " 个失败";
                logger.warn("批量更新部分失败: {}", String.join("; ", failedItems));
            } else {
                message = "批量更新失败：" + String.join("; ", failedItems);
                return Result.error(message);
            }

            return Result.success(message);
        } catch (Exception e) {
            logger.error("批量更新公告状态失败", e);
            return Result.error("批量更新公告状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除公告
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> batchDeleteAnnouncements(@RequestParam List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的公告");
            }

            int successCount = 0;
            for (Integer id : ids) {
                try {
                    announcementService.softDelete(id);
                    successCount++;
                } catch (Exception e) {
                    logger.error("批量删除公告失败: ID={}, {}", id, e.getMessage());
                }
            }

            return Result.success("成功删除 " + successCount + " 个公告");
        } catch (Exception e) {
            logger.error("批量删除公告失败", e);
            return Result.error("批量删除公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告类型列表
     */
    @GetMapping("/types")
    public Result<List<Map<String, Object>>> getAnnouncementTypes() {
        try {
            List<Map<String, Object>> types = new java.util.ArrayList<>();
            for (SystemAnnouncement.AnnouncementType type : SystemAnnouncement.AnnouncementType.values()) {
                Map<String, Object> typeMap = new java.util.HashMap<>();
                typeMap.put("value", type.name());
                typeMap.put("label", typeMap.get("label") != null ? typeMap.get("label") : type.name());
                typeMap.put("description", type.name());
                types.add(typeMap);
            }
            return Result.success(types);
        } catch (Exception e) {
            logger.error("获取公告类型列表失败", e);
            return Result.error("获取公告类型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告状态列表
     */
    @GetMapping("/statuses")
    public Result<List<Map<String, Object>>> getAnnouncementStatuses() {
        try {
            List<Map<String, Object>> statuses = new java.util.ArrayList<>();
            for (SystemAnnouncement.AnnouncementStatus status : SystemAnnouncement.AnnouncementStatus.values()) {
                Map<String, Object> statusMap = new java.util.HashMap<>();
                statusMap.put("value", status.name());
                statusMap.put("label", status.name());
                statusMap.put("description", status.name());
                statuses.add(statusMap);
            }
            return Result.success(statuses);
        } catch (Exception e) {
            logger.error("获取公告状态列表失败", e);
            return Result.error("获取公告状态列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取优先级列表
     */
    @GetMapping("/priorities")
    public Result<List<Map<String, Object>>> getAnnouncementPriorities() {
        try {
            List<Map<String, Object>> priorities = new java.util.ArrayList<>();
            for (SystemAnnouncement.Priority priority : SystemAnnouncement.Priority.values()) {
                Map<String, Object> priorityMap = new java.util.HashMap<>();
                priorityMap.put("value", priority.name());
                priorityMap.put("label", priority.name());
                priorityMap.put("description", priority.name());
                priorityMap.put("level", getPriorityLevel(priority));
                priorities.add(priorityMap);
            }
            return Result.success(priorities);
        } catch (Exception e) {
            logger.error("获取公告优先级列表失败", e);
            return Result.error("获取公告优先级列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取目标受众列表
     */
    @GetMapping("/audiences")
    public Result<List<Map<String, Object>>> getTargetAudiences() {
        try {
            List<Map<String, Object>> audiences = new java.util.ArrayList<>();
            for (SystemAnnouncement.TargetAudience audience : SystemAnnouncement.TargetAudience.values()) {
                Map<String, Object> audienceMap = new java.util.HashMap<>();
                audienceMap.put("value", audience.name());
                audienceMap.put("label", audience.name());
                audienceMap.put("description", audience.name());
                audiences.add(audienceMap);
            }
            return Result.success(audiences);
        } catch (Exception e) {
            logger.error("获取目标受众列表失败", e);
            return Result.error("获取目标受众列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取显示类型列表
     */
    @GetMapping("/display-types")
    public Result<List<Map<String, Object>>> getDisplayTypes() {
        try {
            List<Map<String, Object>> displayTypes = new java.util.ArrayList<>();
            for (SystemAnnouncement.DisplayType displayType : SystemAnnouncement.DisplayType.values()) {
                Map<String, Object> displayTypeMap = new java.util.HashMap<>();
                displayTypeMap.put("value", displayType.name());
                displayTypeMap.put("label", displayType.name());
                displayTypeMap.put("description", displayType.name());
                displayTypes.add(displayTypeMap);
            }
            return Result.success(displayTypes);
        } catch (Exception e) {
            logger.error("获取显示类型列表失败", e);
            return Result.error("获取显示类型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户角色列表（用于目标受众选择）
     */
    @GetMapping("/roles")
    public Result<List<Map<String, Object>>> getUserRoles() {
        try {
            List<Map<String, Object>> roles = new java.util.ArrayList<>();

            // 添加系统中的角色
            Map<String, Object> adminRole = new java.util.HashMap<>();
            adminRole.put("value", "ADMIN");
            adminRole.put("label", "管理员");
            adminRole.put("description", "系统管理员，拥有所有权限");
            roles.add(adminRole);

            Map<String, Object> warehouseKeeperRole = new java.util.HashMap<>();
            warehouseKeeperRole.put("value", "WAREHOUSE_KEEPER");
            warehouseKeeperRole.put("label", "仓库管理员");
            warehouseKeeperRole.put("description", "仓库管理员，负责仓库管理");
            roles.add(warehouseKeeperRole);

            Map<String, Object> employeeRole = new java.util.HashMap<>();
            employeeRole.put("value", "EMPLOYEE");
            employeeRole.put("label", "员工");
            employeeRole.put("description", "普通员工，基础操作权限");
            roles.add(employeeRole);

            return Result.success(roles);
        } catch (Exception e) {
            logger.error("获取用户角色列表失败", e);
            return Result.error("获取用户角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取优先级数值
     */
    private Integer getPriorityLevel(SystemAnnouncement.Priority priority) {
        switch (priority) {
            case URGENT:
                return 4;
            case HIGH:
                return 3;
            case NORMAL:
                return 2;
            case LOW:
                return 1;
            default:
                return 2;
        }
    }
}