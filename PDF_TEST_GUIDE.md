# 入库报表PDF生成功能测试指南

## 测试前准备

### 1. 确保后端服务正在运行
```bash
cd D:\Warehouse\backend
mvn spring-boot:run
```

服务应该在 `http://localhost:8080` 启动

### 2. 确认数据库中有入库记录
如果没有测试数据，可以通过前端或API创建一些入库记录。

---

## 测试方法

### 方法1: 使用批处理脚本（推荐）

1. 双击运行 `test-inbound-report.bat`
2. 脚本会自动：
   - 调用API生成PDF
   - 保存到 `test-reports/` 目录
   - 显示测试结果

3. 打开生成的PDF文件检查：
   - `test-reports/inbound_report_all.pdf` - 全部入库记录
   - `test-reports/inbound_report_2025.pdf` - 2025年的入库记录

---

### 方法2: 使用浏览器测试

#### 测试1: 生成全部入库记录
直接在浏览器中访问：
```
http://localhost:8080/api/reports/inbound/pdf
```

浏览器会自动下载PDF文件。

#### 测试2: 生成指定日期范围的入库记录
```
http://localhost:8080/api/reports/inbound/pdf?startDate=2025-01-01%2000:00:00&endDate=2025-12-31%2023:59:59
```

**注意**：URL中的空格需要编码为 `%20`

---

### 方法3: 使用curl命令

#### 测试全部记录
```bash
curl -X GET "http://localhost:8080/api/reports/inbound/pdf" \
  -H "Accept: application/pdf" \
  --output inbound_report.pdf
```

#### 测试指定日期范围
```bash
curl -X GET "http://localhost:8080/api/reports/inbound/pdf?startDate=2025-01-01 00:00:00&endDate=2025-12-31 23:59:59" \
  -H "Accept: application/pdf" \
  --output inbound_report_2025.pdf
```

---

### 方法4: 使用Postman测试

1. **创建新请求**
   - 方法: `GET`
   - URL: `http://localhost:8080/api/reports/inbound/pdf`

2. **添加Query参数**（可选）
   - Key: `startDate`, Value: `2025-01-01 00:00:00`
   - Key: `endDate`, Value: `2025-12-31 23:59:59`

3. **发送请求**
   - 点击 "Send"
   - 在响应区域点击 "Save Response" > "Save to a file"
   - 保存为 `.pdf` 文件

---

### 方法5: 使用前端页面测试

1. **启动前端服务**
   ```bash
   cd D:\Warehouse\frontend
   npm run serve
   ```

2. **访问报表页面**
   - 打开浏览器访问: `http://localhost:8081`
   - 登录系统（用户名: admin, 密码: admin123）
   - 进入"报表统计"页面

3. **生成PDF报表**
   - （可选）选择日期范围
   - 点击"入库报表PDF"按钮
   - 浏览器会自动下载PDF文件

---

## 检查清单

生成PDF后，请检查以下内容：

### ✅ 基本检查
- [ ] PDF文件能正常打开
- [ ] 文件大小 > 0 字节
- [ ] HTTP状态码为 200

### ✅ 内容检查
- [ ] **标题**: "入库统计报表" 显示正确
- [ ] **统计期间**: 日期范围显示正确（或显示"全部"）
- [ ] **生成时间**: 显示当前时间

### ✅ 表格检查
- [ ] 表头中文正常显示
  - 入库单号
  - 商品编号
  - 商品名称
  - 数量
  - 入库日期
  - 供应商

- [ ] 数据行显示正确
  - 入库单号格式: `INyyyyMMddHHmmssXXXXXXXX`
  - 商品信息完整
  - 数量为数字
  - 日期格式: `yyyy-MM-dd`

### ✅ 统计信息检查
- [ ] "入库单数" 数量正确
- [ ] "入库总量" 计算正确

### ✅ 样式检查
- [ ] 表格有边框
- [ ] 表头有灰色背景
- [ ] 文字居中对齐
- [ ] 中文字体正常显示（无乱码）

---

## 常见问题排查

### 问题1: HTTP 500 错误
**可能原因**：
- 数据库连接失败
- 数据中有null值导致NullPointerException

**解决方法**：
1. 检查后端控制台的错误日志
2. 确认数据库服务正在运行
3. 检查 `application.yml` 中的数据库配置

### 问题2: PDF中文显示为方框或乱码
**可能原因**：
- 中文字体未正确加载

**解决方法**：
1. 确认 `itext-asian` 依赖已添加到 `pom.xml`
2. 确认 `PdfUtil.java` 中使用了 `STSong-Light` 字体
3. 重新编译项目: `mvn clean install`

### 问题3: PDF文件大小为0或损坏
**可能原因**：
- PDF生成过程中抛出异常
- Document未正确关闭

**解决方法**：
1. 检查 `ReportController` 中的异常捕获
2. 查看 `e.printStackTrace()` 的输出
3. 确认 `document.close()` 被调用

### 问题4: 没有数据显示
**可能原因**：
- 数据库中没有入库记录
- 日期范围查询条件不匹配

**解决方法**：
1. 检查数据库: `SELECT * FROM inbound LIMIT 10;`
2. 不传日期参数，查询全部记录
3. 使用更大的日期范围

### 问题5: CORS错误（前端调用时）
**可能原因**：
- 跨域配置不正确

**解决方法**：
1. 确认 `ReportController` 添加了 `@CrossOrigin`
2. 检查 `CorsConfig` 配置

---

## 测试数据准备

如果数据库中没有测试数据，可以通过以下方式创建：

### 使用API创建入库记录
```bash
curl -X POST "http://localhost:8080/api/inbounds" \
  -H "Content-Type: application/json" \
  -d '{
    "product": {"id": 1},
    "quantity": 100,
    "supplier": {"id": 1},
    "remark": "测试入库"
  }'
```

### 使用SQL插入测试数据
```sql
INSERT INTO inbound (inbound_no, product_id, quantity, supplier_id, inbound_date, created_at)
VALUES
  ('IN20251117001', 1, 50, 1, NOW(), NOW()),
  ('IN20251117002', 2, 100, 1, NOW(), NOW()),
  ('IN20251117003', 3, 75, 2, NOW(), NOW());
```

---

## 性能测试

### 测试大数据量
1. 插入1000条入库记录
2. 生成PDF并记录时间
3. 检查PDF文件大小和内容完整性

### 预期性能指标
- 100条记录: < 1秒
- 1000条记录: < 3秒
- 10000条记录: < 10秒

---

## 测试报告模板

### 测试结果

| 测试项 | 预期结果 | 实际结果 | 状态 |
|-------|---------|---------|------|
| 生成全部记录PDF | 成功生成 |  | ✅/❌ |
| 生成指定日期范围PDF | 成功生成 |  | ✅/❌ |
| 中文显示 | 正常显示 |  | ✅/❌ |
| 数据准确性 | 数据正确 |  | ✅/❌ |
| 统计信息 | 计算正确 |  | ✅/❌ |

### 发现的问题
1.
2.
3.

### 改进建议
1.
2.
3.

---

## 下一步

测试通过后，可以继续测试其他报表功能：
- ✅ 入库报表PDF
- ⏳ 出库报表PDF
- ⏳ 库存报表PDF
- ⏳ 低库存报表PDF
