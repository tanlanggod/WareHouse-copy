-- 为新增的审计字段和逻辑删除字段创建索引
-- 优化查询性能，特别是在处理逻辑删除和审计查询时

USE warehouse_management;

-- 逻辑删除相关索引
CREATE INDEX idx_user_deleted ON user(is_deleted);
CREATE INDEX idx_user_creator ON user(creator_id);
CREATE INDEX idx_user_updater ON user(updater_id);

CREATE INDEX idx_warehouse_deleted ON warehouse(is_deleted);
CREATE INDEX idx_warehouse_creator ON warehouse(creator_id);
CREATE INDEX idx_warehouse_updater ON warehouse(updater_id);

CREATE INDEX idx_warehouse_location_deleted ON warehouse_location(is_deleted);
CREATE INDEX idx_warehouse_location_creator ON warehouse_location(creator_id);
CREATE INDEX idx_warehouse_location_updater ON warehouse_location(updater_id);

CREATE INDEX idx_supplier_deleted ON supplier(is_deleted);
CREATE INDEX idx_supplier_creator ON supplier(creator_id);
CREATE INDEX idx_supplier_updater ON supplier(updater_id);

CREATE INDEX idx_customer_deleted ON customer(is_deleted);
CREATE INDEX idx_customer_creator ON customer(creator_id);
CREATE INDEX idx_customer_updater ON customer(updater_id);

CREATE INDEX idx_product_deleted ON product(is_deleted);
CREATE INDEX idx_product_creator ON product(creator_id);
CREATE INDEX idx_product_updater ON product(updater_id);

CREATE INDEX idx_category_deleted ON category(is_deleted);
CREATE INDEX idx_category_creator ON category(creator_id);
CREATE INDEX idx_category_updater ON category(updater_id);

CREATE INDEX idx_inbound_deleted ON inbound(is_deleted);
CREATE INDEX idx_inbound_creator ON inbound(creator_id);
CREATE INDEX idx_inbound_updater ON inbound(updater_id);
CREATE INDEX idx_inbound_updated ON inbound(updated_at);

CREATE INDEX idx_outbound_deleted ON outbound(is_deleted);
CREATE INDEX idx_outbound_creator ON outbound(creator_id);
CREATE INDEX idx_outbound_updater ON outbound(updater_id);
CREATE INDEX idx_outbound_updated ON outbound(updated_at);

CREATE INDEX idx_stock_adjustment_deleted ON stock_adjustment(is_deleted);
CREATE INDEX idx_stock_adjustment_creator ON stock_adjustment(creator_id);
CREATE INDEX idx_stock_adjustment_updater ON stock_adjustment(updater_id);
CREATE INDEX idx_stock_adjustment_updated ON stock_adjustment(updated_at);

CREATE INDEX idx_system_config_deleted ON system_config(is_deleted);
CREATE INDEX idx_system_config_creator ON system_config(creator_id);
CREATE INDEX idx_system_config_updater ON system_config(updater_id);

-- 组合索引，优化常用查询
-- 逻辑删除 + 创建时间的组合索引
CREATE INDEX idx_user_deleted_created ON user(is_deleted, created_at);
CREATE INDEX idx_warehouse_deleted_created ON warehouse(is_deleted, created_at);
CREATE INDEX idx_supplier_deleted_created ON supplier(is_deleted, created_at);
CREATE INDEX idx_customer_deleted_created ON customer(is_deleted, created_at);
CREATE INDEX idx_product_deleted_created ON product(is_deleted, created_at);
CREATE INDEX idx_category_deleted_created ON category(is_deleted, created_at);

-- 业务相关的复合索引
CREATE INDEX idx_product_category_status_deleted ON product(category_id, status, is_deleted);
CREATE INDEX idx_inbound_product_deleted_created ON inbound(product_id, is_deleted, created_at);
CREATE INDEX idx_outbound_product_deleted_created ON outbound(product_id, is_deleted, created_at);
CREATE INDEX idx_warehouse_location_warehouse_deleted ON warehouse_location(warehouse_id, is_deleted);

-- 外键字段优化索引
CREATE INDEX idx_user_creator_foreign ON user(creator_id);
CREATE INDEX idx_user_updater_foreign ON user(updater_id);
CREATE INDEX idx_warehouse_creator_foreign ON warehouse(creator_id);
CREATE INDEX idx_warehouse_updater_foreign ON warehouse(updater_id);
CREATE INDEX idx_warehouse_location_creator_foreign ON warehouse_location(creator_id);
CREATE INDEX idx_warehouse_location_updater_foreign ON warehouse_location(updater_id);
CREATE INDEX idx_supplier_creator_foreign ON supplier(creator_id);
CREATE INDEX idx_supplier_updater_foreign ON supplier(updater_id);
CREATE INDEX idx_customer_creator_foreign ON customer(creator_id);
CREATE INDEX idx_customer_updater_foreign ON customer(updater_id);
CREATE INDEX idx_product_creator_foreign ON product(creator_id);
CREATE INDEX idx_product_updater_foreign ON product(updater_id);
CREATE INDEX idx_category_creator_foreign ON category(creator_id);
CREATE INDEX idx_category_updater_foreign ON category(updater_id);
CREATE INDEX idx_inbound_creator_foreign ON inbound(creator_id);
CREATE INDEX idx_inbound_updater_foreign ON inbound(updater_id);
CREATE INDEX idx_outbound_creator_foreign ON outbound(creator_id);
CREATE INDEX idx_outbound_updater_foreign ON outbound(updater_id);
CREATE INDEX idx_stock_adjustment_creator_foreign ON stock_adjustment(creator_id);
CREATE INDEX idx_stock_adjustment_updater_foreign ON stock_adjustment(updater_id);
CREATE INDEX idx_system_config_creator_foreign ON system_config(creator_id);
CREATE INDEX idx_system_config_updater_foreign ON system_config(updater_id);

COMMIT;