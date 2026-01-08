# Excel导入导出 & PDF报表生成功能说明

## 功能概述

本次更新为商品出入库管理系统添加了以下功能：

### 1. Excel 导入导出功能
- 商品数据的Excel导入/导出
- 入库记录的Excel导出
- 出库记录的Excel导出

### 2. PDF 报表生成功能
- 库存报表PDF
- 入库统计报表PDF
- 出库统计报表PDF
- 低库存预警报表PDF

---

## 后端实现

### 新增文件

#### 1. ExcelUtil.java (`backend/src/main/java/com/warehouse/util/ExcelUtil.java`)
Excel工具类，提供导入导出功能：
- `exportExcel()` - 导出数据到Excel
- `importExcel()` - 从Excel导入数据
- 支持自定义表头和数据提取器
- 自动调整列宽

#### 2. PdfUtil.java (`backend/src/main/java/com/warehouse/util/PdfUtil.java`)
PDF工具类，提供PDF生成功能：
- 支持中文字体（使用iText Asian字体库）
- 提供表格、标题、段落等常用元素创建方法
- 统一的样式管理

#### 3. ReportService.java (`backend/src/main/java/com/warehouse/service/ReportService.java`)
报表服务类，提供4种PDF报表生成：
- `generateStockReport()` - 库存报表
- `generateInboundReport()` - 入库统计报表
- `generateOutboundReport()` - 出库统计报表
- `generateLowStockReport()` - 低库存预警报表

#### 4. ReportController.java (`backend/src/main/java/com/warehouse/controller/ReportController.java`)
报表控制器，提供报表生成接口：
- `GET /api/reports/stock/pdf` - 生成库存报表
- `GET /api/reports/inbound/pdf` - 生成入库报表
- `GET /api/reports/outbound/pdf` - 生成出库报表
- `GET /api/reports/low-stock/pdf` - 生成低库存报表

### 修改的文件

#### ProductService.java & ProductController.java
新增接口：
- `GET /api/products/export` - 导出商品数据
- `POST /api/products/import` - 导入商品数据

导入Excel格式要求：
| 商品编号 | 商品名称 | 分类 | 供应商 | 单价 | 最低库存 | 单位 | 条形码 | 说明 |
|---------|---------|------|--------|------|---------|------|--------|------|

#### InboundService.java & InboundController.java
新增接口：
- `GET /api/inbounds/export?startDate=&endDate=` - 导出入库记录

#### OutboundService.java & OutboundController.java
新增接口：
- `GET /api/outbounds/export?startDate=&endDate=` - 导出出库记录

### 新增依赖 (pom.xml)

```xml
<!-- iText for PDF -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext-asian</artifactId>
    <version>5.2.0</version>
</dependency>
```

---

## 前端实现

### 修改的文件

#### 1. api/index.js
新增API接口：

**商品API (productApi)**
```javascript
exportProducts()        // 导出商品
importProducts(file)    // 导入商品
```

**入库API (inboundApi)**
```javascript
exportInbounds(params)  // 导出入库记录
```

**出库API (outboundApi)**
```javascript
exportOutbounds(params) // 导出出库记录
```

**报表API (reportApi)**
```javascript
generateStockReportPdf()              // 库存报表PDF
generateInboundReportPdf(params)      // 入库报表PDF
generateOutboundReportPdf(params)     // 出库报表PDF
generateLowStockReportPdf()           // 低库存报表PDF
```

#### 2. views/Product/ProductList.vue
在商品管理页面添加：
- **导入Excel** 按钮：支持上传.xlsx或.xls文件
- **导出Excel** 按钮：导出所有商品数据

#### 3. views/Report/ReportList.vue
在报表统计页面添加4个PDF生成按钮：
- **库存报表PDF** - 生成当前所有商品的库存报表
- **入库报表PDF** - 根据日期范围生成入库统计
- **出库报表PDF** - 根据日期范围生成出库统计
- **低库存报表PDF** - 生成低于最低库存的商品报表

---

## 使用说明

### Excel 导入导出

#### 导出商品数据
1. 进入"商品管理"页面
2. 点击"导出Excel"按钮
3. 系统会自动下载包含所有商品数据的Excel文件

#### 导入商品数据
1. 准备Excel文件，格式如下：

   | 商品编号 | 商品名称 | 分类 | 供应商 | 单价 | 最低库存 | 单位 | 条形码 | 说明 |
   |---------|---------|------|--------|------|---------|------|--------|------|
   | P001    | 测试商品 | 电子 | 供应商A | 100  | 10      | 件   | 123456 | 测试 |

2. 进入"商品管理"页面
3. 点击"导入Excel"按钮，选择准备好的Excel文件
4. 系统会自动导入数据，如有错误会显示详细信息

**注意事项：**
- 商品编号和名称为必填项
- 如果商品编号已存在，会跳过该行
- 分类和供应商需要在系统中已存在（根据名称匹配）

### PDF 报表生成

#### 库存报表
1. 进入"报表统计"页面
2. 点击"库存报表PDF"按钮
3. 系统生成包含所有商品库存信息的PDF报表

报表内容：
- 商品ID、编号、名称
- 分类、供应商
- 单价、库存、最低库存
- 统计信息（总商品数、总库存量、低库存商品数）

#### 入库/出库报表
1. 进入"报表统计"页面
2. （可选）选择日期范围
3. 点击"入库报表PDF"或"出库报表PDF"
4. 系统生成指定期间的出入库统计PDF

报表内容：
- 单号、商品信息、数量、日期
- 供应商/客户
- 统计信息（单据数、总数量）

#### 低库存报表
1. 进入"报表统计"页面
2. 点击"低库存报表PDF"
3. 系统生成低于最低库存的商品预警报表

报表内容：
- 低库存商品详细信息
- 当前库存 vs 最低库存
- 缺口数量
- 统计信息

---

## API 接口文档

### Excel 导入导出接口

#### 1. 导出商品
```
GET /api/products/export
响应类型：application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

#### 2. 导入商品
```
POST /api/products/import
Content-Type: multipart/form-data
参数：file (Excel文件)

响应：
{
  "code": 200,
  "message": "导入成功",
  "data": [] // 错误信息列表，为空表示全部成功
}
```

#### 3. 导出入库记录
```
GET /api/inbounds/export?startDate=&endDate=
参数：
  - startDate: 开始日期（可选，格式：yyyy-MM-dd HH:mm:ss）
  - endDate: 结束日期（可选，格式：yyyy-MM-dd HH:mm:ss）
响应类型：application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

#### 4. 导出出库记录
```
GET /api/outbounds/export?startDate=&endDate=
参数：
  - startDate: 开始日期（可选，格式：yyyy-MM-dd HH:mm:ss）
  - endDate: 结束日期（可选，格式：yyyy-MM-dd HH:mm:ss）
响应类型：application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

### PDF 报表接口

#### 1. 库存报表
```
GET /api/reports/stock/pdf
响应类型：application/pdf
```

#### 2. 入库报表
```
GET /api/reports/inbound/pdf?startDate=&endDate=
参数：
  - startDate: 开始日期（可选，格式：yyyy-MM-dd HH:mm:ss）
  - endDate: 结束日期（可选，格式：yyyy-MM-dd HH:mm:ss）
响应类型：application/pdf
```

#### 3. 出库报表
```
GET /api/reports/outbound/pdf?startDate=&endDate=
参数：
  - startDate: 开始日期（可选，格式：yyyy-MM-dd HH:mm:ss）
  - endDate: 结束日期（可选，格式：yyyy-MM-dd HH:mm:ss）
响应类型：application/pdf
```

#### 4. 低库存报表
```
GET /api/reports/low-stock/pdf
响应类型：application/pdf
```

---

## 技术细节

### Excel 处理
- 使用 Apache POI 5.2.3
- 支持 .xlsx 和 .xls 格式
- 自动调整列宽
- 统一的样式管理

### PDF 生成
- 使用 iText 5.5.13.3
- 支持中文显示（itext-asian）
- 使用 STSong-Light 字体
- 统一的表格和文本样式

### 文件下载
- 前端使用 Blob 对象处理二进制数据
- 自动创建下载链接并触发下载
- 文件名包含时间戳，避免重复

---

## 注意事项

1. **权限控制**：当前版本未对导入导出功能添加特殊权限控制，建议在生产环境中根据角色限制访问

2. **数据量限制**：
   - Excel导出适用于中小规模数据（建议不超过10000条）
   - 大数据量建议分批导出或添加筛选条件

3. **导入验证**：
   - 导入时会验证必填字段
   - 重复的商品编号会被跳过
   - 不存在的分类/供应商会被忽略

4. **PDF报表**：
   - 报表生成可能需要几秒钟，请耐心等待
   - 建议使用日期范围过滤大数据量

---

## 后续优化建议

1. 添加Excel模板下载功能
2. 支持更多字段的导入（如供应商、客户等）
3. 添加导入数据预览功能
4. 支持自定义报表样式
5. 添加报表定时生成和邮件发送功能
6. 支持Excel批量导出（按条件筛选）

---

## 更新日期
2025-11-17
