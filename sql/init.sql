-- 商品出入库管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS warehouse_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE warehouse_management;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `role` VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE' COMMENT '用户角色：ADMIN-管理员, WAREHOUSE_KEEPER-库管员, EMPLOYEE-普通员工',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '电话',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_username` (`username`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品类别表
CREATE TABLE IF NOT EXISTS `category` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '类别名称',
  `description` VARCHAR(255) COMMENT '类别描述',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类别表';

-- 供应商表
CREATE TABLE IF NOT EXISTS `supplier` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
  `contact_person` VARCHAR(50) COMMENT '联系人',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `address` VARCHAR(255) COMMENT '地址',
  `email` VARCHAR(100) COMMENT '邮箱',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 客户表
CREATE TABLE IF NOT EXISTS `customer` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '客户名称',
  `contact_person` VARCHAR(50) COMMENT '联系人',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `address` VARCHAR(255) COMMENT '地址',
  `email` VARCHAR(100) COMMENT '邮箱',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编号',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category_id` INT COMMENT '商品类别ID',
  `supplier_id` INT COMMENT '供应商ID',
  `price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '商品单价',
  `stock_qty` INT DEFAULT 0 COMMENT '当前库存量',
  `min_stock` INT DEFAULT 0 COMMENT '库存警戒线',
  `unit` VARCHAR(20) DEFAULT '件' COMMENT '单位',
  `barcode` VARCHAR(100) COMMENT '条形码',
  `description` TEXT COMMENT '商品描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`supplier_id`) REFERENCES `supplier`(`id`) ON DELETE SET NULL,
  INDEX `idx_code` (`code`),
  INDEX `idx_name` (`name`),
  INDEX `idx_category` (`category_id`),
  INDEX `idx_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 入库单表
CREATE TABLE IF NOT EXISTS `inbound` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `inbound_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '入库单号',
  `product_id` INT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL COMMENT '入库数量',
  `supplier_id` INT COMMENT '供应商ID',
  `inbound_date` DATETIME NOT NULL COMMENT '入库时间',
  `operator_id` INT COMMENT '操作员ID',
  `remark` VARCHAR(255) COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`supplier_id`) REFERENCES `supplier`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`operator_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX `idx_inbound_no` (`inbound_no`),
  INDEX `idx_product` (`product_id`),
  INDEX `idx_inbound_date` (`inbound_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- 出库单表
CREATE TABLE IF NOT EXISTS `outbound` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `outbound_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '出库单号',
  `product_id` INT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL COMMENT '出库数量',
  `customer_id` INT COMMENT '客户ID',
  `outbound_date` DATETIME NOT NULL COMMENT '出库时间',
  `operator_id` INT COMMENT '操作员ID',
  `remark` VARCHAR(255) COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`customer_id`) REFERENCES `customer`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`operator_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX `idx_outbound_no` (`outbound_no`),
  INDEX `idx_product` (`product_id`),
  INDEX `idx_outbound_date` (`outbound_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';

-- 库存调整记录表
CREATE TABLE IF NOT EXISTS `stock_adjustment` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `product_id` INT NOT NULL COMMENT '商品ID',
  `adjustment_type` VARCHAR(20) NOT NULL COMMENT '调整类型：INCREASE-增加, DECREASE-减少',
  `quantity` INT NOT NULL COMMENT '调整数量',
  `before_qty` INT NOT NULL COMMENT '调整前库存',
  `after_qty` INT NOT NULL COMMENT '调整后库存',
  `reason` VARCHAR(255) NOT NULL COMMENT '调整理由',
  `operator_id` INT COMMENT '操作员ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`operator_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX `idx_product` (`product_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整记录表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `user_id` INT COMMENT '用户ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_desc` VARCHAR(255) COMMENT '操作描述',
  `module` VARCHAR(50) COMMENT '操作模块',
  `ip_address` VARCHAR(50) COMMENT 'IP地址',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX `idx_user` (`user_id`),
  INDEX `idx_operation_type` (`operation_type`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` VARCHAR(255) COMMENT '配置值',
  `config_desc` VARCHAR(255) COMMENT '配置描述',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入初始数据

-- 插入默认管理员用户（密码：admin123，使用BCrypt加密后的值）
INSERT INTO `user` (`username`, `password`, `role`, `real_name`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6wPa', 'ADMIN', '系统管理员', 1),
('keeper', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6wPa', 'WAREHOUSE_KEEPER', '库管员', 1),
('employee', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6wPa', 'EMPLOYEE', '普通员工', 1);

-- 插入默认商品类别
INSERT INTO `category` (`name`, `description`) VALUES
('电子产品', '各类电子设备及配件'),
('日用品', '日常生活用品'),
('食品饮料', '各类食品和饮料'),
('办公用品', '办公设备和文具'),
('服装鞋帽', '服装和鞋帽类商品');

-- 插入默认供应商
INSERT INTO `supplier` (`name`, `contact_person`, `phone`, `address`) VALUES
('供应商A', '张三', '13800138001', '北京市朝阳区xxx路xxx号'),
('供应商B', '李四', '13800138002', '上海市浦东新区xxx路xxx号'),
('供应商C', '王五', '13800138003', '广州市天河区xxx路xxx号');

-- 插入默认客户
INSERT INTO `customer` (`name`, `contact_person`, `phone`, `address`) VALUES
('客户A', '赵六', '13900139001', '深圳市南山区xxx路xxx号'),
('客户B', '钱七', '13900139002', '杭州市西湖区xxx路xxx号'),
('客户C', '孙八', '13900139003', '成都市锦江区xxx路xxx号');

-- 插入系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('default_min_stock', '10', '默认库存警戒线'),
('system_name', '商品出入库管理系统', '系统名称'),
('page_size', '10', '默认分页大小');

