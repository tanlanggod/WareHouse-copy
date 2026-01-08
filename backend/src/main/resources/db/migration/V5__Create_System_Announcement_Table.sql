-- 创建系统公告表
-- 为仓库管理系统添加系统公告功能

-- 创建系统公告表
CREATE TABLE `system_announcement` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `announcement_type` varchar(20) NOT NULL COMMENT '公告类型：SYSTEM, MAINTENANCE, FEATURE, HOLIDAY, URGENT',
  `priority` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：LOW, NORMAL, HIGH, URGENT',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT, PUBLISHED, EXPIRED, CANCELLED',
  `target_audience` varchar(20) NOT NULL DEFAULT 'ALL' COMMENT '目标受众：ALL, ROLE_BASED, DEPARTMENT, SPECIFIC_USERS',
  `target_roles` varchar(500) DEFAULT NULL COMMENT '目标角色（JSON数组格式）',
  `display_type` varchar(20) NOT NULL DEFAULT 'NOTIFICATION' COMMENT '显示类型：NOTIFICATION, POPUP, BANNER, SIDEBAR',
  `is_pinned` tinyint(1) DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `effective_start_time` datetime DEFAULT NULL COMMENT '生效开始时间',
  `effective_end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
  `read_count` int DEFAULT 0 COMMENT '阅读次数',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `publisher_id` int DEFAULT NULL COMMENT '发布人ID',
  `attachment_url` varchar(500) DEFAULT NULL COMMENT '附件URL',
  `attachment_name` varchar(200) DEFAULT NULL COMMENT '附件名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `creator_id` int DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`id`),
  KEY `idx_announcement_status` (`status`),
  KEY `idx_announcement_target` (`target_audience`),
  KEY `idx_announcement_effective_time` (`effective_start_time`, `effective_end_time`),
  KEY `idx_announcement_created_at` (`created_at`),
  KEY `idx_announcement_publish_time` (`publish_time`),
  KEY `idx_announcement_priority` (`priority`),
  KEY `idx_announcement_type` (`announcement_type`),
  KEY `idx_announcement_pinned` (`is_pinned`),
  KEY `fk_announcement_publisher` (`publisher_id`),
  KEY `fk_announcement_creator` (`creator_id`),
  KEY `fk_announcement_updater` (`updater_id`),
  KEY `fk_announcement_deleter` (`deleter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 添加外键约束
ALTER TABLE `system_announcement`
ADD CONSTRAINT `fk_announcement_publisher`
FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
ADD CONSTRAINT `fk_announcement_creator`
FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
ADD CONSTRAINT `fk_announcement_updater`
FOREIGN KEY (`updater_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
ADD CONSTRAINT `fk_announcement_deleter`
FOREIGN KEY (`deleter_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 添加注释
ALTER TABLE `system_announcement` COMMENT = '系统公告表';

-- 插入示例数据（可选）
INSERT INTO `system_announcement` (
    `title`, `content`, `announcement_type`, `priority`, `status`, `target_audience`,
    `display_type`, `is_pinned`, `effective_start_time`, `effective_end_time`,
    `publish_time`, `publisher_id`, `creator_id`, `created_at`
) VALUES
(
    '欢迎使用仓库管理系统',
    '欢迎使用仓库管理系统！本系统提供完整的仓库管理功能，包括商品管理、入库出库、库存管理、系统配置等功能。如有问题请联系系统管理员。',
    'SYSTEM', 'NORMAL', 'PUBLISHED', 'ALL',
    'NOTIFICATION', 1,
    NOW(), NULL,
    NOW(), 1, 1, NOW()
);