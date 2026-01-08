-- 仓库管理系统数据库表结构
USE warehouse_management;

-- 创建仓库表
CREATE TABLE IF NOT EXISTS warehouse (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  address VARCHAR(255) NOT NULL,
  code VARCHAR(50) UNIQUE,
  contact_person VARCHAR(50),
  phone VARCHAR(20),
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_name (name),
  INDEX idx_code (code),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建仓库位置表
CREATE TABLE IF NOT EXISTS warehouse_location (
  id INT PRIMARY KEY AUTO_INCREMENT,
  warehouse_id INT NOT NULL,
  rack_number VARCHAR(50) NOT NULL,
  shelf_level VARCHAR(20) NOT NULL,
  bin_number VARCHAR(50),
  description VARCHAR(255),
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_warehouse (warehouse_id),
  INDEX idx_location (rack_number, shelf_level, bin_number),
  UNIQUE KEY uk_location (warehouse_id, rack_number, shelf_level, bin_number),
  CONSTRAINT fk_location_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;