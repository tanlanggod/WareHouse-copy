-- 添加基础审计字段和逻辑删除字段
-- 为所有业务表添加创建人、修改人、逻辑删除等基础字段

USE warehouse_management;

-- 为用户表添加基础字段
ALTER TABLE user
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为仓库表添加基础字段
ALTER TABLE warehouse
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为仓库位置表添加基础字段
ALTER TABLE warehouse_location
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为供应商表添加基础字段
ALTER TABLE supplier
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为客户表添加基础字段
ALTER TABLE customer
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为商品表添加基础字段
ALTER TABLE product
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为商品类别表添加基础字段
ALTER TABLE category
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为入库单表添加基础字段
ALTER TABLE inbound
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为出库单表添加基础字段
ALTER TABLE outbound
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为库存调整记录表添加基础字段
ALTER TABLE stock_adjustment
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为系统配置表添加基础字段
ALTER TABLE system_config
ADD COLUMN creator_id INT COMMENT '创建人ID',
ADD COLUMN updater_id INT COMMENT '修改人ID',
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
ADD COLUMN deleted_at DATETIME COMMENT '删除时间',
ADD COLUMN deleter_id INT COMMENT '删除人ID';

-- 为仓库位置表添加版本字段（如果不存在）
ALTER TABLE warehouse_location
ADD COLUMN version BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）';

-- 为商品表添加版本字段
ALTER TABLE product
ADD COLUMN version BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）';

COMMIT;