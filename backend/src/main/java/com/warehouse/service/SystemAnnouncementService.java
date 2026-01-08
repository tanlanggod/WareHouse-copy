package com.warehouse.service;

import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.SystemAnnouncement;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.SystemAnnouncementRepository;
import com.warehouse.repository.UserRepository;
import com.warehouse.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统公告业务服务
 */
@Service
@Transactional
public class SystemAnnouncementService extends BaseService<SystemAnnouncement, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SystemAnnouncementService.class);

    @Autowired
    private SystemAnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected BaseRepository<SystemAnnouncement, Integer> getRepository() {
        return announcementRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    /**
     * 创建公告草稿
     */
    public Result<SystemAnnouncement> createDraft(AnnouncementCreateRequest request) {
        try {
            // 检查标题是否重复
            if (announcementRepository.existsByTitleAndIsDeleted(request.getTitle(), 0)) {
                return Result.error("公告标题已存在");
            }

            SystemAnnouncement announcement = new SystemAnnouncement();
            announcement.setTitle(request.getTitle());
            announcement.setContent(request.getContent());
            announcement.setType(request.getType());
            announcement.setPriority(request.getPriority());
            announcement.setTargetAudience(request.getTargetAudience());
            announcement.setDisplayType(request.getDisplayType());
            announcement.setIsPinned(request.getIsPinned() != null ? request.getIsPinned() : false);
            announcement.setEffectiveStartTime(request.getEffectiveStartTime());
            announcement.setEffectiveEndTime(request.getEffectiveEndTime());
            announcement.setAttachmentUrl(request.getAttachmentUrl());
            announcement.setAttachmentName(request.getAttachmentName());

            // 设置目标角色
            if (request.getTargetAudience() == SystemAnnouncement.TargetAudience.ROLE_BASED
                    && request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
                announcement.setTargetRolesList(request.getTargetRoles());
            }

            // 草稿状态不需要设置发布时间
            announcement.setStatus(SystemAnnouncement.AnnouncementStatus.DRAFT);

            SystemAnnouncement savedAnnouncement = create(announcement);
            logger.info("创建公告草稿成功: ID={}, 标题={}", savedAnnouncement.getId(), savedAnnouncement.getTitle());

            return Result.success("公告草稿创建成功", savedAnnouncement);
        } catch (Exception e) {
            logger.error("创建公告草稿失败: {}", e.getMessage(), e);
            return Result.error("创建公告草稿失败: " + e.getMessage());
        }
    }

    /**
     * 直接发布公告
     */
    public Result<SystemAnnouncement> publishAnnouncement(AnnouncementCreateRequest request) {
        try {
            // 检查标题是否重复
            if (announcementRepository.existsByTitleAndIsDeleted(request.getTitle(), 0)) {
                return Result.error("公告标题已存在");
            }

            SystemAnnouncement announcement = new SystemAnnouncement();
            announcement.setTitle(request.getTitle());
            announcement.setContent(request.getContent());
            announcement.setType(request.getType());
            announcement.setPriority(request.getPriority());
            announcement.setTargetAudience(request.getTargetAudience());
            announcement.setDisplayType(request.getDisplayType());
            announcement.setIsPinned(request.getIsPinned() != null ? request.getIsPinned() : false);
            announcement.setEffectiveStartTime(request.getEffectiveStartTime());
            announcement.setEffectiveEndTime(request.getEffectiveEndTime());
            announcement.setAttachmentUrl(request.getAttachmentUrl());
            announcement.setAttachmentName(request.getAttachmentName());

            // 设置目标角色
            if (request.getTargetAudience() == SystemAnnouncement.TargetAudience.ROLE_BASED
                    && request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
                announcement.setTargetRolesList(request.getTargetRoles());
            }

            // 直接发布
            announcement.publish(getCurrentUserId());

            SystemAnnouncement savedAnnouncement = create(announcement);
            logger.info("发布公告成功: ID={}, 标题={}, 发布人={}",
                    savedAnnouncement.getId(), savedAnnouncement.getTitle(), getCurrentUserId());

            return Result.success("公告发布成功", savedAnnouncement);
        } catch (Exception e) {
            logger.error("发布公告失败: {}", e.getMessage(), e);
            return Result.error("发布公告失败: " + e.getMessage());
        }
    }

    /**
     * 更新公告
     */
    public Result<SystemAnnouncement> updateAnnouncement(Integer id, AnnouncementUpdateRequest request) {
        try {
            Optional<SystemAnnouncement> announcementOpt = findByIdNotDeleted(id);
            if (!announcementOpt.isPresent()) {
                return Result.error("公告不存在");
            }

            SystemAnnouncement announcement = announcementOpt.get();

            // 已发布的公告只能修改有限字段
            if (announcement.getStatus() == SystemAnnouncement.AnnouncementStatus.PUBLISHED) {
                // 已发布的公告只能修改显示方式、生效时间、置顶状态
                announcement.setDisplayType(request.getDisplayType());
                announcement.setEffectiveStartTime(request.getEffectiveStartTime());
                announcement.setEffectiveEndTime(request.getEffectiveEndTime());
                announcement.setIsPinned(request.getIsPinned());

                // 如果修改了目标受众
                if (request.getTargetAudience() != null) {
                    announcement.setTargetAudience(request.getTargetAudience());
                    if (request.getTargetAudience() == SystemAnnouncement.TargetAudience.ROLE_BASED
                            && request.getTargetRoles() != null) {
                        announcement.setTargetRolesList(request.getTargetRoles());
                    }
                }

                logger.info("更新已发布公告: ID={}, 修改了有限字段", id);
            } else {
                // 草稿状态可以修改除ID外的所有字段
                String oldTitle = announcement.getTitle();
                BeanUtils.copyProperties(request, announcement, "id", "createdAt", "creatorId", "publishTime", "readCount");

                // 重新设置目标角色
                if (request.getTargetRoles() != null) {
                    announcement.setTargetRolesList(request.getTargetRoles());
                }

                logger.info("更新公告草稿: ID={}, 标题: {} -> {}", id, oldTitle, announcement.getTitle());
            }

            SystemAnnouncement updatedAnnouncement = update(announcement);
            return Result.success("公告更新成功", updatedAnnouncement);
        } catch (Exception e) {
            logger.error("更新公告失败: ID={}, {}", id, e.getMessage(), e);
            return Result.error("更新公告失败: " + e.getMessage());
        }
    }

    /**
     * 发布草稿公告
     */
    public Result<SystemAnnouncement> publishAnnouncement(Integer id) {
        try {
            Optional<SystemAnnouncement> announcementOpt = findByIdNotDeleted(id);
            if (!announcementOpt.isPresent()) {
                return Result.error("公告不存在");
            }

            SystemAnnouncement announcement = announcementOpt.get();
            if (announcement.getStatus() != SystemAnnouncement.AnnouncementStatus.DRAFT) {
                return Result.error("只有草稿状态的公告才能发布");
            }

            announcement.publish(getCurrentUserId());
            SystemAnnouncement publishedAnnouncement = update(announcement);

            logger.info("发布公告成功: ID={}, 标题={}, 发布人={}",
                    publishedAnnouncement.getId(), publishedAnnouncement.getTitle(), getCurrentUserId());

            return Result.success("公告发布成功", publishedAnnouncement);
        } catch (Exception e) {
            logger.error("发布公告失败: ID={}, {}", id, e.getMessage(), e);
            return Result.error("发布公告失败: " + e.getMessage());
        }
    }

    /**
     * 取消公告
     */
    public Result<SystemAnnouncement> cancelAnnouncement(Integer id) {
        try {
            Optional<SystemAnnouncement> announcementOpt = findByIdNotDeleted(id);
            if (!announcementOpt.isPresent()) {
                return Result.error("公告不存在");
            }

            SystemAnnouncement announcement = announcementOpt.get();
            if (announcement.getStatus() != SystemAnnouncement.AnnouncementStatus.PUBLISHED) {
                return Result.error("只有已发布的公告才能取消");
            }

            announcement.cancel();
            SystemAnnouncement cancelledAnnouncement = update(announcement);

            logger.info("取消公告成功: ID={}, 标题={}", cancelledAnnouncement.getId(), cancelledAnnouncement.getTitle());

            return Result.success("公告取消成功", cancelledAnnouncement);
        } catch (Exception e) {
            logger.error("取消公告失败: ID={}, {}", id, e.getMessage(), e);
            return Result.error("取消公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户可见的公告列表
     */
//    @Cacheable(value = "visibleAnnouncements", key = "#userRole", unless = "#result.size() > 100")
    public List<SystemAnnouncement> getVisibleAnnouncements(String userRole) {
        try {
            LocalDateTime now = LocalDateTime.now();

            List<SystemAnnouncement> announcements;
            if (userRole != null && !userRole.trim().isEmpty()) {
                announcements = announcementRepository.findAnnouncementsForRole(now, userRole);
            } else {
                announcements = announcementRepository.findActiveAnnouncements(now);
            }

            // 过滤出用户确实有权限查看的公告
            return announcements.stream()
                    .filter(announcement -> announcement.isAccessibleByRole(userRole))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("获取可见公告失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取置顶公告
     */
//    @Cacheable(value = "pinnedAnnouncements", unless = "#result.size() > 50")
    public List<SystemAnnouncement> getPinnedAnnouncements() {
        try {
            return announcementRepository.findPinnedAnnouncements(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("获取置顶公告失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 分页查询公告（管理员使用）
     */
    public PageResult<SystemAnnouncement> getAnnouncements(AnnouncementQueryRequest request) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

            Page<SystemAnnouncement> announcementPage = announcementRepository.findAnnouncementsWithFilters(
                    request.getTitle(),
                    request.getType(),
                    request.getStatus(),
                    request.getPriority(),
                    request.getTargetAudience(),
                    request.getPublisherId(),
                    pageable
            );

            return new PageResult<>(
                    announcementPage.getTotalElements(),
                    announcementPage.getContent()
            );
        } catch (Exception e) {
            logger.error("获取公告列表失败: {}", e.getMessage(), e);
            return new PageResult<>(0L, new ArrayList<>());
        }
    }

    /**
     * 根据ID获取公告详情
     */
    public Result<SystemAnnouncement> getAnnouncementById(Integer id) {
        try {
            Optional<SystemAnnouncement> announcementOpt = findByIdNotDeleted(id);
            if (!announcementOpt.isPresent()) {
                return Result.error("公告不存在");
            }

            // 增加阅读次数（仅对已发布的公告）
            SystemAnnouncement announcement = announcementOpt.get();
            if (announcement.getStatus() == SystemAnnouncement.AnnouncementStatus.PUBLISHED) {
                announcement.incrementReadCount();
                announcementRepository.save(announcement);
            }

            return Result.success(announcement);
        } catch (Exception e) {
            logger.error("获取公告详情失败: ID={}, {}", id, e.getMessage(), e);
            return Result.error("获取公告详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告统计信息
     */
    public Map<String, Object> getAnnouncementStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总公告数
            long totalCount = announcementRepository.countAllAnnouncements();
            statistics.put("totalCount", totalCount);

            // 各状态统计
            List<Object[]> statusStats = announcementRepository.countByStatus();
            Map<String, Long> statusCounts = new HashMap<>();
            for (Object[] stat : statusStats) {
                SystemAnnouncement.AnnouncementStatus status = (SystemAnnouncement.AnnouncementStatus) stat[0];
                Long count = (Long) stat[1];
                statusCounts.put(status.name(), count);
            }
            statistics.put("statusCounts", statusCounts);

            // 各类型统计
            List<Object[]> typeStats = announcementRepository.countByType();
            Map<String, Long> typeCounts = new HashMap<>();
            for (Object[] stat : typeStats) {
                SystemAnnouncement.AnnouncementType type = (SystemAnnouncement.AnnouncementType) stat[0];
                Long count = (Long) stat[1];
                typeCounts.put(type.name(), count);
            }
            statistics.put("typeCounts", typeCounts);

            // 各优先级统计
            List<Object[]> priorityStats = announcementRepository.countByPriority();
            Map<String, Long> priorityCounts = new HashMap<>();
            for (Object[] stat : priorityStats) {
                SystemAnnouncement.Priority priority = (SystemAnnouncement.Priority) stat[0];
                Long count = (Long) stat[1];
                priorityCounts.put(priority.name(), count);
            }
            statistics.put("priorityCounts", priorityCounts);

            // 最近7天发布的公告数
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            long recentCount = announcementRepository.countRecentAnnouncements(sevenDaysAgo);
            statistics.put("recentCount", recentCount);

            return statistics;
        } catch (Exception e) {
            logger.error("获取公告统计失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 定时任务：自动标记过期公告
     */
    @Scheduled(cron = "0 0 * * * ?") // 每天凌晨执行
    @CacheEvict(value = {"visibleAnnouncements", "pinnedAnnouncements"}, allEntries = true)
    public void expireAnnouncements() {
        try {
            List<SystemAnnouncement> expiredAnnouncements = announcementRepository.findAnnouncementsToExpire(LocalDateTime.now());

            for (SystemAnnouncement announcement : expiredAnnouncements) {
                announcement.markAsExpired();
                announcementRepository.save(announcement);
                logger.info("自动标记过期公告: ID={}, 标题={}", announcement.getId(), announcement.getTitle());
            }

            if (!expiredAnnouncements.isEmpty()) {
                logger.info("自动标记了 {} 个过期公告", expiredAnnouncements.size());
            }
        } catch (Exception e) {
            logger.error("自动标记过期公告失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 搜索公告
     */
    public List<SystemAnnouncement> searchAnnouncements(String keyword) {
        try {
            return announcementRepository.findByKeyword(keyword);
        } catch (Exception e) {
            logger.error("搜索公告失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取热门公告（按阅读次数排序）
     */
    public List<SystemAnnouncement> getPopularAnnouncements(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return announcementRepository.findTopReadAnnouncements(pageable);
        } catch (Exception e) {
            logger.error("获取热门公告失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ==================== 内部请求类 ====================

    /**
     * 公告创建请求
     */
    public static class AnnouncementCreateRequest {
        private String title;
        private String content;
        private SystemAnnouncement.AnnouncementType type;
        private SystemAnnouncement.Priority priority = SystemAnnouncement.Priority.NORMAL;
        private SystemAnnouncement.TargetAudience targetAudience = SystemAnnouncement.TargetAudience.ALL;
        private List<String> targetRoles;
        private SystemAnnouncement.DisplayType displayType = SystemAnnouncement.DisplayType.NOTIFICATION;
        private Boolean isPinned = false;
        private LocalDateTime effectiveStartTime;
        private LocalDateTime effectiveEndTime;
        private String attachmentUrl;
        private String attachmentName;

        // getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public SystemAnnouncement.AnnouncementType getType() { return type; }
        public void setType(SystemAnnouncement.AnnouncementType type) { this.type = type; }
        public SystemAnnouncement.Priority getPriority() { return priority; }
        public void setPriority(SystemAnnouncement.Priority priority) { this.priority = priority; }
        public SystemAnnouncement.TargetAudience getTargetAudience() { return targetAudience; }
        public void setTargetAudience(SystemAnnouncement.TargetAudience targetAudience) { this.targetAudience = targetAudience; }
        public List<String> getTargetRoles() { return targetRoles; }
        public void setTargetRoles(List<String> targetRoles) { this.targetRoles = targetRoles; }
        public SystemAnnouncement.DisplayType getDisplayType() { return displayType; }
        public void setDisplayType(SystemAnnouncement.DisplayType displayType) { this.displayType = displayType; }
        public Boolean getIsPinned() { return isPinned; }
        public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }
        public LocalDateTime getEffectiveStartTime() { return effectiveStartTime; }
        public void setEffectiveStartTime(LocalDateTime effectiveStartTime) { this.effectiveStartTime = effectiveStartTime; }
        public LocalDateTime getEffectiveEndTime() { return effectiveEndTime; }
        public void setEffectiveEndTime(LocalDateTime effectiveEndTime) { this.effectiveEndTime = effectiveEndTime; }
        public String getAttachmentUrl() { return attachmentUrl; }
        public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
        public String getAttachmentName() { return attachmentName; }
        public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    }

    /**
     * 公告更新请求
     */
    public static class AnnouncementUpdateRequest {
        private String title;
        private String content;
        private SystemAnnouncement.AnnouncementType type;
        private SystemAnnouncement.Priority priority;
        private SystemAnnouncement.TargetAudience targetAudience;
        private List<String> targetRoles;
        private SystemAnnouncement.DisplayType displayType;
        private Boolean isPinned;
        private LocalDateTime effectiveStartTime;
        private LocalDateTime effectiveEndTime;
        private String attachmentUrl;
        private String attachmentName;

        // getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public SystemAnnouncement.AnnouncementType getType() { return type; }
        public void setType(SystemAnnouncement.AnnouncementType type) { this.type = type; }
        public SystemAnnouncement.Priority getPriority() { return priority; }
        public void setPriority(SystemAnnouncement.Priority priority) { this.priority = priority; }
        public SystemAnnouncement.TargetAudience getTargetAudience() { return targetAudience; }
        public void setTargetAudience(SystemAnnouncement.TargetAudience targetAudience) { this.targetAudience = targetAudience; }
        public List<String> getTargetRoles() { return targetRoles; }
        public void setTargetRoles(List<String> targetRoles) { this.targetRoles = targetRoles; }
        public SystemAnnouncement.DisplayType getDisplayType() { return displayType; }
        public void setDisplayType(SystemAnnouncement.DisplayType displayType) { this.displayType = displayType; }
        public Boolean getIsPinned() { return isPinned; }
        public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }
        public LocalDateTime getEffectiveStartTime() { return effectiveStartTime; }
        public void setEffectiveStartTime(LocalDateTime effectiveStartTime) { this.effectiveStartTime = effectiveStartTime; }
        public LocalDateTime getEffectiveEndTime() { return effectiveEndTime; }
        public void setEffectiveEndTime(LocalDateTime effectiveEndTime) { this.effectiveEndTime = effectiveEndTime; }
        public String getAttachmentUrl() { return attachmentUrl; }
        public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
        public String getAttachmentName() { return attachmentName; }
        public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    }

    /**
     * 公告查询请求
     */
    public static class AnnouncementQueryRequest {
        private String title;
        private SystemAnnouncement.AnnouncementType type;
        private SystemAnnouncement.AnnouncementStatus status;
        private SystemAnnouncement.Priority priority;
        private SystemAnnouncement.TargetAudience targetAudience;
        private Integer publisherId;
        private int page = 1;
        private int size = 10;
        private String sortBy = "createdAt";
        private String sortDirection = "DESC";

        // getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public SystemAnnouncement.AnnouncementType getType() { return type; }
        public void setType(SystemAnnouncement.AnnouncementType type) { this.type = type; }
        public SystemAnnouncement.AnnouncementStatus getStatus() { return status; }
        public void setStatus(SystemAnnouncement.AnnouncementStatus status) { this.status = status; }
        public SystemAnnouncement.Priority getPriority() { return priority; }
        public void setPriority(SystemAnnouncement.Priority priority) { this.priority = priority; }
        public SystemAnnouncement.TargetAudience getTargetAudience() { return targetAudience; }
        public void setTargetAudience(SystemAnnouncement.TargetAudience targetAudience) { this.targetAudience = targetAudience; }
        public Integer getPublisherId() { return publisherId; }
        public void setPublisherId(Integer publisherId) { this.publisherId = publisherId; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        public String getSortDirection() { return sortDirection; }
        public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
    }

    /**
     * 根据ID查找活跃的公告（排除草稿）
     */
    public Optional<SystemAnnouncement> findActiveById(Integer id) {
        return announcementRepository.findById(id)
            .filter(announcement -> !announcement.isDeleted())
            .filter(announcement -> announcement.getStatus() != SystemAnnouncement.AnnouncementStatus.DRAFT);
    }

    /**
     * 根据ID查找未删除的公告（包括所有状态）
     */
    public Optional<SystemAnnouncement> findByIdNotDeleted(Integer id) {
        return announcementRepository.findById(id)
            .filter(announcement -> !announcement.isDeleted());
    }
}