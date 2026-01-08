# 仓库管理系统部署指南

## 📋 目录

- [系统要求](#系统要求)
- [部署方案](#部署方案)
- [快速部署](#快速部署)
- [详细部署步骤](#详细部署步骤)
- [配置说明](#配置说明)
- [服务管理](#服务管理)
- [常见问题](#常见问题)
- [维护指南](#维护指南)

## 📋 系统要求

### 硬件要求
- **CPU**: 2核心或以上
- **内存**: 4GB或以上
- **存储**: 20GB可用空间
- **网络**: 稳定的互联网连接

### 操作系统支持
- ✅ Ubuntu 20.04/22.04 LTS
- ✅ CentOS 7/8
- ✅ Windows 10/11 (需要 Docker Desktop)
- ✅ macOS (需要 Docker Desktop)

### 软件依赖
- Docker 20.10+
- Docker Compose 2.20+
- Git (可选，用于代码管理)

## 🚀 部署方案

### 方案对比

| 方案 | 适用场景 | 优点 | 缺点 | 成本 |
|------|----------|------|------|------|
| **单机部署** | 小团队/个人使用 | 简单、成本低 | 单点故障 | 200-300元/月 |
| **云数据库部署** | 生产环境 | 数据安全、自动备份 | 成本稍高 | 300-500元/月 |
| **集群部署** | 大型企业 | 高可用、可扩展 | 复杂、成本高 | 1000元+/月 |

### 推荐配置
根据您的需求，推荐使用 **单机部署** 方案：
- 腾讯云轻量应用服务器 (2核4G)
- 月费用约 200元
- 适合 < 50 人同时使用

## ⚡ 快速部署

### 一键部署 (推荐)

```bash
# Linux/macOS
chmod +x deploy.sh
./deploy.sh
```

```bat
# Windows
deploy.bat
```

### 手动部署

```bash
# 1. 克隆项目
git clone [项目地址] warehouse
cd warehouse

# 2. 配置环境变量
cp .env.example .env

# 3. 构建并启动
docker-compose up -d
```

## 📚 详细部署步骤

### 第一步：准备服务器

#### 1.1 购买腾讯云轻量应用服务器

1. 访问 [腾讯云官网](https://cloud.tencent.com/)
2. 选择 "轻量应用服务器"
3. 推荐配置：
   - 地域：选择离您最近的地域
   - 机型：2核4G
   - 存储：80GB SSD
   - 镜像：Ubuntu 22.04
   - 月付：约 200 元

#### 1.2 基础环境配置

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装必要工具
sudo apt install -y curl wget unzip htop vim git
```

### 第二步：安装 Docker

#### 2.1 安装 Docker (Ubuntu)

```bash
# 更新包索引
sudo apt update

# 安装依赖
sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release

# 添加 Docker 官方 GPG 密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 设置稳定版仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker Engine
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# 启动 Docker 服务
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户添加到 docker 组
sudo usermod -aG docker $USER
```

#### 2.2 安装 Docker Compose

```bash
# 下载 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 设置执行权限
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

### 第三步：部署应用

#### 3.1 下载项目代码

```bash
# 克隆项目（如果有 Git 仓库）
git clone [项目地址] warehouse
cd warehouse

# 或者直接上传项目文件到服务器
```

#### 3.2 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量（根据实际情况修改）
vim .env
```

主要配置项：
```env
# 数据库配置
MYSQL_ROOT_PASSWORD=your_secure_password  # 修改为安全密码
MYSQL_USER=warehouse_user
MYSQL_PASSWORD=your_user_password      # 修改为安全密码

# JWT配置
JWT_SECRET=your_jwt_secret_key_here    # 修改为随机密钥
JWT_EXPIRATION=86400000                 # 24小时

# 应用配置
SPRING_PROFILES_ACTIVE=docker
```

#### 3.3 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 第四步：验证部署

#### 4.1 检查服务状态

```bash
# 检查容器状态
docker-compose ps

# 应该看到三个容器：
# warehouse_mysql
# warehouse_backend
# warehouse_frontend
```

#### 4.2 测试服务连接

```bash
# 测试前端
curl http://localhost

# 测试后端
curl http://localhost:8083/api/actuator/health

# 测试数据库
docker-compose exec mysql mysql -u root -p
```

#### 4.3 访问应用

- **前端访问**: http://[服务器IP]
- **后端API**: http://[服务器IP]:8083/api

## ⚙️ 配置说明

### 环境变量配置

| 变量名 | 说明 | 默认值 | 备注 |
|--------|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | warehouse_secure_2024 | 建议修改 |
| `MYSQL_USER` | MySQL 用户名 | warehouse_user | - |
| `MYSQL_PASSWORD` | MySQL 用户密码 | warehouse_pass_2024 | 建议修改 |
| `JWT_SECRET` | JWT 密钥 | warehouse_management_jwt_secret_2024_secure_key | 必须修改 |
| `JWT_EXPIRATION` | JWT 过期时间(毫秒) | 86400000 | 24小时 |

### Docker Compose 配置

```yaml
# docker-compose.yml 关键配置
services:
  mysql:
    ports:
      - "3306:3306"        # 数据库端口映射
    volumes:
      - mysql_data:/var/lib/mysql  # 数据持久化
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql  # 初始化脚本

  backend:
    ports:
      - "8083:8083"        # 后端端口映射
    environment:
      - SPRING_PROFILES_ACTIVE=docker  # 使用 Docker 配置
    volumes:
      - ./logs:/app/logs      # 日志挂载
      - ./uploads:/app/uploads  # 文件上传目录

  frontend:
    ports:
      - "80:80"             # 前端端口映射
    volumes:
      - ./nginx/ssl:/etc/nginx/ssl  # SSL 证书目录
```

## 🔧 服务管理

### 常用命令

```bash
# 查看服务状态
docker-compose ps

# 查看所有日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql

# 重启服务
docker-compose restart
docker-compose restart backend

# 停止服务
docker-compose down

# 停止并删除容器、网络
docker-compose down --remove-orphans

# 重新构建并启动
docker-compose up -d --build
```

### 更新部署

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker-compose down
docker-compose up -d --build
```

### 数据备份

```bash
# 备份数据库
docker-compose exec mysql mysqldump -u root -p warehouse_management > backup_$(date +%Y%m%d_%H%M%S).sql

# 恢复数据库
docker-compose exec -i mysql mysql -u root -p warehouse_management < backup_file.sql
```

## ❓ 常见问题

### Q1: 端口被占用
**问题**: 端口 80、8083、3306 被占用

**解决方案**:
```bash
# 查看端口占用情况
sudo netstat -tulpn | grep :80
sudo netstat -tulpn | grep :8083
sudo netstat -tulpn | grep :3306

# 停止占用端口的服务
sudo kill -9 [PID]

# 或者修改 docker-compose.yml 中的端口映射
```

### Q2: 内存不足
**问题**: 服务器内存不足导致容器异常退出

**解决方案**:
```bash
# 检查内存使用
free -h
docker stats

# 清理未使用的镜像
docker system prune -a

# 增加交换空间
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### Q3: 数据库连接失败
**问题**: 后端无法连接到数据库

**解决方案**:
```bash
# 检查数据库容器状态
docker-compose logs mysql

# 检查网络连接
docker-compose exec backend ping mysql

# 重启数据库容器
docker-compose restart mysql

# 检查环境变量配置
docker-compose exec backend env | grep MYSQL
```

### Q4: 文件上传失败
**问题**: 文件上传功能不工作

**解决方案**:
```bash
# 检查上传目录权限
ls -la uploads/

# 设置正确的权限
sudo chown -R 1000:1000 uploads/
sudo chmod -R 755 uploads/

# 修改目录权限
sudo chown -R 1000:1000 ./uploads
sudo chmod -R 755 ./uploads
```

### Q5: 访问速度慢
**问题**: 页面加载速度慢

**解决方案**:
1. 检查服务器资源使用情况
2. 优化数据库查询
3. 使用 CDN 加速静态资源
4. 启用 Gzip 压缩

## 🔧 维护指南

### 日常维护

#### 1. 监控服务状态
```bash
# 创建监控脚本
cat > monitor.sh << 'EOF'
#!/bin/bash
echo "=== 服务状态检查 ==="
docker-compose ps

echo "=== 系统资源使用 ==="
free -h
df -h

echo "=== 容器资源使用 ==="
docker stats --no-stream
EOF

chmod +x monitor.sh
./monitor.sh
```

#### 2. 日志管理
```bash
# 创建日志清理脚本
cat > clean_logs.sh << 'EOF'
#!/bin/bash
echo "清理30天前的日志..."
find ./logs -name "*.log" -mtime +30 -delete
docker-compose exec mysql mysql -u root -p -e "PURGE BINARY LOGS;"
EOF

chmod +x clean_logs.sh
# 添加到 crontab
echo "0 2 * * * /path/to/clean_logs.sh" | crontab -
```

#### 3. 自动备份
```bash
# 创建备份脚本
cat > backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/backup/warehouse"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 备份数据库
echo "备份数据库..."
docker-compose exec mysql mysqldump -u root -p warehouse_management | gzip > $BACKUP_DIR/db_backup_$DATE.sql.gz

# 备份上传文件
echo "备份上传文件..."
tar -czf $BACKUP_DIR/uploads_backup_$DATE.tar.gz ./uploads/

# 清理7天前的备份
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete

echo "备份完成: $BACKUP_DIR"
EOF

chmod +x backup.sh
# 添加到 crontab，每天凌晨3点执行
echo "0 3 * * * /path/to/backup.sh" | crontab -
```

### 安全建议

1. **定期更新系统**
   ```bash
   sudo apt update && sudo apt upgrade -y
   ```

2. **配置防火墙**
   ```bash
   sudo ufw enable
   sudo ufw allow ssh
   sudo ufw allow 80
   sudo ufw allow 443
   ```

3. **修改默认密码**
   - 修改 `.env` 文件中的数据库密码
   - 修改 JWT 密钥

4. **启用 HTTPS**
   - 申请免费 SSL 证书（Let's Encrypt）
   - 配置 Nginx 支持 HTTPS

5. **定期检查日志**
   - 监控异常访问
   - 检查系统错误日志

## 📞 技术支持

如果在部署过程中遇到问题，请提供以下信息：

1. **系统信息**：操作系统版本、Docker版本
2. **错误信息**：具体的错误日志或截图
3. **配置信息**：`.env` 文件内容（隐藏密码）
4. **服务状态**：`docker-compose ps` 的输出

### 常用调试命令

```bash
# 检查 Docker 服务状态
sudo systemctl status docker

# 检查容器日志
docker logs [container_name]

# 进入容器调试
docker-compose exec backend bash
docker-compose exec mysql mysql -u root -p

# 检查网络连接
docker network ls
docker network inspect [network_name]
```

---

## 🎉 部署成功！

恭喜！您已成功部署仓库管理系统。现在您可以：

1. **访问系统**：http://[您的服务器IP]
2. **开始使用**：使用默认管理员账号登录
3. **配置管理**：根据业务需求配置用户权限和商品信息

如有任何问题，请参考本文档或联系技术支持。