-- 仓库管理系统数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS warehouse_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE warehouse_management;

-- 创建仓库表
CREATE TABLE IF NOT EXISTS warehouse (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '仓库ID',
  name VARCHAR(100) NOT NULL UNIQUE COMMENT '仓库名称',
  address VARCHAR(255) NOT NULL COMMENT '仓库地址',
  code VARCHAR(50) UNIQUE COMMENT '仓库编码',
  contact_person VARCHAR(50) COMMENT '联系人',
  phone VARCHAR(20) COMMENT '联系电话',
  status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_name (name),
  INDEX idx_code (code),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- 创建仓库位置表
CREATE TABLE IF NOT EXISTS warehouse_location (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '位置ID',
  warehouse_id INT NOT NULL COMMENT '仓库ID',
  rack_number VARCHAR(50) NOT NULL COMMENT '货架号',
  shelf_level VARCHAR(20) NOT NULL COMMENT '层号',
  bin_number VARCHAR(50) COMMENT '货位号',
  description VARCHAR(255) COMMENT '位置描述',
  status TINYINT DEFAULT 1 COMMENT '状态：1-可用，0-禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_warehouse (warehouse_id),
  INDEX idx_location (rack_number, shelf_level, bin_number),
  UNIQUE KEY uk_location (warehouse_id, rack_number, shelf_level, bin_number) COMMENT '位置唯一约束',
  CONSTRAINT fk_location_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库位置表';

-- 插入示例数据
INSERT INTO warehouse (name, address, code, contact_person, phone, status) VALUES
('珠海仓库', '珠海市香洲区', 'ck1', '邸国梁', '17775791252', 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  address = VALUES(address),
  contact_person = VALUES(contact_person),
  phone = VALUES(phone);

-- 插入示例仓库位置数据
INSERT INTO warehouse_location (warehouse_id, rack_number, shelf_level, bin_number, description, status) VALUES
(1, 'A1', '1', '001', 'A区1架1层1号位', 1),
(1, 'A1', '1', '002', 'A区1架1层2号位', 1),
(1, 'A1', '2', '001', 'A区1架2层1号位', 1),
(1, 'B2', '1', '001', 'B区2架1层1号位', 1),
(1, 'C3', '3', '003', 'C区3架3层3号位', 1)
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  status = VALUES(status);