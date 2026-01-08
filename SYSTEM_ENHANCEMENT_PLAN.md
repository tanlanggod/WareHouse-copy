# 仓库管理系统功能扩展详细方案

## 项目概述

基于现有的Spring Boot + Vue.js架构，制定三阶段功能扩展方案，逐步将系统升级为智能化、现代化的企业级仓库管理平台。

**技术栈：**
- 后端：Spring Boot 2.7.14 + JPA + Redis + MySQL
- 前端：Vue.js + Element UI
- 移动端：Vue.js + Vant UI
- 新增：Zebra打印、微信小程序、AI集成

---

## 第一阶段：基础智能化扩展

### 1. 条码/二维码管理

#### 1.1 功能需求分析
- 商品条码/二维码生成、打印、扫描功能
- 支持多种条码格式：Code128、QR Code、EAN-13
- 移动端扫码入库、出库操作
- 批量打印标签功能

#### 1.2 技术实现方案
```java
// 新增条码管理相关实体
@Entity
@Table(name = "barcode")
public class Barcode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 20)
    private String type; // CODE128, QRCODE, EAN13

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(length = 255)
    private String imageUrl;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}

// 条码生成服务
@Service
public class BarcodeService {

    @Autowired
    private BarcodeRepository barcodeRepository;

    // 生成条码
    public Barcode generateBarcode(Integer productId, String type) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));

        String barcodeCode = generateUniqueCode(product, type);
        String imageUrl = generateBarcodeImage(barcodeCode, type);

        Barcode barcode = new Barcode();
        barcode.setCode(barcodeCode);
        barcode.setType(type);
        barcode.setProduct(product);
        barcode.setImageUrl(imageUrl);
        barcode.setGeneratedAt(LocalDateTime.now());

        return barcodeRepository.save(barcode);
    }

    // 批量生成
    public List<Barcode> generateBatchBarcodes(Integer productId, String type, int count) {
        List<Barcode> barcodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            barcodes.add(generateBarcode(productId, type));
        }
        return barcodes;
    }
}
```

**前端接口：**
```javascript
// 条码生成接口
POST /api/barcode/generate
{
  "productId": 123,
  "type": "CODE128"
}

// 批量生成条码
POST /api/barcode/batch-generate
{
  "productId": 123,
  "type": "QRCODE",
  "count": 100
}

// 移动端扫码入库
POST /api/inbound/scan
{
  "barcodeCode": "BC001234567",
  "warehouseId": 1,
  "quantity": 10
}
```

#### 1.3 数据库设计
```sql
CREATE TABLE barcode (
  id INT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL,
  type VARCHAR(20) NOT NULL,
  product_id INT NOT NULL,
  image_url VARCHAR(255),
  generated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_product (product_id),
  INDEX idx_code (code),
  CONSTRAINT fk_barcode_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE barcode_print_record (
  id INT PRIMARY KEY AUTO_INCREMENT,
  barcode_id INT NOT NULL,
  printer_name VARCHAR(100),
  quantity INT DEFAULT 1,
  printed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  operator_id INT,
  CONSTRAINT fk_print_barcode FOREIGN KEY (barcode_id) REFERENCES barcode(id)
);
```

#### 1.4 开发工作量估算
- 后端开发：5人天
  - 条码生成逻辑
  - 打印服务集成
  - 扫码业务逻辑

- 前端开发：4人天
  - 条码管理界面
  - 批量打印功能
  - 扫码操作页面

- 移动端开发：6人天
  - 扫码组件开发
  - 移动端入库/出库操作
  - 离线功能支持

**总计：15人天**

#### 1.5 风险评估
- **技术风险**：中
  - 移动端摄像头权限控制
  - 不同设备条码识别精度差异

- **业务风险**：低
  - 条码标准选择不统一
  - 打印设备兼容性问题

#### 1.6 测试方案
- **单元测试**：条码生成算法、二维码识别精度
- **集成测试**：扫码入库/出库完整流程
- **性能测试**：批量生成1000个条码耗时
- **兼容性测试**：不同手机品牌扫码成功率

---

### 2. 批次管理

#### 2.1 功能需求分析
- 商品批次追踪：生产日期、过期日期
- 先进先出(FIFO)库存管理
- 批次库存查询和统计
- 批次质量追溯功能

#### 2.2 技术实现方案
```java
@Entity
@Table(name = "product_batch")
public class ProductBatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @Column(name = "warehouse_id")
    private Integer warehouseId;

    @Column(length = 255)
    private String remarks;
}

@Service
public class BatchService {

    // 入库时创建批次
    public ProductBatch createBatch(InboundRequest request) {
        ProductBatch batch = new ProductBatch();
        batch.setProduct(productRepository.findById(request.getProductId()).get());
        batch.setBatchNumber(generateBatchNumber());
        batch.setQuantity(request.getQuantity());
        batch.setRemainingQuantity(request.getQuantity());
        batch.setProductionDate(request.getProductionDate());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setWarehouseId(request.getWarehouseId());

        return batchRepository.save(batch);
    }

    // 出库时先进先出
    @Transactional
    public List<BatchConsume> consumeBatches(Integer productId, Integer quantity) {
        List<ProductBatch> batches = batchRepository.findByProductIdOrderByProductionDateAsc(productId);
        List<BatchConsume> consumed = new ArrayList<>();

        for (ProductBatch batch : batches) {
            if (batch.getRemainingQuantity() >= quantity) {
                consumed.add(new BatchConsume(batch.getId(), quantity));
                batch.setRemainingQuantity(batch.getRemainingQuantity() - quantity);
                quantity = 0;
            } else {
                consumed.add(new BatchConsume(batch.getId(), batch.getRemainingQuantity()));
                quantity -= batch.getRemainingQuantity();
                batch.setRemainingQuantity(0);
            }

            if (quantity == 0) break;
        }

        if (quantity > 0) {
            throw new RuntimeException("库存不足，无法完成出库");
        }

        return consumed;
    }
}
```

#### 2.3 数据库设计
```sql
CREATE TABLE product_batch (
  id INT PRIMARY KEY AUTO_INCREMENT,
  product_id INT NOT NULL,
  batch_number VARCHAR(50) NOT NULL,
  production_date DATE,
  expiry_date DATE,
  quantity INT NOT NULL,
  remaining_quantity INT NOT NULL,
  warehouse_id INT,
  remarks VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_product_batch (product_id, batch_number),
  INDEX idx_expiry_date (expiry_date),
  CONSTRAINT fk_batch_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE outbound_batch_detail (
  id INT PRIMARY KEY AUTO_INCREMENT,
  outbound_id INT NOT NULL,
  batch_id INT NOT NULL,
  quantity INT NOT NULL,
  CONSTRAINT fk_outbound_batch FOREIGN KEY (outbound_id) REFERENCES inbound(id),
  CONSTRAINT fk_batch_detail FOREIGN KEY (batch_id) REFERENCES product_batch(id)
);
```

#### 2.4 开发工作量估算
- 后端开发：7人天
- 前端开发：5人天
- 数据库迁移：1人天

**总计：13人天**

#### 2.5 风险评估
- **技术风险**：低
- **业务风险**：中
  - 批次管理逻辑复杂度
  - 历史数据迁移工作量

#### 2.6 测试方案
- FIFO算法正确性测试
- 过期商品提醒功能测试
- 批次追溯完整链路测试

---

### 3. 高级报表分析

#### 3.1 功能需求分析
- 实时库存分析报表
- 出入库趋势分析
- 商品周转率分析
- 库存预警报表
- 客户/供应商分析

#### 3.2 技术实现方案
```java
@Entity
@Table(name = "inventory_report")
public class InventoryReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "report_type")
    private String reportType;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal totalValue;

    private Integer totalItems;

    @Column(columnDefinition = "JSON")
    private String reportData;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}

@Service
public class ReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 库存分析报表
    public InventoryReport generateInventoryReport(LocalDate date) {
        String sql = "SELECT p.category_id, p.name, p.stock_qty, p.price, " +
                    "(p.stock_qty * p.price) as total_value " +
                    "FROM product p WHERE p.status = 1";

        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);

        InventoryReport report = new InventoryReport();
        report.setReportType("INVENTORY_ANALYSIS");
        report.setReportDate(date);
        report.setReportData(new ObjectMapper().writeValueAsString(data));

        BigDecimal totalValue = data.stream()
            .map(row -> new BigDecimal(row.get("total_value").toString()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalValue(totalValue);

        return reportRepository.save(report);
    }

    // 趋势分析报表
    public List<TrendData> getTrendAnalysis(String type, LocalDate startDate, LocalDate endDate) {
        String sql;
        switch (type) {
            case "INBOUND":
                sql = "SELECT DATE(created_at) as date, SUM(quantity) as total " +
                      "FROM inbound WHERE created_at BETWEEN ? AND ? " +
                      "GROUP BY DATE(created_at) ORDER BY date";
                break;
            case "OUTBOUND":
                sql = "SELECT DATE(created_at) as date, SUM(quantity) as total " +
                      "FROM outbound WHERE created_at BETWEEN ? AND ? " +
                      "GROUP BY DATE(created_at) ORDER BY date";
                break;
            default:
                throw new IllegalArgumentException("不支持的分析类型");
        }

        return jdbcTemplate.query(sql, new Object[]{startDate, endDate}, (rs, rowNum) -> {
            TrendData data = new TrendData();
            data.setDate(rs.getDate("date").toLocalDate());
            data.setValue(rs.getBigDecimal("total"));
            return data;
        });
    }

    // 周转率分析
    public List<TurnoverRate> getTurnoverRateAnalysis(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT p.id, p.name, " +
                    "COALESCE(SUM(ob.quantity), 0) as outbound_total, " +
                    "p.stock_qty as current_stock " +
                    "FROM product p " +
                    "LEFT JOIN outbound ob ON p.id = ob.product_id " +
                    "AND ob.created_at BETWEEN ? AND ? " +
                    "GROUP BY p.id, p.name, p.stock_qty";

        return jdbcTemplate.query(sql, new Object[]{startDate, endDate}, (rs, rowNum) -> {
            TurnoverRate rate = new TurnoverRate();
            rate.setProductId(rs.getInt("id"));
            rate.setProductName(rs.getString("name"));
            rate.setOutboundTotal(rs.getBigDecimal("outbound_total"));
            rate.setCurrentStock(rs.getInt("current_stock"));
            rate.setTurnoverRate(calculateTurnoverRate(
                rs.getBigDecimal("outbound_total"),
                rs.getInt("current_stock")
            ));
            return rate;
        });
    }
}
```

#### 3.3 前端界面设计
```vue
<template>
  <div class="report-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div slot="header">库存总价值</div>
          <div class="metric-value">¥{{ inventoryValue | formatMoney }}</div>
          <div class="metric-trend">
            <i :class="inventoryTrend > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
            {{ Math.abs(inventoryTrend) }}%
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card>
          <div slot="header">库存预警</div>
          <div class="metric-value">{{ lowStockCount }}</div>
          <div class="metric-desc">商品低于安全库存</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            出入库趋势
            <el-radio-group v-model="trendPeriod" size="mini" style="float: right;">
              <el-radio-button label="week">周</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
              <el-radio-button label="year">年</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div slot="header">分类库存分布</div>
          <div ref="categoryChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'ReportDashboard',
  data() {
    return {
      inventoryValue: 0,
      inventoryTrend: 0,
      lowStockCount: 0,
      trendPeriod: 'month'
    }
  },
  mounted() {
    this.loadReportData()
    this.initCharts()
  },
  methods: {
    async loadReportData() {
      const response = await this.$http.get('/api/reports/dashboard')
      this.inventoryValue = response.data.inventoryValue
      this.inventoryTrend = response.data.inventoryTrend
      this.lowStockCount = response.data.lowStockCount
    },

    initCharts() {
      this.initTrendChart()
      this.initCategoryChart()
    },

    initTrendChart() {
      const chart = echarts.init(this.$refs.trendChart)
      const option = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['入库', '出库'] },
        xAxis: { type: 'category' },
        yAxis: { type: 'value' },
        series: [
          { name: '入库', type: 'line', data: [] },
          { name: '出库', type: 'line', data: [] }
        ]
      }
      chart.setOption(option)
      this.trendChart = chart
    }
  }
}
</script>
```

#### 3.4 开发工作量估算
- 后端开发：8人天
  - 报表算法实现
  - 数据聚合优化
  - 缓存策略

- 前端开发：10人天
  - 图表组件开发
  - 数据可视化
  - 报表导出功能

**总计：18人天**

#### 3.5 风险评估
- **性能风险**：高
  - 大数据量查询性能问题
  - 实时报表计算复杂度

#### 3.6 测试方案
- 报表数据准确性测试
- 大数据量性能测试
- 并发访问测试

---

### 4. 移动端支持

#### 4.1 功能需求分析
- 移动端适配的入库、出库操作
- 移动端库存查询
- 扫码操作优化
- 离线功能支持
- 消息推送通知

#### 4.2 技术实现方案

**移动端架构选择：**
1. **响应式Web应用**（推荐）：基于Vue.js的移动端适配
2. **微信小程序**：集成微信扫码能力
3. **混合应用**：使用H5+原生壳

**推荐方案：** 响应式Web + 微信小程序

```vue
<!-- 移动端首页 -->
<template>
  <div class="mobile-layout">
    <!-- 顶部导航 -->
    <van-nav-bar
      title="智能仓储"
      :left-text="showBack ? '返回' : ''"
      :left-arrow="showBack"
      @click-left="handleBack"
    >
      <template #right>
        <van-icon name="bell" @click="showNotifications" />
      </template>
    </van-nav-bar>

    <!-- 主要功能区 -->
    <div class="main-functions">
      <van-grid :column-num="3" :gutter="12">
        <van-grid-item @click="navigateTo('/mobile/inbound')">
          <van-icon name="logistics" />
          <span>扫码入库</span>
        </van-grid-item>
        <van-grid-item @click="navigateTo('/mobile/outbound')">
          <van-icon name="send" />
          <span>扫码出库</span>
        </van-grid-item>
        <van-grid-item @click="navigateTo('/mobile/inventory')">
          <van-icon name="search" />
          <span>库存查询</span>
        </van-grid-item>
        <van-grid-item @click="navigateTo('/mobile/stock-check')">
          <van-icon name="records" />
          <span>盘点管理</span>
        </van-grid-item>
        <van-grid-item @click="navigateTo('/mobile/reports')">
          <van-icon name="chart-trending-o" />
          <span>报表查看</span>
        </van-grid-item>
        <van-grid-item @click="navigateTo('/mobile/settings')">
          <van-icon name="setting-o" />
          <span>系统设置</span>
        </van-grid-item>
      </van-grid>
    </div>

    <!-- 快速操作区 -->
    <div class="quick-actions">
      <van-cell-group title="快速操作">
        <van-cell
          title="扫码搜索"
          icon="scan"
          is-link
          @click="openScanner"
        />
        <van-cell
          title="最近操作"
          icon="clock-o"
          :value="recentOperations.length"
          is-link
          @click="showRecentOperations"
        />
      </van-cell-group>
    </div>

    <!-- 扫码组件 -->
    <van-popup v-model="showScanner" position="bottom" :style="{ height: '100%' }">
      <qr-scanner
        v-if="showScanner"
        @scan-success="handleScanSuccess"
        @scan-error="handleScanError"
        @close="showScanner = false"
      />
    </van-popup>
  </div>
</template>

<script>
import { Grid, GridItem, NavBar, Icon, Cell, CellGroup, Popup } from 'vant'
import QrScanner from '@/components/QrScanner.vue'

export default {
  name: 'MobileHome',
  components: {
    [Grid.name]: Grid,
    [GridItem.name]: GridItem,
    [NavBar.name]: NavBar,
    [Icon.name]: Icon,
    [Cell.name]: Cell,
    [CellGroup.name]: CellGroup,
    [Popup.name]: Popup,
    QrScanner
  },
  data() {
    return {
      showScanner: false,
      recentOperations: []
    }
  },
  mounted() {
    this.checkLoginStatus()
    this.loadRecentOperations()
  },
  methods: {
    openScanner() {
      if (this.checkCameraPermission()) {
        this.showScanner = true
      } else {
        this.$dialog.alert({
          title: '提示',
          message: '请授权相机权限以使用扫码功能'
        })
      }
    },

    handleScanSuccess(result) {
      this.showScanner = false
      this.processScanResult(result)
    },

    async processScanResult(scanResult) {
      try {
        const response = await this.$http.post('/api/mobile/scan-query', {
          code: scanResult
        })

        const { type, data } = response.data

        switch (type) {
          case 'PRODUCT':
            this.$router.push(`/mobile/product/${data.id}`)
            break
          case 'BATCH':
            this.$router.push(`/mobile/batch/${data.id}`)
            break
          case 'INBOUND':
            this.$router.push(`/mobile/inbound-detail/${data.id}`)
            break
          default:
            this.$toast.fail('未识别的条码类型')
        }
      } catch (error) {
        this.$toast.fail('扫码查询失败')
      }
    },

    checkCameraPermission() {
      // 检查相机权限逻辑
      return new Promise((resolve) => {
        navigator.mediaDevices.getUserMedia({ video: true })
          .then(() => resolve(true))
          .catch(() => resolve(false))
      })
    }
  }
}
</script>
```

#### 4.3 移动端专用API设计
```java
@RestController
@RequestMapping("/api/mobile")
public class MobileController {

    @Autowired
    private MobileService mobileService;

    // 移动端扫码查询
    @PostMapping("/scan-query")
    public Result<Object> scanQuery(@RequestBody ScanQueryRequest request) {
        return Result.success(mobileService.queryByBarcode(request.getCode()));
    }

    // 移动端入库
    @PostMapping("/inbound")
    public Result<InboundResponse> mobileInbound(@RequestBody MobileInboundRequest request) {
        return Result.success(mobileService.processMobileInbound(request));
    }

    // 获取快速操作数据
    @GetMapping("/quick-data")
    public Result<QuickDataResponse> getQuickData() {
        return Result.success(mobileService.getQuickData());
    }

    // 批量操作支持
    @PostMapping("/batch-operation")
    public Result<BatchOperationResponse> batchOperation(@RequestBody BatchOperationRequest request) {
        return Result.success(mobileService.processBatchOperation(request));
    }
}
```

#### 4.4 离线功能设计
```javascript
// 离线数据存储服务
class OfflineStorage {
  constructor() {
    this.db = new Dexie('WarehouseOfflineDB')
    this.db.version(1).stores({
      products: 'id, code, name, barcode',
      inboundOperations: '++id, barcode, quantity, timestamp',
      outboundOperations: '++id, barcode, quantity, timestamp',
      syncQueue: '++id, type, data, timestamp, status'
    })
  }

  // 保存离线入库操作
  async saveOfflineInbound(operation) {
    await this.db.inboundOperations.add({
      ...operation,
      timestamp: Date.now(),
      synced: false
    })

    // 加入同步队列
    await this.db.syncQueue.add({
      type: 'INBOUND',
      data: operation,
      timestamp: Date.now(),
      status: 'PENDING'
    })
  }

  // 同步离线数据
  async syncOfflineData() {
    const pendingOperations = await this.db.syncQueue
      .where('status').equals('PENDING')
      .toArray()

    for (const operation of pendingOperations) {
      try {
        await this.syncOperation(operation)
        operation.status = 'SYNCED'
        await this.db.syncQueue.put(operation)
      } catch (error) {
        console.error('同步失败:', error)
        operation.status = 'FAILED'
        await this.db.syncQueue.put(operation)
      }
    }
  }

  async syncOperation(operation) {
    switch (operation.type) {
      case 'INBOUND':
        return await this.$http.post('/api/mobile/inbound', operation.data)
      case 'OUTBOUND':
        return await this.$http.post('/api/mobile/outbound', operation.data)
      default:
        throw new Error(`未知的操作类型: ${operation.type}`)
    }
  }
}
```

#### 4.5 开发工作量估算
- 移动端适配：8人天
- 扫码功能：5人天
- 离线功能：6人天
- 消息推送：3人天

**总计：22人天**

#### 4.6 风险评估
- **兼容性风险**：高
  - 不同设备扫码识别率差异
  - iOS/Android权限管理差异

- **性能风险**：中
  - 移动网络环境下的数据同步
  - 大量离线数据的存储和同步

#### 4.7 测试方案
- 多设备兼容性测试
- 不同网络环境下的功能测试
- 离线/在线切换测试
- 扫码成功率测试

---

## 第一阶段总结

**总工作量估算：68人天**
- 条码管理：15人天
- 批次管理：13人天
- 高级报表：18人天
- 移动端支持：22人天

**预计工期：10-12周（按标准配置3人团队）**

**关键里程碑：**
- 第3周：条码管理上线
- 第5周：批次管理上线
- 第8周：报表系统上线
- 第12周：移动端功能完整上线

---

## 第二阶段：企业级功能扩展

### 1. 多仓库管理

#### 1.1 功能需求分析
- 支持多个仓库的统一管理
- 仓库间库存调拨
- 分仓库权限控制
- 跨仓库业务流程

#### 1.2 技术实现方案
```java
@Entity
@Table(name = "warehouse_config")
public class WarehouseConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String address;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "storage_capacity")
    private BigDecimal storageCapacity;

    @Column(name = "current_usage")
    private BigDecimal currentUsage;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status;
}

@Entity
@Table(name = "warehouse_transfer")
public class WarehouseTransfer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "from_warehouse_id")
    private Warehouse fromWarehouse;

    @ManyToOne
    @JoinColumn(name = "to_warehouse_id")
    private Warehouse toWarehouse;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "transfer_date")
    private LocalDateTime transferDate;

    @Column(length = 20)
    private String status; // PENDING, APPROVED, COMPLETED, CANCELLED

    @Column(length = 255)
    private String remarks;
}

@Service
public class MultiWarehouseService {

    @Transactional
    public WarehouseTransfer createTransfer(TransferRequest request) {
        // 验证源仓库库存
        validateSourceStock(request.getFromWarehouseId(),
                           request.getProductId(),
                           request.getQuantity());

        // 创建调拨记录
        WarehouseTransfer transfer = new WarehouseTransfer();
        transfer.setFromWarehouse(warehouseRepository.findById(request.getFromWarehouseId()).get());
        transfer.setToWarehouse(warehouseRepository.findById(request.getToWarehouseId()).get());
        transfer.setProduct(productRepository.findById(request.getProductId()).get());
        transfer.setQuantity(request.getQuantity());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setStatus("PENDING");
        transfer.setRemarks(request.getRemarks());

        return transferRepository.save(transfer);
    }

    @Transactional
    public void executeTransfer(Integer transferId) {
        WarehouseTransfer transfer = transferRepository.findById(transferId)
            .orElseThrow(() -> new RuntimeException("调拨记录不存在"));

        if (!"APPROVED".equals(transfer.getStatus())) {
            throw new RuntimeException("调拨未批准，无法执行");
        }

        // 扣除源仓库库存
        decreaseWarehouseStock(transfer.getFromWarehouse().getId(),
                             transfer.getProduct().getId(),
                             transfer.getQuantity());

        // 增加目标仓库库存
        increaseWarehouseStock(transfer.getToWarehouse().getId(),
                             transfer.getProduct().getId(),
                             transfer.getQuantity());

        transfer.setStatus("COMPLETED");
        transferRepository.save(transfer);
    }

    public WarehouseStatisticsDTO getWarehouseStatistics(Integer warehouseId) {
        WarehouseStatisticsDTO stats = new WarehouseStatisticsDTO();

        // 计算库存总价值
        String sql = "SELECT SUM(p.stock_qty * p.price) as total_value " +
                    "FROM product p WHERE p.warehouse_id = ?";
        BigDecimal totalValue = jdbcTemplate.queryForObject(
            sql, new Object[]{warehouseId}, BigDecimal.class);
        stats.setTotalValue(totalValue);

        // 计算库存周转率
        stats.setTurnoverRate(calculateWarehouseTurnoverRate(warehouseId));

        // 计算仓库利用率
        stats.setUtilizationRate(calculateUtilizationRate(warehouseId));

        return stats;
    }
}
```

#### 1.3 数据库设计
```sql
CREATE TABLE warehouse_config (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50) NOT NULL UNIQUE,
  address VARCHAR(255),
  manager_id INT,
  storage_capacity DECIMAL(15,2),
  current_usage DECIMAL(15,2),
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_manager (manager_id),
  CONSTRAINT fk_warehouse_manager FOREIGN KEY (manager_id) REFERENCES user(id)
);

CREATE TABLE warehouse_stock (
  id INT PRIMARY KEY AUTO_INCREMENT,
  warehouse_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 0,
  batch_number VARCHAR(50),
  location_id INT,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_warehouse_product (warehouse_id, product_id, batch_number),
  CONSTRAINT fk_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse_config(id),
  CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE warehouse_transfer (
  id INT PRIMARY KEY AUTO_INCREMENT,
  from_warehouse_id INT NOT NULL,
  to_warehouse_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  transfer_date DATETIME,
  status VARCHAR(20) NOT NULL,
  operator_id INT,
  approver_id INT,
  remarks VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_from_warehouse (from_warehouse_id),
  INDEX idx_to_warehouse (to_warehouse_id),
  INDEX idx_status (status)
);
```

#### 1.4 开发工作量估算
- 后端开发：12人天
- 前端开发：10人天
- 数据库设计：2人天

**总计：24人天**

---

### 2. 权限精细化管理

#### 2.1 功能需求分析
- 基于角色的权限控制(RBAC)
- 菜单权限控制
- 数据权限控制
- 操作权限控制
- 权限继承机制

#### 2.2 技术实现方案
```java
@Entity
@Table(name = "permission")
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "permission_type")
    private String type; // MENU, OPERATION, DATA

    @Column(name = "resource_path")
    private String resourcePath;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Permission parent;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;
}

@Entity
@Table(name = "role")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "data_scope")
    private String dataScope; // ALL, DEPT, SELF

    @ManyToMany
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}

@Service
public class PermissionService {

    public List<PermissionDTO> getPermissionTree() {
        List<Permission> permissions = permissionRepository.findAllByOrderBySortOrderAsc();
        return buildPermissionTree(permissions, null);
    }

    private List<PermissionDTO> buildPermissionTree(List<Permission> permissions, Integer parentId) {
        return permissions.stream()
            .filter(p -> parentId == null ? p.getParent() == null :
                         p.getParent() != null && p.getParent().getId().equals(parentId))
            .map(this::convertToDTO)
            .peek(dto -> {
                List<PermissionDTO> children = buildPermissionTree(permissions, dto.getId());
                dto.setChildren(children);
            })
            .sorted(Comparator.comparing(PermissionDTO::getSortOrder))
            .collect(Collectors.toList());
    }

    public boolean hasPermission(Integer userId, String permissionCode) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(p -> p.getCode().equals(permissionCode));
    }

    public boolean hasDataPermission(Integer userId, String dataType, Integer dataId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        for (Role role : user.getRoles()) {
            switch (role.getDataScope()) {
                case "ALL":
                    return true;
                case "DEPT":
                    return hasDeptPermission(user, dataType, dataId);
                case "SELF":
                    return hasSelfPermission(user, dataType, dataId);
            }
        }

        return false;
    }
}

// 自定义权限拦截器
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);

        if (requirePermission == null) {
            return true;
        }

        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            response.setStatus(401);
            return false;
        }

        // 检查权限
        for (String permission : requirePermission.value()) {
            if (!permissionService.hasPermission(userId, permission)) {
                response.setStatus(403);
                return false;
            }
        }

        // 检查数据权限
        if (requirePermission.checkData()) {
            Integer dataId = extractDataId(request);
            if (dataId != null && !permissionService.hasDataPermission(userId,
                requirePermission.dataType(), dataId)) {
                response.setStatus(403);
                return false;
            }
        }

        return true;
    }
}
```

#### 2.3 前端权限控制
```vue
<template>
  <div class="permission-controlled-menu">
    <template v-for="menu in menuTree">
      <el-submenu
        v-if="hasChildren(menu) && hasMenuPermission(menu.permission)"
        :key="menu.id"
        :index="menu.id.toString()"
      >
        <template slot="title">
          <i :class="menu.icon"></i>
          <span>{{ menu.name }}</span>
        </template>

        <permission-controlled-menu :menu-tree="menu.children" />
      </el-submenu>

      <el-menu-item
        v-else-if="hasMenuPermission(menu.permission)"
        :key="menu.id"
        :index="menu.path"
      >
        <i :class="menu.icon"></i>
        <span slot="title">{{ menu.name }}</span>
      </el-menu-item>
    </template>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'PermissionControlledMenu',
  props: {
    menuTree: {
      type: Array,
      required: true
    }
  },
  computed: {
    ...mapState('user', ['permissions'])
  },
  methods: {
    hasChildren(menu) {
      return menu.children && menu.children.length > 0
    },

    hasMenuPermission(permission) {
      if (!permission) return true
      return this.permissions.includes(permission)
    }
  }
}
</script>
```

#### 2.4 开发工作量估算
- 后端开发：10人天
- 前端开发：8人天
- 权限配置界面：4人天

**总计：22人天**

---

### 3. 智能预测补货

#### 3.1 功能需求分析
- 基于历史销售数据的库存预测
- 安全库存自动计算
- 补货提醒和建议
- 季节性需求预测
- 供应商交货周期考虑

#### 3.2 技术实现方案
```java
@Entity
@Table(name = "sales_forecast")
public class SalesForecast {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "forecast_date")
    private LocalDate forecastDate;

    @Column(name = "forecast_quantity")
    private BigDecimal forecastQuantity;

    @Column(name = "confidence_level")
    private BigDecimal confidenceLevel;

    @Column(name = "forecast_model")
    private String forecastModel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

@Service
public class InventoryForecastService {

    @Autowired
    private SalesForecastRepository forecastRepository;

    // 基于历史数据的销售预测
    public SalesForecast generateForecast(Integer productId, LocalDate startDate, LocalDate endDate) {
        List<SalesData> historicalData = getHistoricalSalesData(productId, startDate.minusMonths(12), startDate);

        // 使用移动平均法进行预测
        BigDecimal movingAvg = calculateMovingAverage(historicalData);

        // 考虑季节性因素
        BigDecimal seasonalFactor = calculateSeasonalFactor(productId, startDate);

        // 考虑趋势因素
        BigDecimal trendFactor = calculateTrendFactor(historicalData);

        BigDecimal finalForecast = movingAvg.multiply(seasonalFactor).multiply(trendFactor);

        SalesForecast forecast = new SalesForecast();
        forecast.setProduct(productRepository.findById(productId).get());
        forecast.setForecastDate(endDate);
        forecast.setForecastQuantity(finalForecast);
        forecast.setConfidenceLevel(calculateConfidenceLevel(historicalData));
        forecast.setForecastModel("MOVING_AVG_SEASONAL");

        return forecastRepository.save(forecast);
    }

    // 安全库存计算
    public ReorderPoint calculateReorderPoint(Integer productId) {
        // 获取平均日销量
        BigDecimal avgDailyDemand = getAverageDailyDemand(productId);

        // 获取平均供应商交货周期
        BigDecimal avgLeadTime = getAverageLeadTime(productId);

        // 获取需求标准差
        BigDecimal demandStdDev = getDemandStandardDeviation(productId);

        // 服务水平系数（95%服务水平对应的Z值）
        double serviceLevel = 1.65;

        // 安全库存 = Z * σd * √L
        BigDecimal safetyStock = BigDecimal.valueOf(serviceLevel)
            .multiply(demandStdDev)
            .multiply(BigDecimal.valueOf(Math.sqrt(avgLeadTime.doubleValue())));

        // 再订货点 = 平均日销量 * 平均交货周期 + 安全库存
        BigDecimal reorderPoint = avgDailyDemand.multiply(avgLeadTime).add(safetyStock);

        ReorderPoint point = new ReorderPoint();
        point.setProductId(productId);
        point.setSafetyStock(safetyStock);
        point.setReorderPoint(reorderPoint);
        point.setOrderQuantity(calculateOptimalOrderQuantity(productId, avgDailyDemand));

        return point;
    }

    // 生成补货建议
    public List<ReplenishmentSuggestion> generateReplenishmentSuggestions() {
        List<ReplenishmentSuggestion> suggestions = new ArrayList<>();

        // 获取所有活跃商品
        List<Product> activeProducts = productRepository.findByStatus(1);

        for (Product product : activeProducts) {
            ReorderPoint reorderPoint = calculateReorderPoint(product.getId());

            if (product.getStockQty() <= reorderPoint.getReorderPoint().intValue()) {
                ReplenishmentSuggestion suggestion = new ReplenishmentSuggestion();
                suggestion.setProduct(product);
                suggestion.setCurrentStock(product.getStockQty());
                suggestion.setReorderPoint(reorderPoint.getReorderPoint());
                suggestion.setSuggestedQuantity(reorderPoint.getOrderQuantity());
                suggestion.setUrgencyLevel(calculateUrgencyLevel(product.getStockQty(), reorderPoint));
                suggestion.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(product));

                suggestions.add(suggestion);
            }
        }

        return suggestions.stream()
            .sorted(Comparator.comparing(ReplenishmentSuggestion::getUrgencyLevel).reversed())
            .collect(Collectors.toList());
    }
}
```

#### 3.3 机器学习集成（可选）
```java
@Service
public class MLForecastService {

    @Autowired
    private RestTemplate restTemplate;

    // 调用Python机器学习服务进行预测
    public SalesForecast generateMLForecast(Integer productId, LocalDate targetDate) {
        // 准备历史数据
        Map<String, Object> requestData = prepareMLData(productId, targetDate);

        // 调用Python ML服务
        MLForecastResponse response = restTemplate.postForObject(
            "http://ml-service:5000/forecast",
            requestData,
            MLForecastResponse.class
        );

        // 转换为系统实体
        SalesForecast forecast = new SalesForecast();
        forecast.setProduct(productRepository.findById(productId).get());
        forecast.setForecastDate(targetDate);
        forecast.setForecastQuantity(BigDecimal.valueOf(response.getPrediction()));
        forecast.setConfidenceLevel(BigDecimal.valueOf(response.getConfidence()));
        forecast.setForecastModel(response.getModel());

        return forecast;
    }

    private Map<String, Object> prepareMLData(Integer productId, LocalDate targetDate) {
        Map<String, Object> data = new HashMap<>();

        // 获取最近24个月的销售数据
        List<SalesData> salesData = getHistoricalSalesData(productId,
            targetDate.minusMonths(24), targetDate);

        data.put("product_id", productId);
        data.put("historical_sales", salesData);
        data.put("target_date", targetDate);
        data.put("features", extractFeatures(salesData));

        return data;
    }
}
```

#### 3.4 前端界面设计
```vue
<template>
  <div class="forecast-dashboard">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <div slot="header">
            <span>智能补货建议</span>
            <el-button-group style="float: right;">
              <el-button size="mini" @click="refreshForecast">
                <i class="el-icon-refresh"></i> 刷新预测
              </el-button>
              <el-button size="mini" @click="exportSuggestions">
                <i class="el-icon-download"></i> 导出建议
              </el-button>
            </el-button-group>
          </div>

          <el-table :data="suggestions" stripe>
            <el-table-column prop="productName" label="商品名称" width="150" />
            <el-table-column prop="currentStock" label="当前库存" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStockType(scope.row.currentStock, scope.row.reorderPoint)">
                  {{ scope.row.currentStock }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reorderPoint" label="再订货点" width="120" />
            <el-table-column prop="suggestedQuantity" label="建议补货量" width="120" />
            <el-table-column prop="urgencyLevel" label="紧急程度" width="120">
              <template slot-scope="scope">
                <el-tag :type="getUrgencyType(scope.row.urgencyLevel)">
                  {{ getUrgencyText(scope.row.urgencyLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="estimatedDeliveryDate" label="预计到货日期" width="150" />
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button size="mini" @click="viewDetails(scope.row)">详情</el-button>
                <el-button type="primary" size="mini" @click="createPurchase(scope.row)">
                  创建采购
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预测趋势图 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <div slot="header">销售趋势预测</div>
          <div ref="forecastChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">库存水平预测</div>
          <div ref="inventoryChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'ForecastDashboard',
  data() {
    return {
      suggestions: []
    }
  },
  mounted() {
    this.loadSuggestions()
    this.initCharts()
  },
  methods: {
    async loadSuggestions() {
      const response = await this.$http.get('/api/forecast/replenishment-suggestions')
      this.suggestions = response.data
    },

    getUrgencyType(level) {
      const types = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
      return types[level] || 'info'
    },

    getUrgencyText(level) {
      const texts = { HIGH: '紧急', MEDIUM: '中等', LOW: '一般' }
      return texts[level] || '一般'
    },

    async createPurchase(suggestion) {
      try {
        await this.$confirm('确定要创建采购订单吗？', '确认')

        const response = await this.$http.post('/api/purchase/create-from-forecast', {
          productId: suggestion.productId,
          quantity: suggestion.suggestedQuantity,
          urgencyLevel: suggestion.urgencyLevel
        })

        this.$message.success('采购订单创建成功')
        this.loadSuggestions()
      } catch (error) {
        this.$message.error('创建采购订单失败')
      }
    }
  }
}
</script>
```

#### 3.5 开发工作量估算
- 预测算法开发：15人天
- 机器学习集成：10人天（可选）
- 前端界面：8人天
- 数据分析和处理：7人天

**总计：40人天**

---

### 4. 第三方系统集成

#### 4.1 功能需求分析
- ERP系统集成
- 财务系统对接
- 电商平台API对接
- 物流系统集成
- 电子发票系统集成

#### 4.2 技术实现方案
```java
// 集成配置实体
@Entity
@Table(name = "integration_config")
public class IntegrationConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String systemCode; // ERP, FINANCE, ECOMMERCE, LOGISTICS

    @Column(nullable = false, length = 100)
    private String systemName;

    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;

    @Column(name = "api_key", length = 255)
    private String apiKey;

    @Column(name = "api_secret", length = 255)
    private String apiSecret;

    @Column(name = "webhook_url", length = 255)
    private String webhookUrl;

    @Column(columnDefinition = "JSON")
    private String additionalConfig;

    @Column(name = "is_active")
    private Boolean isActive = true;
}

// ERP集成服务
@Service
public class ERPIntegrationService {

    @Autowired
    private ERPClient erpClient;

    // 同步产品到ERP
    @Async
    public CompletableFuture<Boolean> syncProductToERP(Integer productId) {
        try {
            Product product = productRepository.findById(productId).get();

            ERPProductDTO erpProduct = new ERPProductDTO();
            erpProduct.setCode(product.getCode());
            erpProduct.setName(product.getName());
            erpProduct.setPrice(product.getPrice());
            erpProduct.setCategory(product.getCategory().getName());

            ERPResponse response = erpClient.createOrUpdateProduct(erpProduct);

            if (response.isSuccess()) {
                // 更新同步状态
                updateSyncStatus(productId, "ERP", "SUCCESS", response.getExternalId());
                return CompletableFuture.completedFuture(true);
            } else {
                updateSyncStatus(productId, "ERP", "FAILED", response.getErrorMessage());
                return CompletableFuture.completedFuture(false);
            }
        } catch (Exception e) {
            updateSyncStatus(productId, "ERP", "ERROR", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    // 从ERP同步订单
    @Scheduled(fixedDelay = 300000) // 每5分钟执行一次
    public void syncOrdersFromERP() {
        try {
            List<ERPOrderDTO> orders = erpClient.getPendingOrders();

            for (ERPOrderDTO erpOrder : orders) {
                processERPOrder(erpOrder);
            }
        } catch (Exception e) {
            logger.error("ERP订单同步失败", e);
        }
    }
}

// 电商平台集成
@Service
public class ECommerceIntegrationService {

    // 同步库存到电商平台
    @Async
    public CompletableFuture<Boolean> syncInventoryToPlatform(Integer productId, String platformCode) {
        try {
            Product product = productRepository.findById(productId).get();
            PlatformConfig config = getPlatformConfig(platformCode);

            PlatformInventoryDTO inventory = new PlatformInventoryDTO();
            inventory.setProductCode(product.getCode());
            inventory.setQuantity(product.getStockQty());
            inventory.setPrice(product.getPrice());

            PlatformResponse response = platformClient(config).updateInventory(inventory);

            return CompletableFuture.completedFuture(response.isSuccess());
        } catch (Exception e) {
            logger.error("库存同步失败", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    // 处理电商平台订单
    @EventListener
    public void handlePlatformOrder(PlatformOrderEvent event) {
        try {
            PlatformOrder order = event.getOrder();

            // 创建系统出库单
            Outbound outbound = new Outbound();
            outbound.setProduct(findProductByPlatformCode(order.getProductCode()));
            outbound.setQuantity(order.getQuantity());
            outbound.setCustomer(findOrCreateCustomer(order.getCustomerInfo()));
            outbound.setOrderNumber(order.getPlatformOrderNumber());
            outbound.setOrderSource(order.getPlatformCode());

            outboundRepository.save(outbound);

            // 扣减库存
            productService.decreaseStock(outbound.getProduct().getId(), outbound.getQuantity());

            // 确认订单给平台
            platformClient.confirmOrder(order.getOrderId(), "CONFIRMED");

        } catch (Exception e) {
            logger.error("处理平台订单失败", e);
            // 异常处理和重试机制
        }
    }
}

// 通用集成客户端
@Component
public class IntegrationClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${integration.timeout:30000}")
    private int timeout;

    public <T, R> R callAPI(IntegrationConfig config, String endpoint, T request, Class<R> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + config.getApiKey());
            headers.set("Content-Type", "application/json");

            HttpEntity<T> entity = new HttpEntity<>(request, headers);

            ResponseEntity<R> response = restTemplate.exchange(
                config.getApiEndpoint() + endpoint,
                HttpMethod.POST,
                entity,
                responseType
            );

            return response.getBody();
        } catch (Exception e) {
            logger.error("API调用失败: {}", config.getSystemCode(), e);
            throw new IntegrationException("API调用失败", e);
        }
    }

    // 重试机制
    @Retryable(value = { IntegrationException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public <T, R> R callAPIWithRetry(IntegrationConfig config, String endpoint, T request, Class<R> responseType) {
        return callAPI(config, endpoint, request, responseType);
    }
}
```

#### 4.3 Webhook处理器
```java
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @PostMapping("/ecommerce/{platform}")
    public ResponseEntity<String> handleECommerceWebhook(
            @PathVariable String platform,
            @RequestBody String payload,
            @RequestHeader("X-Platform-Signature") String signature) {

        try {
            // 验证签名
            if (!validateWebhookSignature(platform, payload, signature)) {
                return ResponseEntity.status(401).body("Invalid signature");
            }

            // 解析webhook数据
            WebhookEvent event = parseWebhookEvent(platform, payload);

            // 发布事件进行处理
            applicationEventPublisher.publishEvent(event);

            return ResponseEntity.ok("Webhook received");

        } catch (Exception e) {
            logger.error("Webhook处理失败", e);
            return ResponseEntity.status(500).body("Processing failed");
        }
    }

    @PostMapping("/logistics/{provider}")
    public ResponseEntity<String> handleLogisticsWebhook(
            @PathVariable String provider,
            @RequestBody LogisticsUpdate update) {

        // 处理物流状态更新
        logisticsService.updateLogisticsStatus(update);

        return ResponseEntity.ok("Status updated");
    }
}
```

#### 4.4 开发工作量估算
- ERP集成：12人天
- 电商集成：10人天
- 物流集成：8人天
- 财务集成：6人天
- 通用集成框架：5人天

**总计：41人天**

---

## 第二阶段总结

**总工作量估算：127人天**
- 多仓库管理：24人天
- 权限精细化管理：22人天
- 智能预测补货：40人天
- 第三方系统集成：41人天

**预计工期：16-20周（按标准配置3-4人团队）**

---

## 第三阶段：智能化和国际化

### 1. AI智能化功能

#### 1.1 功能需求分析
- 智能商品分类推荐
- 异常数据检测和预警
- 智能客服支持
- 语音助手集成
- 图像识别库存盘点

#### 1.2 技术实现方案
```java
// AI服务接口
@Service
public class AIService {

    @Autowired
    private TensorFlowService tensorFlowService;

    @Autowired
    private NLPService nlpService;

    // 异常检测
    public List<InventoryAnomaly> detectInventoryAnomalies() {
        List<Product> products = productRepository.findAll();
        List<InventoryAnomaly> anomalies = new ArrayList<>();

        for (Product product : products) {
            // 使用LSTM模型预测正常库存范围
            InventoryPrediction prediction = tensorFlowService.predictInventory(
                product.getId(), LocalDate.now());

            // 检测实际库存是否在预测范围内
            if (product.getStockQty() < prediction.getLowerBound() ||
                product.getStockQty() > prediction.getUpperBound()) {

                InventoryAnomaly anomaly = new InventoryAnomaly();
                anomaly.setProduct(product);
                anomaly.setActualStock(product.getStockQty());
                anomaly.setExpectedRange(prediction.getLowerBound() + "-" + prediction.getUpperBound());
                anomaly.setAnomalyType(product.getStockQty() < prediction.getLowerBound() ?
                                     "LOW_STOCK" : "OVER_STOCK");
                anomaly.setConfidence(prediction.getConfidence());

                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    // 智能商品分类
    public String classifyProduct(String productName, String description) {
        // 使用BERT模型进行文本分类
        ClassificationRequest request = new ClassificationRequest();
        request.setText(productName + " " + description);
        request.setModel("product_classifier_v2");

        ClassificationResponse response = nlpService.classify(request);
        return response.getPredictedCategory();
    }

    // 智能推荐
    public List<ProductRecommendation> getRecommendations(Integer userId, String context) {
        // 基于用户行为和商品相似度的推荐
        UserBehavior behavior = behaviorService.getUserBehavior(userId);

        return recommendationEngine.getRecommendations(behavior, context);
    }
}

// 图像识别服务
@Service
public class ImageRecognitionService {

    @Autowired
    private OpenCVService openCVService;

    // 商品识别
    public ProductRecognitionResult recognizeProduct(MultipartFile image) {
        try {
            // 预处理图像
            Mat processedImage = openCVService.preprocessImage(image);

            // 使用YOLO模型检测商品
            List<DetectionResult> detections = yoloService.detect(processedImage);

            ProductRecognitionResult result = new ProductRecognitionResult();

            for (DetectionResult detection : detections) {
                if (detection.getConfidence() > 0.8) {
                    // 根据识别的特征查找匹配的商品
                    Product product = findProductByFeatures(detection.getFeatures());
                    if (product != null) {
                        result.setProduct(product);
                        result.setConfidence(detection.getConfidence());
                        result.setBoundingBox(detection.getBoundingBox());
                        break;
                    }
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("图像识别失败", e);
            throw new ImageRecognitionException("识别失败", e);
        }
    }

    // 库存盘点图像识别
    public InventoryCountResult recognizeInventoryCount(MultipartFile shelfImage) {
        List<DetectionResult> detections = yoloService.detect(shelfImage);

        Map<Integer, Integer> productCounts = new HashMap<>();

        for (DetectionResult detection : detections) {
            if (detection.getConfidence() > 0.7) {
                Product product = findProductByVisualFeatures(detection);
                if (product != null) {
                    productCounts.merge(product.getId(), 1, Integer::sum);
                }
            }
        }

        InventoryCountResult result = new InventoryCountResult();
        result.setProductCounts(productCounts);
        result.setTotalItems(productCounts.values().stream().mapToInt(Integer::intValue).sum());
        result.setConfidence(calculateOverallConfidence(detections));

        return result;
    }
}
```

#### 1.3 开发工作量估算
- AI模型集成：20人天
- 异常检测算法：12人天
- 图像识别功能：15人天
- 智能推荐系统：10人天

**总计：57人天**

---

### 2. 语音操作

#### 2.1 功能需求分析
- 语音命令识别和执行
- 语音查询库存信息
- 语音录入入库/出库数据
- 多语言语音识别支持

#### 2.2 技术实现方案
```java
// 语音服务
@Service
public class VoiceService {

    @Autowired
    private SpeechToTextService sttService;

    @Autowired
    private TextToSpeechService ttsService;

    @Autowired
    private NLPService nlpService;

    // 处理语音命令
    public VoiceCommandResult processVoiceCommand(MultipartFile audioFile, Integer userId) {
        try {
            // 语音转文字
            String transcription = sttService.transcribe(audioFile);

            // 意图识别
            Intent intent = nlpService.recognizeIntent(transcription);

            // 执行命令
            Object result = executeCommand(intent, userId);

            // 生成语音回复
            String responseText = generateResponseText(intent, result);
            byte[] responseAudio = ttsService.synthesize(responseText);

            VoiceCommandResult commandResult = new VoiceCommandResult();
            commandResult.setTranscription(transcription);
            commandResult.setIntent(intent);
            commandResult.setResult(result);
            commandResult.setResponseText(responseText);
            commandResult.setResponseAudio(responseAudio);

            return commandResult;

        } catch (Exception e) {
            logger.error("语音命令处理失败", e);
            return VoiceCommandResult.error("语音处理失败");
        }
    }

    private Object executeCommand(Intent intent, Integer userId) {
        switch (intent.getAction()) {
            case "QUERY_INVENTORY":
                return queryInventory(intent.getEntities());
            case "CREATE_INBOUND":
                return createInbound(intent.getEntities(), userId);
            case "CREATE_OUTBOUND":
                return createOutbound(intent.getEntities(), userId);
            case "QUERY_PRICE":
                return queryPrice(intent.getEntities());
            default:
                throw new UnsupportedOperationException("不支持的命令类型: " + intent.getAction());
        }
    }

    private InventoryInfo queryInventory(Map<String, String> entities) {
        String productCode = entities.get("product_code");
        String productName = entities.get("product_name");

        Product product;
        if (productCode != null) {
            product = productRepository.findByCode(productCode);
        } else if (productName != null) {
            product = productRepository.findByNameContaining(productName).stream()
                .findFirst().orElse(null);
        } else {
            throw new IllegalArgumentException("请指定商品名称或编号");
        }

        if (product == null) {
            throw new IllegalArgumentException("未找到指定商品");
        }

        InventoryInfo info = new InventoryInfo();
        info.setProduct(product);
        info.setStockQuantity(product.getStockQty());
        info.setAvailableQuantity(product.getStockQty()); // 简化处理
        info.setLocation(getProductLocation(product.getId()));

        return info;
    }
}
```

#### 2.3 前端语音组件
```vue
<template>
  <div class="voice-assistant">
    <div class="voice-controls">
      <el-button
        type="primary"
        circle
        :loading="isRecording"
        @mousedown="startRecording"
        @mouseup="stopRecording"
        @touchstart="startRecording"
        @touchend="stopRecording"
      >
        <i class="el-icon-microphone"></i>
      </el-button>

      <div class="voice-status" v-if="voiceStatus">
        {{ voiceStatus }}
      </div>
    </div>

    <!-- 识别结果展示 -->
    <el-dialog
      title="语音命令"
      :visible.sync="showVoiceResult"
      width="50%"
    >
      <div class="voice-result">
        <div class="transcription">
          <strong>识别内容：</strong>{{ voiceResult.transcription }}
        </div>
        <div class="intent">
          <strong>意图：</strong>{{ getIntentText(voiceResult.intent) }}
        </div>
        <div class="response">
          <strong>回复：</strong>
          <el-button
            size="mini"
            @click="playResponse"
            :disabled="!voiceResult.responseAudio"
          >
            <i class="el-icon-video-play"></i> 播放回复
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'VoiceAssistant',
  data() {
    return {
      isRecording: false,
      voiceStatus: '',
      showVoiceResult: false,
      voiceResult: {},
      mediaRecorder: null,
      audioChunks: []
    }
  },
  methods: {
    async startRecording() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        this.mediaRecorder = new MediaRecorder(stream)
        this.audioChunks = []

        this.mediaRecorder.ondataavailable = (event) => {
          this.audioChunks.push(event.data)
        }

        this.mediaRecorder.onstop = () => {
          this.processRecording()
        }

        this.mediaRecorder.start()
        this.isRecording = true
        this.voiceStatus = '正在录音...'

      } catch (error) {
        this.$message.error('无法访问麦克风')
      }
    },

    stopRecording() {
      if (this.mediaRecorder && this.isRecording) {
        this.mediaRecorder.stop()
        this.isRecording = false
        this.voiceStatus = '处理中...'
      }
    },

    async processRecording() {
      const audioBlob = new Blob(this.audioChunks, { type: 'audio/wav' })
      const formData = new FormData()
      formData.append('audio', audioBlob)

      try {
        const response = await this.$http.post('/api/voice/command', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })

        this.voiceResult = response.data
        this.showVoiceResult = true
        this.voiceStatus = ''

        // 自动播放回复
        if (this.voiceResult.responseAudio) {
          this.playAudioResponse(this.voiceResult.responseAudio)
        }

      } catch (error) {
        this.voiceStatus = '语音处理失败'
        this.$message.error('语音处理失败')
      }
    },

    playAudioResponse(audioData) {
      const audio = new Audio(`data:audio/wav;base64,${audioData}`)
      audio.play()
    },

    getIntentText(intent) {
      const intentMap = {
        'QUERY_INVENTORY': '查询库存',
        'CREATE_INBOUND': '创建入库',
        'CREATE_OUTBOUND': '创建出库'
      }
      return intentMap[intent?.action] || '未知意图'
    }
  }
}
</script>
```

#### 2.4 开发工作量估算
- 语音识别集成：15人天
- 自然语言处理：12人天
- 语音合成：8人天
- 前端语音组件：5人天

**总计：40人天**

---

### 3. 图像识别

#### 3.1 功能需求分析
- 商品图像识别和匹配
- 仓库货架自动识别
- 库存盘点图像处理
- 异常情况图像检测

#### 3.2 技术实现方案
```java
// 图像识别控制器
@RestController
@RequestMapping("/api/image-recognition")
public class ImageRecognitionController {

    @Autowired
    private ImageRecognitionService imageRecognitionService;

    @PostMapping("/recognize-product")
    public ResponseEntity<ProductRecognitionResult> recognizeProduct(
            @RequestParam("image") MultipartFile image) {
        ProductRecognitionResult result = imageRecognitionService.recognizeProduct(image);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/inventory-count")
    public ResponseEntity<InventoryCountResult> countInventory(
            @RequestParam("image") MultipartFile shelfImage,
            @RequestParam(value = "warehouseId", required = false) Integer warehouseId) {
        InventoryCountResult result = imageRecognitionService.recognizeInventoryCount(shelfImage);

        // 如果指定了仓库，保存盘点记录
        if (warehouseId != null) {
            inventoryCountService.saveCountRecord(warehouseId, result);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/detect-anomalies")
    public ResponseEntity<List<AnomalyDetectionResult>> detectAnomalies(
            @RequestParam("image") MultipartFile image,
            @RequestParam("type") String detectionType) {
        List<AnomalyDetectionResult> results = imageRecognitionService.detectAnomalies(image, detectionType);
        return ResponseEntity.ok(results);
    }
}
```

#### 3.3 开发工作量估算
- 图像识别模型训练：18人天
- 图像处理算法：12人天
- 移动端图像识别：8人天
- 异常检测算法：7人天

**总计：45人天**

---

### 4. 国际化支持

#### 4.1 功能需求分析
- 多语言界面支持
- 多时区处理
- 多货币支持
- 本地化数据格式
- 多语言文档支持

#### 4.2 技术实现方案
```java
// 国际化配置
@Configuration
public class InternationalizationConfig {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}

// 多语言消息服务
@Service
public class MessageService {

    @Autowired
    private MessageSource messageSource;

    public String getMessage(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }

    public String getMessage(String key, Locale locale) {
        return getMessage(key, null, locale);
    }

    // 动态消息翻译
    public String translateText(String text, Locale targetLocale) {
        if (targetLocale.equals(Locale.SIMPLIFIED_CHINESE)) {
            return text; // 中文直接返回
        }

        // 调用翻译API进行动态翻译
        return translationService.translate(text, "zh-CN", targetLocale.toLanguageTag());
    }
}

// 多货币服务
@Service
public class CurrencyService {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    public BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        ExchangeRate rate = exchangeRateRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency);
        if (rate == null) {
            throw new RuntimeException("未找到汇率信息");
        }

        return amount.multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP);
    }

    public String formatCurrency(BigDecimal amount, String currencyCode, Locale locale) {
        Currency currency = Currency.getInstance(currencyCode);
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setCurrency(currency);
        return format.format(amount);
    }
}
```

#### 4.3 前端国际化
```javascript
// i18n配置
import VueI18n from 'vue-i18n'

const messages = {
  'zh-CN': {
    common: {
      confirm: '确认',
      cancel: '取消',
      save: '保存',
      delete: '删除',
      edit: '编辑',
      add: '新增',
      search: '搜索'
    },
    product: {
      name: '商品名称',
      code: '商品编码',
      price: '价格',
      stock: '库存',
      category: '分类'
    },
    warehouse: {
      inbound: '入库',
      outbound: '出库',
      inventory: '库存',
      transfer: '调拨'
    }
  },
  'en-US': {
    common: {
      confirm: 'Confirm',
      cancel: 'Cancel',
      save: 'Save',
      delete: 'Delete',
      edit: 'Edit',
      add: 'Add',
      search: 'Search'
    },
    product: {
      name: 'Product Name',
      code: 'Product Code',
      price: 'Price',
      stock: 'Stock',
      category: 'Category'
    },
    warehouse: {
      inbound: 'Inbound',
      outbound: 'Outbound',
      inventory: 'Inventory',
      transfer: 'Transfer'
    }
  },
  'ja-JP': {
    common: {
      confirm: '確認',
      cancel: 'キャンセル',
      save: '保存',
      delete: '削除',
      edit: '編集',
      add: '追加',
      search: '検索'
    },
    product: {
      name: '商品名',
      code: '商品コード',
      price: '価格',
      stock: '在庫',
      category: 'カテゴリー'
    },
    warehouse: {
      inbound: '入庫',
      outbound: '出庫',
      inventory: '在庫',
      transfer: '移送'
    }
  }
}

const i18n = new VueI18n({
  locale: localStorage.getItem('language') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages
})

export default i18n
```

#### 4.4 多语言组件
```vue
<template>
  <div class="language-selector">
    <el-dropdown @command="changeLanguage" trigger="click">
      <span class="el-dropdown-link">
        {{ currentLanguage.label }}
        <i class="el-icon-arrow-down el-icon--right"></i>
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item
          v-for="lang in languages"
          :key="lang.code"
          :command="lang.code"
          :class="{ active: lang.code === currentLanguage.code }"
        >
          <span class="language-flag">{{ lang.flag }}</span>
          {{ lang.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
export default {
  name: 'LanguageSelector',
  data() {
    return {
      currentLanguage: this.getCurrentLanguage(),
      languages: [
        { code: 'zh-CN', label: '简体中文', flag: '🇨🇳' },
        { code: 'en-US', label: 'English', flag: '🇺🇸' },
        { code: 'ja-JP', label: '日本語', flag: '🇯🇵' },
        { code: 'ko-KR', label: '한국어', flag: '🇰🇷' },
        { code: 'es-ES', label: 'Español', flag: '🇪🇸' },
        { code: 'fr-FR', label: 'Français', flag: '🇫🇷' },
        { code: 'de-DE', label: 'Deutsch', flag: '🇩🇪' }
      ]
    }
  },
  methods: {
    getCurrentLanguage() {
      const currentCode = this.$i18n.locale
      return this.languages.find(lang => lang.code === currentCode) || this.languages[0]
    },

    changeLanguage(languageCode) {
      this.$i18n.locale = languageCode
      localStorage.setItem('language', languageCode)
      this.currentLanguage = this.getCurrentLanguage()

      // 更新后端语言设置
      this.updateUserLanguage(languageCode)

      // 更新页面标题和meta信息
      this.updatePageMetadata(languageCode)
    },

    async updateUserLanguage(languageCode) {
      try {
        await this.$http.post('/api/user/language', { language: languageCode })
      } catch (error) {
        console.error('更新用户语言设置失败', error)
      }
    },

    updatePageMetadata(languageCode) {
      const messages = this.$i18n.messages[languageCode]
      document.title = messages.app?.title || document.title
    }
  }
}
</script>
```

#### 4.5 开发工作量估算
- 后端国际化：8人天
- 前端国际化：10人天
- 多语言资源文件：6人天
- 时区和货币处理：5人天
- 翻译集成：4人天

**总计：33人天**

---

## 第三阶段总结

**总工作量估算：175人天**
- AI智能化功能：57人天
- 语音操作：40人天
- 图像识别：45人天
- 国际化支持：33人天

**预计工期：22-28周（按标准配置3-4人团队）**

---

## 项目总体规划

### 总体工作量汇总
- **第一阶段：68人天**（10-12周）
- **第二阶段：127人天**（16-20周）
- **第三阶段：175人天**（22-28周）
- **总计：370人天**（48-60周）

### 实施建议
1. **分阶段实施**：确保每个阶段的功能稳定后再进入下一阶段
2. **增量交付**：每个功能模块独立部署和测试
3. **用户反馈**：在每个阶段收集用户反馈，调整开发优先级
4. **技术储备**：提前准备AI、移动端、集成等技术的学习和研究

### 关键成功因素
1. **架构设计**：确保系统架构能够支持未来功能扩展
2. **数据质量**：高质量的历史数据是AI功能的基础
3. **团队技能**：团队需要具备AI、移动端、集成等技能
4. **用户培训**：确保用户能够充分利用新功能

### 风险控制
1. **技术风险**：选择成熟的技术栈，建立技术预研机制
2. **进度风险**：采用敏捷开发，定期评估和调整计划
3. **质量风险**：建立完善的测试体系，确保功能稳定

这个详细的功能扩展方案为您的仓库管理系统提供了从基础智能化到AI高级应用的完整升级路径，每个功能都包含了具体的技术实现方案和工作量估算，便于后续的开发实施。