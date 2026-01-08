# 商品出入库管理系统

## 项目简介

本系统是一个基于Vue.js和Spring Boot的商品出入库管理系统，支持商品的入库、出库、库存管理、报表统计等功能。

## 技术栈

### 后端
- Spring Boot 2.7.14
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- Maven

### 前端
- Vue.js 2.6.14
- Vue Router
- Vuex
- Element UI
- Axios

## 项目结构

```
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/warehouse/
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   ├── repository/      # 数据访问层
│   │   │   │   ├── service/         # 业务逻辑层
│   │   │   │   ├── controller/     # 控制器层
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── filter/         # 过滤器
│   │   │   │   ├── dto/            # 数据传输对象
│   │   │   │   ├── common/         # 通用类
│   │   │   │   └── util/           # 工具类
│   │   │   └── resources/
│   │   │       └── application.yml # 配置文件
│   │   └── pom.xml
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── views/           # 页面组件
│   │   ├── router/          # 路由配置
│   │   ├── store/           # 状态管理
│   │   ├── layout/          # 布局组件
│   │   ├── utils/           # 工具函数
│   │   └── styles/          # 样式文件
│   ├── package.json
│   └── vue.config.js
└── sql/                     # SQL脚本
    └── init.sql            # 数据库初始化脚本
```

## 环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 14+
- MySQL 8.0+

## 安装与运行

### 1. 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE warehouse_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p warehouse_management < sql/init.sql
```

3. 修改后端配置文件 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/warehouse_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的数据库密码
```

### 2. 后端运行

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 3. 前端运行

```bash
cd frontend
npm install
npm run serve
```

前端服务将在 `http://localhost:8081` 启动

## 默认账号

系统初始化后，提供以下默认账号：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | 管理员 | 拥有所有权限 |
| keeper | admin123 | 库管员 | 负责出入库操作 |
| employee | admin123 | 普通员工 | 可查看库存信息 |

**注意**：默认密码已使用BCrypt加密，实际密码为 `admin123`

## 功能模块

### 1. 用户管理
- 用户登录/登出
- 基于角色的权限控制（管理员、库管员、普通员工）

### 2. 商品管理
- 商品的增删改查
- 商品分类管理
- 商品信息导入导出（待实现）

### 3. 入库管理
- 商品入库操作
- 入库单管理
- 自动更新库存

### 4. 出库管理
- 商品出库操作
- 出库单管理
- 库存校验

### 5. 库存管理
- 库存查询
- 库存调整
- 低库存预警

### 6. 报表统计
- 出入库报表
- 库存统计
- 数据导出（待实现）

## API接口说明

### 认证接口
- `POST /api/auth/login` - 用户登录

### 商品接口
- `GET /api/products` - 获取商品列表
- `GET /api/products/{id}` - 获取商品详情
- `POST /api/products` - 创建商品
- `PUT /api/products/{id}` - 更新商品
- `DELETE /api/products/{id}` - 删除商品
- `GET /api/products/low-stock` - 获取低库存商品

### 入库接口
- `GET /api/inbounds` - 获取入库单列表
- `GET /api/inbounds/{id}` - 获取入库单详情
- `POST /api/inbounds` - 创建入库单
- `DELETE /api/inbounds/{id}` - 删除入库单

### 出库接口
- `GET /api/outbounds` - 获取出库单列表
- `GET /api/outbounds/{id}` - 获取出库单详情
- `POST /api/outbounds` - 创建出库单
- `DELETE /api/outbounds/{id}` - 删除出库单

### 类别接口
- `GET /api/categories` - 获取所有类别
- `POST /api/categories` - 创建类别
- `PUT /api/categories/{id}` - 更新类别
- `DELETE /api/categories/{id}` - 删除类别

### 库存调整接口
- `GET /api/stock-adjustments` - 获取调整记录
- `POST /api/stock-adjustments` - 创建调整记录

## 开发说明

### 后端开发
1. 实体类使用JPA注解，自动管理表结构
2. 使用JWT进行身份认证
3. 统一使用Result类封装响应结果
4. 使用@Transactional保证数据一致性

### 前端开发
1. 使用Vuex管理全局状态
2. 使用Element UI组件库
3. 使用Axios进行HTTP请求
4. 路由守卫控制页面访问权限

## 注意事项

1. 首次运行前需要执行SQL初始化脚本
2. 修改数据库连接配置
3. JWT密钥在application.yml中配置
4. 前端代理配置在vue.config.js中

## 待完善功能

- [x] Excel导入导出功能 ✅ (已完成)
- [x] PDF报表生成 ✅ (已完成)
- [ ] 操作日志详细记录
- [ ] 条形码扫描功能
- [ ] 数据备份与恢复
- [ ] 系统参数配置界面

### 最新功能 (2025-11-17)

#### Excel 导入导出
- ✅ 商品数据Excel导入/导出
- ✅ 入库记录Excel导出
- ✅ 出库记录Excel导出

#### PDF 报表生成
- ✅ 库存报表PDF
- ✅ 入库统计报表PDF
- ✅ 出库统计报表PDF
- ✅ 低库存预警报表PDF

详细使用说明请查看：[EXCEL_PDF_FEATURE.md](EXCEL_PDF_FEATURE.md)

## 许可证

MIT License

## 联系方式

如有问题，请提交Issue或联系开发团队。

