<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">仪表板</h1>
      <p class="page-subtitle">欢迎回来，{{ userInfo.realName || userInfo.username }}！</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon primary">
          <i class="el-icon-goods"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品总数</div>
          <div class="stat-change positive">
            <i class="el-icon-top"></i>
            +12% 较上月
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon success">
          <i class="el-icon-box"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayInbounds }}</div>
          <div class="stat-label">今日入库</div>
          <div class="stat-change positive">
            <i class="el-icon-top"></i>
            +8% 较昨日
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon warning">
          <i class="el-icon-sold-out"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayOutbounds }}</div>
          <div class="stat-label">今日出库</div>
          <div class="stat-change negative">
            <i class="el-icon-bottom"></i>
            -3% 较昨日
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon danger">
          <i class="el-icon-warning"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.lowStockCount }}</div>
          <div class="stat-label">库存预警</div>
          <div class="stat-change neutral">
            <i class="el-icon-minus"></i>
            需要关注
          </div>
        </div>
      </div>
    </div>

    <!-- 图表和表格区域 -->
    <div class="dashboard-grid">
      <!-- 低库存预警 -->
      <div class="dashboard-card">
        <div class="card-header">
          <h3 class="card-title">
            <i class="el-icon-warning"></i>
            低库存预警
          </h3>
          <router-link to="/app/stock" class="view-all">查看全部</router-link>
        </div>
        <div class="card-body">
          <div class="warning-list" v-if="lowStockProducts.length > 0">
            <div v-for="product in lowStockProducts.slice(0, 5)" :key="product.id" class="warning-item">
              <div class="warning-info">
                <div class="warning-product">{{ product.name }}</div>
                <div class="warning-quantity">
                  当前库存: <span class="warning-value">{{ product.stockQty }}</span>
                  / 最小库存: {{ product.minStock }}
                </div>
              </div>
              <div class="warning-actions">
                <button class="btn btn-sm btn-primary" @click="navigateTo('products')">补货</button>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="el-icon-success"></i>
            <p>暂无库存预警</p>
          </div>
        </div>
      </div>

      <!-- 快速操作 -->
      <div class="dashboard-card">
        <div class="card-header">
          <h3 class="card-title">
            <i class="el-icon-s-operation"></i>
            快速操作
          </h3>
        </div>
        <div class="card-body">
          <div class="quick-actions-grid">
            <router-link to="/app/products/add" class="quick-action">
              <div class="action-icon">
                <i class="el-icon-plus"></i>
              </div>
              <span>新增商品</span>
            </router-link>
            <router-link to="/app/inbounds/add" class="quick-action">
              <div class="action-icon">
                <i class="el-icon-box"></i>
              </div>
              <span>入库登记</span>
            </router-link>
            <router-link to="/app/outbounds/add" class="quick-action">
              <div class="action-icon">
                <i class="el-icon-sold-out"></i>
              </div>
              <span>出库登记</span>
            </router-link>
            <router-link to="/app/stock" class="quick-action">
              <div class="action-icon">
                <i class="el-icon-search"></i>
              </div>
              <span>库存查询</span>
            </router-link>
            <router-link to="/app/reports" class="quick-action">
              <div class="action-icon">
                <i class="el-icon-data-analysis"></i>
              </div>
              <span>报表统计</span>
            </router-link>
            <router-link to="/app/categories" class="quick-action">
              <div class="action-icon">
                <i class="el-icon-menu"></i>
              </div>
              <span>分类管理</span>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据概览 -->
    <div class="dashboard-card">
      <div class="card-header">
        <h3 class="card-title">
          <i class="el-icon-data-line"></i>
          数据概览
        </h3>
      </div>
      <div class="card-body">
        <div class="overview-grid">
          <div class="overview-item">
            <div class="overview-label">商品分类</div>
            <div class="overview-value">{{ categoryCount }}</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">仓库数量</div>
            <div class="overview-value">{{ warehouseCount }}</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">供应商数量</div>
            <div class="overview-value">{{ supplierCount }}</div>
          </div>
          <div class="overview-item">
            <div class="overview-label">今日操作</div>
            <div class="overview-value">{{ todayOperations }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { productApi } from '@/api'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {
        totalProducts: 0,
        lowStockCount: 0,
        todayInbounds: 0,
        todayOutbounds: 0
      },
      lowStockProducts: [],
      categoryCount: 0,
      warehouseCount: 0,
      supplierCount: 0,
      todayOperations: 0
    }
  },
  computed: {
    ...mapState('user', ['userInfo'])
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        // 加载低库存商品
        const lowStockResponse = await productApi.getLowStockProducts()
        this.lowStockProducts = lowStockResponse.data || []
        this.stats.lowStockCount = this.lowStockProducts.length

        // 加载商品总数
        const productsResponse = await productApi.getProducts({ page: 1, size: 1 })
        this.stats.totalProducts = productsResponse.data?.total || 0

        // 模拟其他统计数据
        this.stats.todayInbounds = Math.floor(Math.random() * 50) + 10
        this.stats.todayOutbounds = Math.floor(Math.random() * 40) + 8
        this.categoryCount = Math.floor(Math.random() * 20) + 5
        this.warehouseCount = Math.floor(Math.random() * 5) + 1
        this.supplierCount = Math.floor(Math.random() * 30) + 10
        this.todayOperations = this.stats.todayInbounds + this.stats.todayOutbounds + Math.floor(Math.random() * 20)

      } catch (error) {
        console.error('加载仪表板数据失败:', error)
        this.$message.error('加载数据失败，请刷新页面重试')
      }
    },

    navigateTo(path) {
      this.$router.push(`/app/${path}`)
    }
  }
}
</script>

<style scoped>
@import '@/styles/design-system.css';

.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.page-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: var(--text-lg);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-2xl);
}

.stat-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  transition: all var(--transition-fast);
  border: 1px solid var(--border-color);
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: var(--text-inverse);
  flex-shrink: 0;
}

.stat-icon.primary {
  background: var(--gradient-primary);
}

.stat-icon.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-icon.danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: var(--leading-tight);
}

.stat-label {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  margin-bottom: var(--spacing-xs);
}

.stat-change {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--text-xs);
  font-weight: 500;
}

.stat-change.positive { color: var(--success-color); }
.stat-change.negative { color: var(--danger-color); }
.stat-change.neutral { color: var(--text-muted); }

/* 仪表板网格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-2xl);
}

.dashboard-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  overflow: hidden;
  transition: all var(--transition-fast);
}

.dashboard-card:hover {
  box-shadow: var(--shadow-md);
}

.card-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.card-title i {
  color: var(--primary-color);
}

.view-all {
  color: var(--primary-color);
  text-decoration: none;
  font-size: var(--text-sm);
  font-weight: 500;
}

.view-all:hover {
  text-decoration: underline;
}

.card-body {
  padding: var(--spacing-lg);
}

/* 预警列表 */
.warning-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.warning-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  border-radius: var(--radius-lg);
  background: rgba(239, 68, 68, 0.05);
  border: 1px solid rgba(239, 68, 68, 0.2);
  transition: all var(--transition-fast);
}

.warning-item:hover {
  background: rgba(239, 68, 68, 0.1);
}

.warning-product {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.warning-quantity {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.warning-value {
  color: var(--danger-color);
  font-weight: 600;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-3xl);
  color: var(--text-muted);
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: var(--spacing-md);
  color: var(--success-color);
}

.empty-state p {
  font-size: var(--text-sm);
  margin: 0;
}

/* 快速操作网格 */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--spacing-md);
}

.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg) var(--spacing-md);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  text-decoration: none;
  color: var(--text-primary);
  transition: all var(--transition-fast);
  border: 1px solid var(--border-color);
}

.quick-action:hover {
  background: var(--primary-color);
  color: var(--text-inverse);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.action-icon {
  width: 50px;
  height: 50px;
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: var(--primary-color);
  transition: all var(--transition-fast);
}

.quick-action:hover .action-icon {
  background: rgba(255, 255, 255, 0.2);
  color: var(--text-inverse);
}

.quick-action span {
  font-size: var(--text-xs);
  font-weight: 500;
  text-align: center;
}

/* 概览网格 */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.overview-item {
  text-align: center;
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
}

.overview-item:hover {
  background: var(--bg-tertiary);
}

.overview-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
}

.overview-value {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--primary-color);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-actions-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }

  .warning-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: var(--spacing-lg);
  }

  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 1.2rem;
  }
}
</style>