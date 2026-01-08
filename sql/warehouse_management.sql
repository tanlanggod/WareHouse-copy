/*
 Navicat Premium Data Transfer

 Source Server         : Warehouse
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : warehouse_management

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 08/01/2026 14:23:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for approval_record
-- ----------------------------
DROP TABLE IF EXISTS `approval_record`;
CREATE TABLE `approval_record`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `creator_id` int NULL DEFAULT NULL,
  `deleted_at` datetime(6) NULL DEFAULT NULL,
  `deleter_id` int NULL DEFAULT NULL,
  `is_deleted` tinyint NULL DEFAULT 0,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `updater_id` int NULL DEFAULT NULL,
  `approval_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `approval_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `approval_time` datetime(6) NULL DEFAULT NULL,
  `approver_id` int NULL DEFAULT NULL,
  `business_id` int NOT NULL,
  `business_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `flow_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `step_order` int NULL DEFAULT NULL,
  `submit_time` datetime(6) NULL DEFAULT NULL,
  `submitter_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKpdnrpkjxnelt9v455x412i62u`(`approver_id` ASC) USING BTREE,
  INDEX `FKfwki2nxy7tocmwwst0n3ldfs9`(`submitter_id` ASC) USING BTREE,
  CONSTRAINT `FKfwki2nxy7tocmwwst0n3ldfs9` FOREIGN KEY (`submitter_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKpdnrpkjxnelt9v455x412i62u` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类别名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类别描述',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_category_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_category_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_category_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_category_deleted_created`(`is_deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_category_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_category_updater_foreign`(`updater_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品类别表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for customer
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户名称',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_customer_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_customer_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_customer_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_customer_deleted_created`(`is_deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_customer_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_customer_updater_foreign`(`updater_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for inbound
-- ----------------------------
DROP TABLE IF EXISTS `inbound`;
CREATE TABLE `inbound`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `inbound_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入库单号',
  `product_id` int NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '入库数量',
  `supplier_id` int NULL DEFAULT NULL COMMENT '供应商ID',
  `inbound_date` datetime NOT NULL COMMENT '入库时间',
  `operator_id` int NULL DEFAULT NULL COMMENT '操作员ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  `approval_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approval_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approval_time` datetime(6) NULL DEFAULT NULL,
  `flow_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approver_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `inbound_no`(`inbound_no` ASC) USING BTREE,
  INDEX `supplier_id`(`supplier_id` ASC) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_inbound_no`(`inbound_no` ASC) USING BTREE,
  INDEX `idx_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_inbound_date`(`inbound_date` ASC) USING BTREE,
  INDEX `idx_inbound_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_inbound_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_inbound_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_inbound_updated`(`updated_at` ASC) USING BTREE,
  INDEX `idx_inbound_product_deleted_created`(`product_id` ASC, `is_deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_inbound_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_inbound_updater_foreign`(`updater_id` ASC) USING BTREE,
  INDEX `FKjvif47b8q58dl2v42deofm6r9`(`approver_id` ASC) USING BTREE,
  CONSTRAINT `FKjvif47b8q58dl2v42deofm6r9` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `inbound_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `inbound_ibfk_2` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `inbound_ibfk_3` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_success` bit(1) NOT NULL,
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `resource_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `response_status` int NOT NULL,
  `response_time` bigint NOT NULL,
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `operation_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2777 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for outbound
-- ----------------------------
DROP TABLE IF EXISTS `outbound`;
CREATE TABLE `outbound`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `outbound_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出库单号',
  `product_id` int NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '出库数量',
  `customer_id` int NULL DEFAULT NULL COMMENT '客户ID',
  `outbound_date` datetime NOT NULL COMMENT '出库时间',
  `operator_id` int NULL DEFAULT NULL COMMENT '操作员ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  `approval_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approval_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approval_time` datetime(6) NULL DEFAULT NULL,
  `flow_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approver_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `outbound_no`(`outbound_no` ASC) USING BTREE,
  INDEX `customer_id`(`customer_id` ASC) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_outbound_no`(`outbound_no` ASC) USING BTREE,
  INDEX `idx_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_outbound_date`(`outbound_date` ASC) USING BTREE,
  INDEX `idx_outbound_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_outbound_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_outbound_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_outbound_updated`(`updated_at` ASC) USING BTREE,
  INDEX `idx_outbound_product_deleted_created`(`product_id` ASC, `is_deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_outbound_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_outbound_updater_foreign`(`updater_id` ASC) USING BTREE,
  INDEX `FKeqp0xaoqj21ija2tgsvj3y7vd`(`approver_id` ASC) USING BTREE,
  CONSTRAINT `FKeqp0xaoqj21ija2tgsvj3y7vd` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `outbound_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `outbound_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `outbound_ibfk_3` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `category_id` int NULL DEFAULT NULL COMMENT '商品类别ID',
  `supplier_id` int NULL DEFAULT NULL COMMENT '供应商ID',
  `price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '商品单价',
  `stock_qty` int NULL DEFAULT 0 COMMENT '当前库存量',
  `min_stock` int NULL DEFAULT 0 COMMENT '库存警戒线',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '件' COMMENT '单位',
  `barcode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '条形码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  `version` bigint NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_supplier`(`supplier_id` ASC) USING BTREE,
  INDEX `idx_product_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_product_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_product_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_product_deleted_created`(`is_deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_product_category_status_deleted`(`category_id` ASC, `status` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_product_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_product_updater_foreign`(`updater_id` ASC) USING BTREE,
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `product_ibfk_2` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for stock_adjustment
-- ----------------------------
DROP TABLE IF EXISTS `stock_adjustment`;
CREATE TABLE `stock_adjustment`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL COMMENT '商品ID',
  `adjustment_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调整类型：INCREASE-增加, DECREASE-减少',
  `quantity` int NOT NULL COMMENT '调整数量',
  `before_qty` int NOT NULL COMMENT '调整前库存',
  `after_qty` int NOT NULL COMMENT '调整后库存',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调整理由',
  `operator_id` int NULL DEFAULT NULL COMMENT '操作员ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  `approval_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approval_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approval_time` datetime(6) NULL DEFAULT NULL,
  `flow_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approver_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_product`(`product_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_stock_adjustment_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_stock_adjustment_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_stock_adjustment_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_stock_adjustment_updated`(`updated_at` ASC) USING BTREE,
  INDEX `idx_stock_adjustment_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_stock_adjustment_updater_foreign`(`updater_id` ASC) USING BTREE,
  INDEX `FKc43itbvp24v8ikvtskw6njfa5`(`approver_id` ASC) USING BTREE,
  CONSTRAINT `FKc43itbvp24v8ikvtskw6njfa5` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `stock_adjustment_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `stock_adjustment_ibfk_2` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存调整记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplier
-- ----------------------------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_supplier_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_supplier_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_supplier_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_supplier_deleted_created`(`is_deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_supplier_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_supplier_updater_foreign`(`updater_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_announcement
-- ----------------------------
DROP TABLE IF EXISTS `system_announcement`;
CREATE TABLE `system_announcement`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `announcement_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型：SYSTEM, MAINTENANCE, FEATURE, HOLIDAY, URGENT',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：LOW, NORMAL, HIGH, URGENT',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT, PUBLISHED, EXPIRED, CANCELLED',
  `target_audience` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ALL' COMMENT '目标受众：ALL, ROLE_BASED, DEPARTMENT, SPECIFIC_USERS',
  `target_roles` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标角色（JSON数组格式）',
  `display_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOTIFICATION' COMMENT '显示类型：NOTIFICATION, POPUP, BANNER, SIDEBAR',
  `is_pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `effective_start_time` datetime NULL DEFAULT NULL COMMENT '生效开始时间',
  `effective_end_time` datetime NULL DEFAULT NULL COMMENT '生效结束时间',
  `read_count` int NULL DEFAULT 0 COMMENT '阅读次数',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `publisher_id` int NULL DEFAULT NULL COMMENT '发布人ID',
  `attachment_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件URL',
  `attachment_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_announcement_status`(`status` ASC) USING BTREE,
  INDEX `idx_announcement_target`(`target_audience` ASC) USING BTREE,
  INDEX `idx_announcement_effective_time`(`effective_start_time` ASC, `effective_end_time` ASC) USING BTREE,
  INDEX `idx_announcement_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_announcement_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `idx_announcement_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_announcement_type`(`announcement_type` ASC) USING BTREE,
  INDEX `idx_announcement_pinned`(`is_pinned` ASC) USING BTREE,
  INDEX `fk_announcement_publisher`(`publisher_id` ASC) USING BTREE,
  INDEX `fk_announcement_creator`(`creator_id` ASC) USING BTREE,
  INDEX `fk_announcement_updater`(`updater_id` ASC) USING BTREE,
  INDEX `fk_announcement_deleter`(`deleter_id` ASC) USING BTREE,
  CONSTRAINT `fk_announcement_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_announcement_deleter` FOREIGN KEY (`deleter_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_announcement_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_announcement_updater` FOREIGN KEY (`updater_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `config_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_system_config_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_system_config_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_system_config_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_system_config_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_system_config_updater_foreign`(`updater_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'EMPLOYEE' COMMENT '用户角色：ADMIN-管理员, WAREHOUSE_KEEPER-库管员, EMPLOYEE-普通员工',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `login_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone_verification_time` datetime(6) NULL DEFAULT NULL,
  `phone_verified` tinyint(1) NULL DEFAULT 0,
  `wechat_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `wechat_nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `wechat_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `wechat_unionid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `UK_1kosdkbcx2rj3mit4mnd5vsym`(`wechat_openid` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_user_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_user_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_user_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_user_deleted_created`(`is_deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_user_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_user_updater_foreign`(`updater_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for warehouse
-- ----------------------------
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库地址',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库编号',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_warehouse_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_warehouse_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_warehouse_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_warehouse_deleted_created`(`is_deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_warehouse_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_warehouse_updater_foreign`(`updater_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for warehouse_location
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_location`;
CREATE TABLE `warehouse_location`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `warehouse_id` int NOT NULL COMMENT '仓库ID',
  `rack_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '货架号',
  `shelf_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '层号',
  `bin_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '货位号',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-可用，0-禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updater_id` int NULL DEFAULT NULL COMMENT '修改人ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleter_id` int NULL DEFAULT NULL COMMENT '删除人ID',
  `version` bigint NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_location`(`rack_number` ASC, `shelf_level` ASC, `bin_number` ASC) USING BTREE,
  INDEX `idx_warehouse_location_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_warehouse_location_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_warehouse_location_updater`(`updater_id` ASC) USING BTREE,
  INDEX `idx_warehouse_location_warehouse_deleted`(`warehouse_id` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_warehouse_location_creator_foreign`(`creator_id` ASC) USING BTREE,
  INDEX `idx_warehouse_location_updater_foreign`(`updater_id` ASC) USING BTREE,
  CONSTRAINT `warehouse_location_ibfk_1` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库位置表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
