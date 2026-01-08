<template>
  <div class="operation-log-container">
    <!-- 搜索条件区域 -->
    <el-card class="search-card">
      <div slot="header">
        <span class="card-title">
          <i class="el-icon-search"></i> 搜索条件
        </span>
        <el-button
          type="primary"
          size="small"
          icon="el-icon-refresh"
          @click="resetSearch"
          style="float: right"
        >
          重置
        </el-button>
      </div>

      <el-form :model="searchForm" inline size="small">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="用户名">
              <el-input
                v-model="searchForm.username"
                placeholder="请输入用户名"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="操作类型">
              <el-select
                v-model="searchForm.operationType"
                placeholder="请选择操作类型"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="type in operationTypes"
                  :key="type"
                  :label="getOperationTypeLabel(type)"
                  :value="type"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="资源类型">
              <el-select
                v-model="searchForm.resourceType"
                placeholder="请选择资源类型"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="type in resourceTypes"
                  :key="type"
                  :label="getResourceTypeLabel(type)"
                  :value="type"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="模块">
              <el-select
                v-model="searchForm.module"
                placeholder="请选择模块"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="module in modules"
                  :key="module"
                  :label="getModuleLabel(module)"
                  :value="module"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="操作状态">
              <el-select
                v-model="searchForm.isSuccess"
                placeholder="请选择操作状态"
                clearable
                style="width: 100%"
              >
                <el-option label="成功" :value="true" />
                <el-option label="失败" :value="false" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="IP地址">
              <el-input
                v-model="searchForm.ipAddress"
                placeholder="请输入IP地址"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="操作时间">
              <el-date-picker
                v-model="searchForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="yyyy-MM-dd HH:mm:ss"
                value-format="yyyy-MM-dd'T'HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-search"
                @click="handleSearch"
                style="width: 100%"
              >
                搜索
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 统计信息区域 -->
    <el-card class="statistics-card" v-if="statistics.totalCount">
      <div slot="header">
        <span class="card-title">
          <i class="el-icon-data-analysis"></i> 统计信息
        </span>
        <el-button
          type="success"
          size="small"
          icon="el-icon-refresh"
          @click="loadStatistics"
          style="float: right"
        >
          刷新
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col :span="6">
          <div class="statistic-item">
            <div class="statistic-value">{{ statistics.totalCount }}</div>
            <div class="statistic-label">总操作次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="statistic-item">
            <div class="statistic-value">{{ statistics.successCount?.true || 0 }}</div>
            <div class="statistic-label">成功次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="statistic-item">
            <div class="statistic-value">{{ statistics.successCount?.false || 0 }}</div>
            <div class="statistic-label">失败次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="statistic-item">
            <div class="statistic-value">
              {{ statistics.totalCount ? ((statistics.successCount?.true || 0) / statistics.totalCount * 100).toFixed(1) : 0 }}%
            </div>
            <div class="statistic-label">成功率</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card">
      <div slot="header">
        <span class="card-title">
          <i class="el-icon-document"></i> 操作日志列表
        </span>
        <div style="float: right">
          <el-button
            type="warning"
            size="small"
            icon="el-icon-warning"
            @click="showErrorLogs"
            v-if="statistics.successCount?.false > 0"
          >
            查看错误 ({{ statistics.successCount?.false }})
          </el-button>
          <el-button
            type="info"
            size="small"
            icon="el-icon-timer"
            @click="showSlowOperations"
          >
            慢操作
          </el-button>
          <el-button
            type="success"
            size="small"
            icon="el-icon-refresh"
            @click="loadData"
          >
            刷新
          </el-button>
        </div>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        size="small"
        @sort-change="handleSortChange"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="requestId" label="请求ID" width="120" show-overflow-tooltip />

        <el-table-column prop="username" label="用户" width="100" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag type="primary" size="mini" v-if="scope.row.username">
              {{ scope.row.username }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="operationType" label="操作类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)" size="mini">
              {{ getOperationTypeLabel(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="resourceType" label="资源类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getResourceTypeTag(scope.row.resourceType)" size="mini">
              {{ getResourceTypeLabel(scope.row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="module" label="模块" width="80">
          <template slot-scope="scope">
            <el-tag :type="getModuleTag(scope.row.module)" size="mini">
              {{ getModuleLabel(scope.row.module) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="operationDesc" label="操作描述" min-width="200" show-overflow-tooltip />

        <el-table-column prop="ipAddress" label="IP地址" width="120" show-overflow-tooltip />

        <el-table-column prop="responseTime" label="响应时间" width="100" sortable="custom">
          <template slot-scope="scope">
            <span :class="getResponseTimeClass(scope.row.responseTime)">
              {{ scope.row.responseTime }}ms
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="isSuccess" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isSuccess ? 'success' : 'danger'" size="mini">
              {{ scope.row.isSuccess ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="操作时间" width="150" sortable="custom">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="primary"
              size="mini"
              icon="el-icon-view"
              @click="handleView(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="操作日志详情" :visible.sync="detailDialogVisible" width="800px">
      <el-descriptions :column="2" border size="small" v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="请求ID">{{ currentLog.requestId }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentLog.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户角色">{{ currentLog.userRole || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(currentLog.operationType)" size="mini">
            {{ getOperationTypeLabel(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="资源类型">
          <el-tag :type="getResourceTypeTag(currentLog.resourceType)" size="mini">
            {{ getResourceTypeLabel(currentLog.resourceType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块">
          <el-tag :type="getModuleTag(currentLog.module)" size="mini">
            {{ getModuleLabel(currentLog.module) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="响应状态">{{ currentLog.responseStatus }}</el-descriptions-item>
        <el-descriptions-item label="响应时间">{{ currentLog.responseTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.isSuccess ? 'success' : 'danger'" size="mini">
            {{ currentLog.isSuccess ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatDateTime(currentLog.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2" v-if="currentLog.requestParams">
          <pre style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">{{ currentLog.requestParams }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <pre style="white-space: pre-wrap; color: #f56c6c; max-height: 200px; overflow-y: auto;">{{ currentLog.errorMessage }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="用户代理" :span="2" v-if="currentLog.userAgent">
          <div style="word-break: break-all;">{{ currentLog.userAgent }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getOperationLogs, getOperationLogById, getOperationStatistics, getOperationTypes, getResourceTypes, getModules } from '@/api/index'

export default {
  name: 'OperationLogList',
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: {
        username: '',
        operationType: '',
        resourceType: '',
        module: '',
        isSuccess: null,
        ipAddress: '',
        timeRange: []
      },
      pagination: {
        page: 0,
        size: 20,
        total: 0
      },
      sort: {
        prop: 'createdAt',
        order: 'desc'
      },
      statistics: {},
      currentLog: null,
      detailDialogVisible: false,
      operationTypes: [],
      resourceTypes: [],
      modules: []
    }
  },
  created() {
    this.loadData()
    this.loadStatistics()
    this.loadEnumData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          size: this.pagination.size,
          sort: this.sort.prop,
          direction: this.sort.order === 'descending' ? 'desc' : 'asc',
          ...this.searchForm
        }

        // 处理时间范围
        if (this.searchForm.timeRange && this.searchForm.timeRange.length === 2) {
          params.startTime = this.searchForm.timeRange[0]
          params.endTime = this.searchForm.timeRange[1]
        }

        // 清除空值参数
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key]
          }
        })

        const response = await getOperationLogs(params)
        if (response.code === 200) {
          this.tableData = response.data.content
          this.pagination.total = response.data.totalElements
        } else {
          this.$message.error(response.msg || '获取数据失败')
        }
      } catch (error) {
        console.error('获取操作日志失败:', error)
        this.$message.error('获取数据失败')
      } finally {
        this.loading = false
      }
    },

    async loadStatistics() {
      try {
        const params = {}
        if (this.searchForm.timeRange && this.searchForm.timeRange.length === 2) {
          params.startTime = this.searchForm.timeRange[0]
          params.endTime = this.searchForm.timeRange[1]
        }

        const response = await getOperationStatistics(params)
        if (response.code === 200) {
          this.statistics = response.data
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    },

    async loadEnumData() {
      try {
        const [typesRes, resourcesRes, modulesRes] = await Promise.all([
          getOperationTypes(),
          getResourceTypes(),
          getModules()
        ])

        if (typesRes.code === 200) this.operationTypes = typesRes.data
        if (resourcesRes.code === 200) this.resourceTypes = resourcesRes.data
        if (modulesRes.code === 200) this.modules = modulesRes.data
      } catch (error) {
        console.error('获取枚举数据失败:', error)
      }
    },

    handleSearch() {
      this.pagination.page = 0
      this.loadData()
      this.loadStatistics()
    },

    resetSearch() {
      this.searchForm = {
        username: '',
        operationType: '',
        resourceType: '',
        module: '',
        isSuccess: null,
        ipAddress: '',
        timeRange: []
      }
      this.pagination.page = 0
      this.loadData()
      this.loadStatistics()
    },

    handleSortChange({ prop, order }) {
      this.sort.prop = prop
      this.sort.order = order
      this.loadData()
    },

    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 0
      this.loadData()
    },

    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },

    async handleView(row) {
      try {
        const response = await getOperationLogById(row.id)
        if (response.code === 200) {
          this.currentLog = response.data
          this.detailDialogVisible = true
        } else {
          this.$message.error(response.msg || '获取详情失败')
        }
      } catch (error) {
        console.error('获取操作日志详情失败:', error)
        this.$message.error('获取详情失败')
      }
    },

    showErrorLogs() {
      this.searchForm.isSuccess = false
      this.handleSearch()
    },

    showSlowOperations() {
      this.$message.info('慢操作功能开发中...')
    },

    formatDateTime(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },

    getOperationTypeLabel(type) {
      const labels = {
        CREATE: '创建',
        READ: '查询',
        UPDATE: '更新',
        DELETE: '删除',
        EXPORT: '导出',
        IMPORT: '导入',
        LOGIN: '登录',
        LOGOUT: '登出',
        DOWNLOAD: '下载'
      }
      return labels[type] || type
    },

    getOperationTypeTag(type) {
      const tags = {
        CREATE: 'success',
        READ: 'info',
        UPDATE: 'warning',
        DELETE: 'danger',
        EXPORT: 'success',
        IMPORT: 'warning',
        LOGIN: 'info',
        LOGOUT: 'info',
        DOWNLOAD: 'success'
      }
      return tags[type] || 'info'
    },

    getResourceTypeLabel(type) {
      const labels = {
        PRODUCT: '商品',
        CATEGORY: '分类',
        SUPPLIER: '供应商',
        CUSTOMER: '客户',
        INBOUND: '入库',
        OUTBOUND: '出库',
        STOCK: '库存',
        USER: '用户',
        REPORT: '报表',
        SYSTEM: '系统'
      }
      return labels[type] || type
    },

    getResourceTypeTag(type) {
      const tags = {
        PRODUCT: 'primary',
        CATEGORY: 'primary',
        SUPPLIER: 'primary',
        CUSTOMER: 'primary',
        INBOUND: 'success',
        OUTBOUND: 'warning',
        STOCK: 'info',
        USER: 'warning',
        REPORT: 'success',
        SYSTEM: 'info'
      }
      return tags[type] || 'info'
    },

    getModuleLabel(module) {
      const labels = {
        PRODUCT: '商品',
        INVENTORY: '库存',
        REPORT: '报表',
        SYSTEM: '系统',
        AUTH: '认证'
      }
      return labels[module] || module
    },

    getModuleTag(module) {
      const tags = {
        PRODUCT: 'primary',
        INVENTORY: 'success',
        REPORT: 'warning',
        SYSTEM: 'info',
        AUTH: 'danger'
      }
      return tags[module] || 'info'
    },

    getResponseTimeClass(responseTime) {
      if (responseTime >= 5000) return 'response-time-slow'
      if (responseTime >= 2000) return 'response-time-medium'
      return 'response-time-fast'
    }
  }
}
</script>

<style scoped>
.operation-log-container {
  padding: 20px;
}

.search-card,
.statistics-card,
.table-card {
  margin-bottom: 20px;
}

.card-title {
  font-weight: bold;
  color: #303133;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.statistic-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.statistic-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.statistic-label {
  font-size: 14px;
  color: #909399;
}

.response-time-fast {
  color: #67c23a;
  font-weight: bold;
}

.response-time-medium {
  color: #e6a23c;
  font-weight: bold;
}

.response-time-slow {
  color: #f56c6c;
  font-weight: bold;
}

pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
}

.el-tag {
  margin-right: 5px;
}
</style>