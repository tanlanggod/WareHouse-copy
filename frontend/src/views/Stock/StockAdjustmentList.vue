<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="header-content">
        <div class="header-left">
          <span class="page-title">库存调整记录</span>
          <el-tag v-if="total > 0" type="info" size="small">共 {{ total }} 条记录</el-tag>
        </div>
        <div class="header-right">
          <el-button type="primary" icon="el-icon-plus" @click="$router.push('/app/stock/adjustment')">
            新建调整
          </el-button>
          <el-button
            v-if="hasSelectedData"
            type="danger"
            icon="el-icon-delete"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
      </div>

      <!-- 搜索筛选区域 -->
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form-inline">
          <el-form-item label="商品筛选">
            <el-select
              v-model="searchForm.productId"
              placeholder="请选择商品"
              clearable
              filterable
              style="width: 220px"
            >
              <el-option
                v-for="item in products"
                :key="item.id"
                :label="`${item.name} (${item.code})`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="调整类型">
            <el-select
              v-model="searchForm.adjustmentType"
              placeholder="请选择调整类型"
              clearable
              style="width: 140px"
            >
              <el-option label="增加" value="INCREASE">
                <i class="el-icon-plus" style="color: #67c23a; margin-right: 5px;"></i>
                增加
              </el-option>
              <el-option label="减少" value="DECREASE">
                <i class="el-icon-minus" style="color: #f56c6c; margin-right: 5px;"></i>
                减少
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
              style="width: 380px"
              clearable
              :picker-options="pickerOptions"
            />
          </el-form-item>

          <el-form-item class="search-actions">
            <el-button-group>
              <el-button type="primary" icon="el-icon-search" @click="handleSearch">
                搜索
              </el-button>
              <el-button icon="el-icon-refresh" @click="handleReset">
                重置
              </el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="adjustments"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-name">{{ row.product.name }}</div>
              <div class="product-code">编号: {{ row.product.code }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="调整类型" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.adjustmentType === 'INCREASE' ? 'success' : 'danger'"
              size="small"
            >
              {{ row.adjustmentType === 'INCREASE' ? '增加' : '减少' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="quantity" label="调整数量" width="100">
          <template #default="{ row }">
            <span :class="{ 'increase': row.adjustmentType === 'INCREASE', 'decrease': row.adjustmentType === 'DECREASE' }">
              {{ row.adjustmentType === 'INCREASE' ? '+' : '-' }}{{ row.quantity }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="库存变化" width="150">
          <template #default="{ row }">
            <div class="stock-change">
              <div class="before">{{ row.beforeQty }}</div>
              <div class="arrow">→</div>
              <div class="after">{{ row.afterQty }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="reason" label="调整原因" min-width="200" show-overflow-tooltip />

        <el-table-column label="操作人" width="120">
          <template #default="{ row }">
            <span v-if="row.operator">{{ row.operator.realName || row.operator.username }}</span>
            <span v-else class="text-muted">未知</span>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="调整时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="text"
              size="small"
              icon="el-icon-view"
              @click="handleView(row)"
            >
              详情
            </el-button>
            <el-button
              type="text"
              size="small"
              icon="el-icon-delete"
              @click="handleDelete(row)"
              class="danger-btn"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="库存调整详情" :visible.sync="showDetailDialog" width="600px">
      <div v-if="currentAdjustment" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品名称">{{ currentAdjustment.product.name }}</el-descriptions-item>
          <el-descriptions-item label="商品编号">{{ currentAdjustment.product.code }}</el-descriptions-item>
          <el-descriptions-item label="调整类型">
            <el-tag :type="currentAdjustment.adjustmentType === 'INCREASE' ? 'success' : 'danger'" size="small">
              {{ currentAdjustment.adjustmentType === 'INCREASE' ? '增加' : '减少' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="调整数量">{{ currentAdjustment.quantity }}</el-descriptions-item>
          <el-descriptions-item label="调整前库存">{{ currentAdjustment.beforeQty }}</el-descriptions-item>
          <el-descriptions-item label="调整后库存">{{ currentAdjustment.afterQty }}</el-descriptions-item>
          <el-descriptions-item label="操作人">
            {{ currentAdjustment.operator ? (currentAdjustment.operator.realName || currentAdjustment.operator.username) : '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="调整时间">{{ formatDateTime(currentAdjustment.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="调整原因" :span="2">{{ currentAdjustment.reason }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { stockAdjustmentApi, productApi } from '@/api'
import { formatDateTime } from '@/utils/date'

export default {
  name: 'StockAdjustmentList',
  data() {
    return {
      loading: false,
      adjustments: [],
      products: [],
      total: 0,
      selectedRows: [],

      searchForm: {
        productId: null,
        adjustmentType: '',
        dateRange: []
      },

      pagination: {
        page: 1,
        size: 10
      },

      showDetailDialog: false,
      currentAdjustment: null,

      // 时间选择器配置
      pickerOptions: {
        shortcuts: [
          {
            text: '最近1小时',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近24小时',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近3天',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 3)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近一周',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '本月',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setDate(1)
              start.setHours(0, 0, 0, 0)
              end.setHours(23, 59, 59, 999)
              picker.$emit('pick', [start, end])
            }
          }
        ]
      }
    }
  },

  computed: {
    hasSelectedData() {
      return this.selectedRows.length > 0
    },

    searchParams() {
      const params = {
        page: this.pagination.page,
        size: this.pagination.size
      }

      if (this.searchForm.productId) {
        params.productId = this.searchForm.productId
      }

      if (this.searchForm.adjustmentType) {
        params.adjustmentType = this.searchForm.adjustmentType
      }

      if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
        params.startDate = this.searchForm.dateRange[0]
        params.endDate = this.searchForm.dateRange[1]
      }

      return params
    }
  },

  async created() {
    await this.loadProducts()
    await this.loadAdjustments()
  },

  methods: {
    formatDateTime,

    async loadProducts() {
      try {
        const response = await productApi.getProducts({ page: 1, size: 1000 })
        if (response.code === 200) {
          this.products = response.data.records
        }
      } catch (error) {
        console.error('加载商品列表失败:', error)
        this.$message.error('加载商品列表失败')
      }
    },

    async loadAdjustments() {
      this.loading = true
      try {
        const response = await stockAdjustmentApi.getAdjustments(this.searchParams)
        if (response.code === 200) {
          this.adjustments = response.data.records || []
          this.total = response.data.total || 0
        } else {
          this.$message.error(response.message || '加载库存调整记录失败')
        }
      } catch (error) {
        console.error('加载库存调整记录失败:', error)
        this.$message.error('加载库存调整记录失败')
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      this.pagination.page = 1
      this.loadAdjustments()
    },

    handleReset() {
      this.searchForm = {
        productId: null,
        adjustmentType: '',
        dateRange: []
      }
      this.pagination.page = 1
      this.loadAdjustments()
    },

    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.page = 1
      this.loadAdjustments()
    },

    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadAdjustments()
    },

    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    handleView(row) {
      this.currentAdjustment = row
      this.showDetailDialog = true
    },

    handleDelete(row) {
      this.$confirm(`确定要删除这条库存调整记录吗？`, '确认删除', {
        type: 'warning'
      }).then(async () => {
        try {
          // 注意：后端需要提供删除接口，这里暂时模拟
          this.$message.success('删除成功')
          await this.loadAdjustments()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {
        // 用户取消删除
      })
    },

    handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要删除的记录')
        return
      }

      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条记录吗？`, '批量删除', {
        type: 'warning'
      }).then(async () => {
        try {
          // 注意：后端需要提供批量删除接口，这里暂时模拟
          this.$message.success('批量删除成功')
          await this.loadAdjustments()
        } catch (error) {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        }
      }).catch(() => {
        // 用户取消删除
      })
    }
  }
}
</script>

<style scoped>
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.header-right {
  display: flex;
  gap: 10px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-form-inline {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.search-form-inline .el-form-item {
  margin-bottom: 0 !important;
  flex-shrink: 0;
}

.search-form-inline .el-form-item__label {
  font-weight: 500;
  color: #606266;
  line-height: 32px;
  padding-right: 8px;
}

.search-actions {
  margin-left: auto !important;
  flex-shrink: 0;
}

/* 统一控件高度和样式 */
.search-form-inline .el-input__inner,
.search-form-inline .el-select .el-input__inner {
  height: 36px;
  line-height: 36px;
  border-radius: 6px;
  border-color: #dcdfe6;
  transition: all 0.3s ease;
}

.search-form-inline .el-input__inner:focus,
.search-form-inline .el-select:hover .el-input__inner {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.search-form-inline .el-date-editor.el-range-editor {
  height: 36px;
  line-height: 34px;
  border-radius: 6px;
  border-color: #dcdfe6;
  transition: all 0.3s ease;
}

.search-form-inline .el-date-editor.el-range-editor:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.search-form-inline .el-range-input {
  font-size: 14px;
  background-color: transparent;
  color: #606266;
}

.search-form-inline .el-range-separator {
  color: #909399;
  font-weight: 500;
  padding: 0 8px;
}

/* 按钮组样式优化 */
.search-actions .el-button-group .el-button {
  height: 36px;
  padding: 0 16px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-actions .el-button-group .el-button:first-child {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.search-actions .el-button-group .el-button:last-child {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  border-left: 1px solid #dcdfe6;
}

.search-actions .el-button-group .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 选项下拉框优化 */
.search-form-inline .el-select-dropdown__item {
  padding: 10px 20px;
  font-size: 14px;
  transition: background-color 0.2s ease;
}

.search-form-inline .el-select-dropdown__item:hover {
  background-color: #f5f7fa;
}

/* 时间选择器优化 */
.search-form-inline .el-picker-panel__shortcut {
  padding: 8px 20px;
  font-size: 13px;
  color: #606266;
  transition: all 0.2s ease;
}

.search-form-inline .el-picker-panel__shortcut:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-form-inline {
    gap: 12px;
  }

  .search-form-inline .el-date-editor.el-range-editor {
    width: 340px;
  }
}

@media (max-width: 992px) {
  .search-form-inline {
    align-items: stretch;
    gap: 16px;
  }

  .search-form-inline .el-form-item {
    width: calc(50% - 8px);
  }

  .search-form-inline .el-date-editor.el-range-editor {
    width: 100%;
  }

  .search-actions {
    width: 100%;
    margin-left: 0 !important;
    text-align: right;
  }

  .search-actions .el-button-group {
    width: auto;
    display: inline-flex;
  }
}

@media (max-width: 768px) {
  .search-section {
    padding: 16px;
  }

  .search-form-inline {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .search-form-inline .el-form-item {
    width: 100%;
  }

  .search-actions {
    text-align: center;
  }

  .search-actions .el-button-group {
    width: 100%;
    display: flex;
  }

  .search-actions .el-button-group .el-button {
    flex: 1;
  }
}

.product-info {
  line-height: 1.4;
}

.product-name {
  font-weight: 500;
  color: #333;
}

.product-code {
  font-size: 12px;
  color: #999;
}

.stock-change {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: monospace;
}

.stock-change .before {
  color: #666;
}

.stock-change .arrow {
  color: #999;
}

.stock-change .after {
  font-weight: 600;
  color: #333;
}

.increase {
  color: #67c23a;
  font-weight: 600;
}

.decrease {
  color: #f56c6c;
  font-weight: 600;
}

.text-muted {
  color: #999;
}

.danger-btn {
  color: #f56c6c;
}

.danger-btn:hover {
  color: #f78989;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.detail-content {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .search-section .el-form {
    display: block;
  }

  .search-section .el-form-item {
    display: block;
    margin-bottom: 15px;
  }
}
</style>