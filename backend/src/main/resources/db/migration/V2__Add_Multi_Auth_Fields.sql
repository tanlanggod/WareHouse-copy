-- Add multi-authentication fields to user table
-- 微信相关字段
ALTER TABLE user ADD COLUMN wechat_openid VARCHAR(100) UNIQUE;
ALTER TABLE user ADD COLUMN wechat_unionid VARCHAR(100);
ALTER TABLE user ADD COLUMN wechat_nickname VARCHAR(100);
ALTER TABLE user ADD COLUMN wechat_avatar VARCHAR(255);

-- 手机验证相关字段
ALTER TABLE user ADD COLUMN phone_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE user ADD COLUMN phone_verification_time DATETIME;

-- 登录方式字段
ALTER TABLE user ADD COLUMN login_type VARCHAR(20) DEFAULT 'USERNAME';

-- 创建索引
CREATE INDEX idx_user_wechat_openid ON user(wechat_openid);
CREATE INDEX idx_user_wechat_unionid ON user(wechat_unionid);
CREATE INDEX idx_user_phone ON user(phone);
CREATE INDEX idx_user_login_type ON user(login_type);