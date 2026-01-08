<template>
  <div class="warehouse-management">
    <div class="page-header">
      <h2>仓库管理</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission">
        <i class="el-icon-plus"></i>
        新增仓库
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="仓库名称/编号/地址"
            clearable
            @keyup.enter.native="handleSearch">
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
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
          prop="name"
          label="仓库名称"
          min-width="120">
        </el-table-column>
        <el-table-column
          prop="code"
          label="仓库编号"
          width="120">
        </el-table-column>
        <el-table-column
          prop="address"
          label="仓库地址"
          min-width="200"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="contactPerson"
          label="联系人"
          width="100">
        </el-table-column>
        <el-table-column
          prop="phone"
          label="联系电话"
          width="120">
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="80"
          align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ getStatusDescription(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createdAt"
          label="创建时间"
          width="180"
          :formatter="formatDate">
        </el-table-column>
        <el-table-column
          label="操作"
          width="280"
          fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              @click="handleView(scope.row)">
              查看
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
              v-if="hasPermission">
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

    <!-- 表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose">
      <el-form
        ref="warehouseForm"
        :model="warehouseForm"
        :rules="warehouseRules"
        label-width="100px">
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="warehouseForm.name" placeholder="请输入仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="仓库编号" prop="code">
          <el-input v-model="warehouseForm.code" placeholder="请输入仓库编号"></el-input>
        </el-form-item>
        <el-form-item label="仓库地址" prop="address">
          <el-input v-model="warehouseForm.address" placeholder="请输入仓库地址"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="warehouseForm.contactPerson" placeholder="请输入联系人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="warehouseForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="warehouseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      title="仓库详情"
      :visible.sync="viewDialogVisible"
      width="600px">
      <div v-if="viewData" class="detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="仓库ID">{{ viewData.id }}</el-descriptions-item>
          <el-descriptions-item label="仓库编号">{{ viewData.code || '-' }}</el-descriptions-item>
          <el-descriptions-item label="仓库名称">{{ viewData.name }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">
              {{ viewData.statusDescription }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="联系人">{{ viewData.contactPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ viewData.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="仓库地址" :span="2">{{ viewData.address }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(viewData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(viewData.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { warehouseApi } from '@/api'
import { mapState } from 'vuex'

export default {
  name: 'WarehouseList',
  data() {
    return {
      // 搜索表单
      searchForm: {
        keyword: '',
        status: ''
      },

      // 表格数据
      tableData: [],
      loading: false,

      // 分页数据
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },

      // 对话框数据
      dialogVisible: false,
      viewDialogVisible: false,
      dialogTitle: '新增仓库',
      warehouseForm: {
        id: null,
        name: '',
        code: '',
        address: '',
        contactPerson: '',
        phone: '',
        status: 1
      },

      // 查看数据
      viewData: null,

      // 表单验证规则
      warehouseRules: {
        name: [
          { required: true, message: '请输入仓库名称', trigger: 'blur', transform: value => value?.trim() },
          { min: 1, max: 100, message: '仓库名称长度应在1-100个字符之间', trigger: 'blur', transform: value => value?.trim() },
          { validator: this.validateNotEmpty, trigger: 'blur' }
        ],
        code: [
          { max: 50, message: '仓库编号长度不能超过50个字符', trigger: 'blur', transform: value => value?.trim() }
        ],
        address: [
          { required: true, message: '请输入仓库地址', trigger: 'blur', transform: value => value?.trim() },
          { min: 1, max: 255, message: '仓库地址长度应在1-255个字符之间', trigger: 'blur', transform: value => value?.trim() },
          { validator: this.validateNotEmpty, trigger: 'blur' }
        ],
        contactPerson: [
          { max: 50, message: '联系人姓名长度不能超过50个字符', trigger: 'blur', transform: value => value?.trim() }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur', transform: value => value?.trim() },
          { validator: this.validatePhone, trigger: 'blur' }
        ]
      },

      submitting: false
    }
  },

  computed: {
    ...mapState('user', ['userInfo']),

    // 检查是否有操作权限
    hasPermission() {
      if (!this.userInfo?.role) {
        console.warn('仓库管理 - 用户角色信息缺失:', this.userInfo)
        return false
      }
      const allowedRoles = ['ADMIN', 'WAREHOUSE_KEEPER']
      const hasPermission = allowedRoles.includes(this.userInfo.role)
      if (!hasPermission) {
        console.warn(`仓库管理 - 权限不足: 当前角色 ${this.userInfo.role}，允许角色 ${allowedRoles.join(', ')}`)
      }
      return hasPermission
    }
  },

  created() {
    // 验证用户信息
    this.validateUserInfo()
    this.fetchData()
  },

  methods: {
    // 获取状态描述
    getStatusDescription(status) {
      return status === 1 ? '启用' : '禁用'
    },

    // 验证非空字符串（去除空格后）
    validateNotEmpty(rule, value, callback) {
      const fieldNames = {
        name: '仓库名称',
        address: '仓库地址'
      }
      const fieldName = fieldNames[rule.field] || rule.field

      if (!value || (typeof value === 'string' && value.trim().length === 0)) {
        callback(new Error(`请输入有效的${fieldName}，不能为空或空格`))
      } else {
        callback()
      }
    },

    // 验证手机号码
    validatePhone(rule, value, callback) {
      if (!value) {
        callback() // 手机号是可选的，为空时通过验证
      } else if (!/^1[3-9]\d{9}$/.test(value.trim())) {
        callback(new Error('请输入正确的手机号码'))
      } else {
        callback()
      }
    },

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

    // 获取数据
    async fetchData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          size: this.pagination.size,
          keyword: this.searchForm.keyword,
          status: this.searchForm.status
        }

        const response = await warehouseApi.getWarehousesPage(params)
        if (response.code === 200) {
          this.tableData = response.data.records
          this.pagination.total = response.data.total
        } else {
          this.$message.error(response.message || '获取数据失败')
        }
      } catch (error) {
        console.error('获取仓库列表失败:', error)
        this.$message.error('获取数据失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索
    handleSearch() {
      this.pagination.page = 1
      this.fetchData()
    },

    // 重置搜索
    handleReset() {
      this.searchForm = {
        keyword: '',
        status: ''
      }
      this.pagination.page = 1
      this.fetchData()
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
      this.dialogTitle = '新增仓库'
      this.warehouseForm = {
        id: null,
        name: '',
        code: '',
        address: '',
        contactPerson: '',
        phone: '',
        status: 1
      }
      this.dialogVisible = true
    },

    // 编辑
    handleEdit(row) {
      this.dialogTitle = '编辑仓库'
      this.warehouseForm = {
        id: row.id,
        name: row.name,
        code: row.code || '',
        address: row.address,
        contactPerson: row.contactPerson || '',
        phone: row.phone || '',
        status: row.status
      }
      this.dialogVisible = true
    },

    // 查看
    async handleView(row) {
      this.viewData = row
      this.viewDialogVisible = true
    },

    // 切换状态
    async handleToggleStatus(row) {
      const action = row.status === 1 ? '禁用' : '启用'
      try {
        const response = await warehouseApi.toggleWarehouseStatus(row.id)
        if (response.code === 200) {
          this.$message.success(`${action}成功`)
          this.fetchData()
        } else {
          this.$message.error(response.message || `${action}失败`)
        }
      } catch (error) {
        console.error(`${action}仓库失败:`, error)
        this.$message.error(`${action}失败`)
      }
    },

    // 删除仓库
    async handleDelete(row) {
      this.$confirm(`确定要删除仓库"${row.name}"吗？此操作不可恢复。`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await warehouseApi.deleteWarehouse(row.id)
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.fetchData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除仓库失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {
        // 用户取消删除，无需处理
      })
    },

    // 创建清理后的提交数据
    createSubmitData() {
      // 基础数据对象，不包含id字段
      const submitData = {
        name: this.warehouseForm.name?.trim() || '',
        address: this.warehouseForm.address?.trim() || '',
        status: this.warehouseForm.status || 1
      }

      // 可选字段，只有在有值时才包含
      if (this.warehouseForm.code && this.warehouseForm.code.trim()) {
        submitData.code = this.warehouseForm.code.trim()
      }

      if (this.warehouseForm.contactPerson && this.warehouseForm.contactPerson.trim()) {
        submitData.contactPerson = this.warehouseForm.contactPerson.trim()
      }

      if (this.warehouseForm.phone && this.warehouseForm.phone.trim()) {
        submitData.phone = this.warehouseForm.phone.trim()
      }

      return submitData
    },

    // 提交表单
    handleSubmit() {
      this.$refs.warehouseForm.validate(async (valid) => {
        // 添加详细的调试日志
        console.log('=== 新增仓库调试信息 ===')
        console.log('表单验证结果:', valid)
        console.log('原始表单数据:', JSON.stringify(this.warehouseForm, null, 2))
        console.log('是否为编辑模式:', !!this.warehouseForm.id)
        console.log('========================')

        if (valid) {
          this.submitting = true
          try {
            // 创建清理后的提交数据
            const submitData = this.createSubmitData()
            console.log('清理后的提交数据:', JSON.stringify(submitData, null, 2))

            let response
            if (this.warehouseForm.id) {
              console.log('执行更新操作，ID:', this.warehouseForm.id)
              response = await warehouseApi.updateWarehouse(this.warehouseForm.id, submitData)
            } else {
              console.log('执行创建操作')
              response = await warehouseApi.createWarehouse(submitData)
            }

            console.log('API响应:', JSON.stringify(response, null, 2))

            if (response.code === 200) {
              this.$message.success(this.warehouseForm.id ? '更新成功' : '创建成功')
              this.dialogVisible = false
              this.fetchData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          } catch (error) {
            console.error('=== 提交失败详细错误信息 ===')
            console.error('错误对象:', error)
            console.error('错误响应:', error.response?.data)
            console.error('错误状态码:', error.response?.status)
            console.error('错误消息:', error.message)
            console.error('============================')

            // 显示更详细的错误信息
            const errorMessage = error.response?.data?.message || error.message || '操作失败'
            this.$message.error(errorMessage)
          } finally {
            this.submitting = false
          }
        }
      })
    },

    // 对话框关闭
    handleDialogClose() {
      this.$refs.warehouseForm.resetFields()
    },

    // 格式化日期
    formatDate(row, column, cellValue) {
      return this.formatDateTime(cellValue)
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
.warehouse-management {
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
  border-radius: 4px;
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.table-container {
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  padding: 20px;
  text-align: right;
}

.detail-container {
  padding: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>