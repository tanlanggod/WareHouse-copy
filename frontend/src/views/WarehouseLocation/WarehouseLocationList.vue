<template>
  <div class="warehouse-location-management">
    <div class="page-header">
      <h2>仓库位置管理</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission">
        <i class="el-icon-plus"></i>
        新增位置
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable @change="handleWarehouseChange">
            <el-option label="全部" value=""></el-option>
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="货架号/层号/货位号/描述"
            clearable
            @keyup.enter.native="handleSearch">
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="可用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计信息 -->
    <div class="statistics-bar" v-if="statistics">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.totalCount }}</div>
              <div class="stat-label">总位置数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.enabledCount }}</div>
              <div class="stat-label">可用位置</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.disabledCount }}</div>
              <div class="stat-label">禁用位置</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        style="width: 100%">
        <el-table-column
          prop="id"
          label="ID"
          width="80">
        </el-table-column>
        <el-table-column
          prop="warehouse.name"
          label="仓库名称"
          width="150">
        </el-table-column>
        <el-table-column
          prop="rackNumber"
          label="货架号"
          width="100">
        </el-table-column>
        <el-table-column
          prop="shelfLevel"
          label="层号"
          width="100">
        </el-table-column>
        <el-table-column
          prop="binNumber"
          label="货位号"
          width="100">
        </el-table-column>
        <el-table-column
          label="完整位置"
          width="200">
          <template slot-scope="scope">
            <span>{{ getFullLocation(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="描述"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '可用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createdAt"
          label="创建时间"
          width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="220"
          fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              @click="handleView(scope.row)">
              详情
            </el-button>
            <el-button
              size="mini"
              type="primary"
              @click="handleEdit(scope.row)"
              v-if="hasPermission">
              编辑
            </el-button>
            <el-button
              size="mini"
              type="warning"
              @click="handleToggleStatus(scope.row)"
              v-if="hasPermission">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.row)"
              v-if="hasPermission && userInfo.role === 'ADMIN'">
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
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose">
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="货架号" prop="rackNumber">
          <el-input v-model="form.rackNumber" placeholder="请输入货架号"></el-input>
        </el-form-item>
        <el-form-item label="层号" prop="shelfLevel">
          <el-input v-model="form.shelfLevel" placeholder="请输入层号"></el-input>
        </el-form-item>
        <el-form-item label="货位号" prop="binNumber">
          <el-input v-model="form.binNumber" placeholder="请输入货位号（可选）"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入位置描述"
            :rows="3">
          </el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      title="位置详情"
      :visible.sync="detailDialogVisible"
      width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentRow.warehouse?.name }}</el-descriptions-item>
        <el-descriptions-item label="货架号">{{ currentRow.rackNumber }}</el-descriptions-item>
        <el-descriptions-item label="层号">{{ currentRow.shelfLevel }}</el-descriptions-item>
        <el-descriptions-item label="货位号">{{ currentRow.binNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完整位置">{{ getFullLocation(currentRow) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'">
            {{ currentRow.status === 1 ? '可用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentRow.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentRow.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions-item v-if="currentRow.description" label="描述" :span="2">
        {{ currentRow.description }}
      </el-descriptions-item>
      </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { warehouseApi, warehouseLocationApi } from '@/api'

export default {
  name: 'WarehouseLocationList',
  data() {
    return {
      loading: false,
      submitting: false,
      tableData: [],
      warehouseList: [],
      statistics: null,
      searchForm: {
        warehouseId: null,
        keyword: '',
        status: null
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        warehouseId: null,
        rackNumber: '',
        shelfLevel: '',
        binNumber: '',
        description: '',
        status: 1
      },
      formRules: {
        warehouseId: [
          { required: true, message: '请选择仓库', trigger: 'change' }
        ],
        rackNumber: [
          { required: true, message: '请输入货架号', trigger: 'blur' },
          { max: 50, message: '货架号长度不能超过50个字符', trigger: 'blur' }
        ],
        shelfLevel: [
          { required: true, message: '请输入层号', trigger: 'blur' },
          { max: 20, message: '层号长度不能超过20个字符', trigger: 'blur' }
        ],
        binNumber: [
          { max: 50, message: '货位号长度不能超过50个字符', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      },
      detailDialogVisible: false,
      currentRow: {}
    }
  },
  computed: {
    ...mapState('user', ['userInfo']),

    // 检查是否有操作权限
    hasPermission() {
      const role = this.userInfo?.role
      return role === 'ADMIN' || role === 'WAREHOUSE_KEEPER'
    }
  },
  async created() {
    try {
      // 验证用户信息
      this.validateUserInfo()
      await this.initData()
    } catch (error) {
      console.error('页面初始化失败:', error)
      this.$message.error('页面加载失败，请刷新重试')
    }
  },
  methods: {
    // 验证用户信息
    validateUserInfo() {
      console.log('=== 用户信息验证 ===')

      if (!this.userInfo) {
        console.error('用户信息未加载')
        this.$message.error('用户信息未正确加载，请重新登录')
        return
      }

      console.log('用户信息验证通过:', this.userInfo)

      // 检查权限
      if (!this.hasPermission) {
        this.$message.warning(`您当前的角色是 ${this.userInfo.role}，没有操作权限`)
      }

      console.log('==================')
    },

    // 初始化数据
    async initData() {
      await this.fetchWarehouses()
      await this.fetchData()
    },

    // 获取仓库列表
    async fetchWarehouses() {
      try {
        const response = await warehouseApi.getAllWarehouses()
        if (response.code === 200) {
          this.warehouseList = response.data
        }
      } catch (error) {
        console.error('获取仓库列表失败:', error)
        this.$message.error('获取仓库列表失败')
      }
    },

    // 获取数据
    async fetchData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          size: this.pagination.size,
          warehouseId: this.searchForm.warehouseId,
          keyword: this.searchForm.keyword,
          status: this.searchForm.status
        }

        const response = await warehouseLocationApi.getWarehouseLocations(params)
        if (response.code === 200 && response.data) {
          this.tableData = Array.isArray(response.data.records) ? response.data.records : []
          this.pagination.total = response.data.total || 0

          // 如果有选择的仓库，获取统计信息
          if (this.searchForm.warehouseId) {
            this.fetchStatistics(this.searchForm.warehouseId)
          } else {
            this.statistics = null
          }
        } else {
          this.tableData = []
          this.pagination.total = 0
          this.$message.error(response.message || '获取数据失败')
        }
      } catch (error) {
        console.error('获取数据失败:', error)
        this.tableData = []
        this.pagination.total = 0
        this.$message.error('获取数据失败')
      } finally {
        this.loading = false
      }
    },

    // 获取统计信息
    async fetchStatistics(warehouseId) {
      try {
        const response = await warehouseLocationApi.getStatistics(warehouseId)
        if (response.code === 200) {
          this.statistics = response.data
        }
      } catch (error) {
        console.error('获取统计信息失败:', error)
      }
    },

    // 搜索
    handleSearch() {
      this.pagination.page = 1
      this.fetchData()
    },

    // 重置
    handleReset() {
      this.searchForm = {
        warehouseId: null,
        keyword: '',
        status: null
      }
      this.pagination.page = 1
      this.fetchData()
    },

    // 仓库变化
    handleWarehouseChange() {
      this.handleSearch()
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.page = 1
      this.fetchData()
    },

    // 当前页变化
    handleCurrentChange(page) {
      this.pagination.page = page
      this.fetchData()
    },

    // 新增
    handleCreate() {
      this.dialogTitle = '新增仓库位置'
      this.form = {
        id: null,
        warehouseId: this.searchForm.warehouseId,
        rackNumber: '',
        shelfLevel: '',
        binNumber: '',
        description: '',
        status: 1
      }
      this.dialogVisible = true
    },

    // 编辑
    handleEdit(row) {
      this.dialogTitle = '编辑仓库位置'
      this.form = {
        id: row.id,
        warehouseId: row.warehouse.id,
        rackNumber: row.rackNumber,
        shelfLevel: row.shelfLevel,
        binNumber: row.binNumber,
        description: row.description,
        status: row.status
      }
      this.dialogVisible = true
    },

    // 查看
    handleView(row) {
      this.currentRow = row
      this.detailDialogVisible = true
    },

    // 提交表单
    handleSubmit() {
      this.$refs.formRef.validate(async (valid) => {
        if (valid) {
          this.submitting = true
          try {
            let response
            if (this.form.id) {
              response = await warehouseLocationApi.updateWarehouseLocation(this.form.id, this.form)
            } else {
              response = await warehouseLocationApi.createWarehouseLocation(this.form)
            }

            if (response.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '创建成功')
              this.dialogVisible = false
              this.fetchData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          } catch (error) {
            console.error('操作失败:', error)
            this.$message.error('操作失败')
          } finally {
            this.submitting = false
          }
        }
      })
    },

    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除位置 "${this.getFullLocation(row)}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await warehouseLocationApi.deleteWarehouseLocation(row.id)
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.fetchData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {
        // 取消删除
      })
    },

    // 切换状态
    handleToggleStatus(row) {
      const action = row.status === 1 ? '禁用' : '启用'
      this.$confirm(`确定要${action}位置 "${this.getFullLocation(row)}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await warehouseLocationApi.toggleStatus(row.id)
          if (response.code === 200) {
            this.$message.success(`${action}成功`)
            this.fetchData()
          } else {
            this.$message.error(response.message || `${action}失败`)
          }
        } catch (error) {
          console.error(`${action}失败:`, error)
          this.$message.error(`${action}失败`)
        }
      }).catch(() => {
        // 取消操作
      })
    },

    // 关闭对话框
    handleDialogClose() {
      this.$refs.formRef.resetFields()
      this.dialogVisible = false
    },

    // 获取完整位置显示
    getFullLocation(row) {
      if (!row) return '-'
      const parts = []
      if (row.warehouse?.name) parts.push(row.warehouse.name)
      if (row.rackNumber) parts.push(row.rackNumber)
      if (row.shelfLevel) parts.push(row.shelfLevel)
      if (row.binNumber) parts.push(row.binNumber)
      return parts.join('-') || '-'
    },

    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.warehouse-location-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.search-bar {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.statistics-bar {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.table-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>