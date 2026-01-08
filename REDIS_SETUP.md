# Redis集成安装和使用指南

## 安装Redis

### Windows环境
1. 下载Redis for Windows
   - 访问：https://github.com/microsoftarchive/redis/releases
   - 下载：Redis-x64-5.0.14.1.msi

2. 安装步骤
   - 双击安装包，选择安装路径（如 C:\Redis）
   - 勾选"添加到系统PATH"
   - 安装完成后，Redis服务会自动启动

3. 配置Redis
   - 编辑 `C:\Redis\redis.windows-service.conf`
   - 设置密码：`requirepass your_password`
   - 设置最大内存：`maxmemory 512mb`

### Linux环境（Docker）
```bash
# 拉取Redis镜像
docker pull redis:7-alpine

# 启动Redis容器
docker run -d --name redis \
  -p 6379:6379 \
  -v redis_data:/data \
  redis:7-alpine \
  redis-server --requirepass your_password --maxmemory 256mb
```

## 修改配置文件

更新 `backend/src/main/resources/application.yml` 中的Redis配置：

```yaml
spring:
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: your_password  # 替换为实际密码
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0

  # 缓存配置
  cache:
    type: redis
    redis:
      time-to-live: 900000  # 15分钟，单位毫秒
      cache-null-values: true  # 缓存空值，防止缓存穿透
      key-prefix: "warehouse:cache:"
      use-key-prefix: true
```

## 缓存功能

### 已实现的功能
1. **商品列表查询缓存** - 缓存分页和条件查询结果
2. **库存预警查询缓存** - 缓存低库存商品列表
3. **自动缓存失效** - 商品增删改时自动清除相关缓存

### 缓存策略
- **缓存时间**：15分钟（可配置）
- **大结果集保护**：结果超过1000条时不缓存
- **缓存key**：基于查询参数自动生成唯一key
- **缓存穿透防护**：缓存空查询结果

## 测试验证

### 1. 启动Redis服务
```bash
# Windows
net start Redis

# Linux Docker
docker start redis
```

### 2. 验证Redis连接
```bash
# 连接Redis
redis-cli
> auth your_password
> ping
# 应该返回 PONG
```

### 3. 启动应用
```bash
cd backend
mvn spring-boot:run
```

### 4. 测试缓存功能
1. 访问商品列表API：`GET /api/api/products`
2. 查看Redis中的缓存数据
3. 再次访问相同API，应该从缓存返回
4. 增删改商品，缓存应该自动失效

## 监控缓存

### Redis CLI命令
```bash
# 查看所有key
redis-cli --scan --pattern "*"

# 查看缓存数据
redis-cli get "warehouse:cache:products:getProducts::::1:10"

# 清空所有缓存
redis-cli flushall
```

### 应用日志
启用DEBUG级别日志查看缓存操作：
```yaml
logging:
  level:
    org.springframework.cache: DEBUG
    com.warehouse.service: DEBUG
```

## 性能优化建议

1. **Redis内存优化**
   - 设置合理的maxmemory限制
   - 使用LRU淘汰策略

2. **缓存策略优化**
   - 根据数据访问频率调整过期时间
   - 避免缓存过大的结果集

3. **监控指标**
   - 缓存命中率
   - 响应时间改善
   - 数据库查询减少比例

## 故障排查

### 常见问题
1. **连接失败**
   - 检查Redis服务是否启动
   - 验证配置文件中的host和port
   - 检查密码是否正确

2. **缓存不生效**
   - 检查@Cacheable注解的参数
   - 确认缓存key生成器是否正确
   - 查看应用日志中的缓存操作信息

3. **内存不足**
   - 增加Redis内存限制
   - 调整缓存过期时间
   - 优化大结果集处理