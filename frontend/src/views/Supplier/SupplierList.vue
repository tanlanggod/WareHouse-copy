<template>
  <div class="supplier-management">
    <div class="page-header">
      <h2>供应商管理</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission">
        <i class="el-icon-plus"></i>
        新增供应商
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="供应商名称/联系人/地址"
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
          label="供应商名称"
          min-width="150">
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
          prop="email"
          label="邮箱"
          width="150">
        </el-table-column>
        <el-table-column
          prop="address"
          label="地址"
          min-width="200"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="80"
          align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
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
          width="220"
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

    <!-- 表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose">
      <el-form
        ref="supplierForm"
        :model="supplierForm"
        :rules="supplierRules"
        label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="supplierForm.name" placeholder="请输入供应商名称"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="supplierForm.contactPerson" placeholder="请输入联系人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="supplierForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="supplierForm.email" placeholder="请输入邮箱地址"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="supplierForm.address" placeholder="请输入详细地址"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="supplierForm.status">
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
      title="供应商详情"
      :visible.sync="viewDialogVisible"
      width="600px">
      <div v-if="viewData" class="detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="供应商ID">{{ viewData.id }}</el-descriptions-item>
          <el-descriptions-item label="供应商名称">{{ viewData.name }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ viewData.contactPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ viewData.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ viewData.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">
              {{ viewData.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ viewData.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(viewData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(viewData.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { supplierApi } from '@/api'
import { mapState } from 'vuex'

export default {
  name: 'SupplierList',
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
      dialogTitle: '新增供应商',
      supplierForm: {
        id: null,
        name: '',
        contactPerson: '',
        phone: '',
        email: '',
        address: '',
        status: 1
      },

      // 查看数据
      viewData: null,

      // 表单验证规则
      supplierRules: {
        name: [
          { required: true, message: '请输入供应商名称', trigger: 'blur' },
          { max: 100, message: '供应商名称长度不能超过100个字符', trigger: 'blur' }
        ],
        contactPerson: [
          { max: 50, message: '联系人姓名长度不能超过50个字符', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
          { max: 100, message: '邮箱长度不能超过100个字符', trigger: 'blur' }
        ],
        address: [
          { max: 255, message: '地址长度不能超过255个字符', trigger: 'blur' }
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
        console.warn('供应商管理 - 用户角色信息缺失:', this.userInfo)
        return false
      }
      const allowedRoles = ['ADMIN', 'WAREHOUSE_KEEPER']
      const hasPermission = allowedRoles.includes(this.userInfo.role)
      if (!hasPermission) {
        console.warn(`供应商管理 - 权限不足: 当前角色 ${this.userInfo.role}，允许角色 ${allowedRoles.join(', ')}`)
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

        const response = await supplierApi.getSuppliersPage(params)
        if (response.code === 200) {
          this.tableData = response.data.records
          this.pagination.total = response.data.total
        } else {
          this.$message.error(response.message || '获取数据失败')
        }
      } catch (error) {
        console.error('获取供应商列表失败:', error)
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
      this.dialogTitle = '新增供应商'
      this.supplierForm = {
        id: null,
        name: '',
        contactPerson: '',
        phone: '',
        email: '',
        address: '',
        status: 1
      }
      this.dialogVisible = true
    },

    // 编辑
    handleEdit(row) {
      this.dialogTitle = '编辑供应商'
      this.supplierForm = {
        id: row.id,
        name: row.name,
        contactPerson: row.contactPerson || '',
        phone: row.phone || '',
        email: row.email || '',
        address: row.address || '',
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
        const response = await supplierApi.toggleSupplierStatus(row.id)
        if (response.code === 200) {
          this.$message.success(`${action}成功`)
          this.fetchData()
        } else {
          this.$message.error(response.message || `${action}失败`)
        }
      } catch (error) {
        console.error(`${action}供应商失败:`, error)
        this.$message.error(`${action}失败`)
      }
    },

    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除供应商"${row.name}"吗？此操作不可恢复。`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await supplierApi.deleteSupplier(row.id)
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.fetchData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除供应商失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {
        // 用户取消删除
      })
    },

    // 提交表单
    handleSubmit() {
      this.$refs.supplierForm.validate(async (valid) => {
        if (valid) {
          this.submitting = true
          try {
            let response
            if (this.supplierForm.id) {
              response = await supplierApi.updateSupplier(this.supplierForm.id, this.supplierForm)
            } else {
              response = await supplierApi.createSupplier(this.supplierForm)
            }

            if (response.code === 200) {
              this.$message.success(this.supplierForm.id ? '更新成功' : '创建成功')
              this.dialogVisible = false
              this.fetchData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          } catch (error) {
            console.error('提交失败:', error)
            this.$message.error('操作失败')
          } finally {
            this.submitting = false
          }
        }
      })
    },

    // 对话框关闭
    handleDialogClose() {
      this.$refs.supplierForm.resetFields()
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
.supplier-management {
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