<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="header-content">
        <div class="header-left">
          <span class="page-title">客户管理</span>
          <el-tag v-if="customers.length > 0" type="info" size="small">共 {{ customers.length }} 个客户</el-tag>
        </div>
        <div class="header-right">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
            新增客户
          </el-button>
          <el-button
            v-if="hasSelectedData"
            type="danger"
            icon="el-icon-delete"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
          <el-button icon="el-icon-download" @click="handleExport">
            导出数据
          </el-button>
        </div>
      </div>

      <!-- 搜索筛选区域 -->
      <div class="search-section">
        <el-form :model="searchForm" inline class="search-form-inline">
          <el-form-item label="客户名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入客户名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>

          <el-form-item label="联系人">
            <el-input
              v-model="searchForm.contactPerson"
              placeholder="请输入联系人"
              clearable
              style="width: 160px"
            />
          </el-form-item>

          <el-form-item label="联系电话">
            <el-input
              v-model="searchForm.phone"
              placeholder="请输入联系电话"
              clearable
              style="width: 160px"
            />
          </el-form-item>

          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 100px"
            >
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
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
        :data="filteredCustomers"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="name" label="客户名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="customer-name">
              <i class="el-icon-office-building" style="margin-right: 8px; color: #409eff;"></i>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="contactPerson" label="联系人" width="120">
          <template #default="{ row }">
            <div class="contact-info" v-if="row.contactPerson">
              <i class="el-icon-user" style="margin-right: 4px; color: #909399;"></i>
              {{ row.contactPerson }}
            </div>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="联系电话" width="140">
          <template #default="{ row }">
            <div class="contact-info" v-if="row.phone">
              <i class="el-icon-phone" style="margin-right: 4px; color: #67c23a;"></i>
              {{ row.phone }}
            </div>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="contact-info" v-if="row.email">
              <i class="el-icon-message" style="margin-right: 4px; color: #e6a23c;"></i>
              {{ row.email }}
            </div>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>

        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="address-info" v-if="row.address">
              <i class="el-icon-location" style="margin-right: 4px; color: #f56c6c;"></i>
              {{ row.address }}
            </div>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
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
              icon="el-icon-edit"
              @click="handleEdit(row)"
            >
              编辑
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
    </el-card>

    <!-- 客户表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="showDialog"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="customerForm"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="客户名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入客户名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="联系人" prop="contactPerson">
          <el-input
            v-model="formData.contactPerson"
            placeholder="请输入联系人姓名"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入联系电话"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱地址"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="3"
            placeholder="请输入客户地址"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="客户详情" :visible.sync="showDetailDialog" width="600px">
      <div v-if="currentCustomer" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户ID">{{ currentCustomer.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentCustomer.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentCustomer.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户名称" :span="2">{{ currentCustomer.name }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentCustomer.contactPerson || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentCustomer.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱地址">{{ currentCustomer.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentCustomer.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="客户地址" :span="2">{{ currentCustomer.address || '未设置' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { customerApi } from '@/api'
import { formatDateTime } from '@/utils/date'

export default {
  name: 'CustomerList',
  data() {
    return {
      loading: false,
      customers: [],
      filteredCustomers: [],
      selectedRows: [],

      searchForm: {
        name: '',
        contactPerson: '',
        phone: '',
        status: null
      },

      showDialog: false,
      showDetailDialog: false,
      dialogTitle: '新增客户',
      submitting: false,
      currentCustomer: null,

      formData: {
        id: null,
        name: '',
        contactPerson: '',
        phone: '',
        email: '',
        address: '',
        status: 1
      },

      formRules: {
        name: [
          { required: true, message: '请输入客户名称', trigger: 'blur' },
          { min: 2, max: 100, message: '客户名称长度应在2-100个字符之间', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },

  computed: {
    hasSelectedData() {
      return this.selectedRows.length > 0
    }
  },

  async created() {
    await this.loadCustomers()
  },

  methods: {
    formatDateTime,

    async loadCustomers() {
      this.loading = true
      try {
        const response = await customerApi.getAllCustomers()
        if (response.code === 200) {
          this.customers = response.data || []
          this.filteredCustomers = [...this.customers]
        } else {
          this.$message.error(response.message || '加载客户列表失败')
        }
      } catch (error) {
        console.error('加载客户列表失败:', error)
        this.$message.error('加载客户列表失败')
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      let filtered = [...this.customers]

      if (this.searchForm.name) {
        filtered = filtered.filter(customer =>
          customer.name.toLowerCase().includes(this.searchForm.name.toLowerCase())
        )
      }

      if (this.searchForm.contactPerson) {
        filtered = filtered.filter(customer =>
          customer.contactPerson && customer.contactPerson.toLowerCase().includes(this.searchForm.contactPerson.toLowerCase())
        )
      }

      if (this.searchForm.phone) {
        filtered = filtered.filter(customer =>
          customer.phone && customer.phone.includes(this.searchForm.phone)
        )
      }

      if (this.searchForm.status !== null) {
        filtered = filtered.filter(customer => customer.status === this.searchForm.status)
      }

      this.filteredCustomers = filtered
    },

    handleReset() {
      this.searchForm = {
        name: '',
        contactPerson: '',
        phone: '',
        status: null
      }
      this.filteredCustomers = [...this.customers]
    },

    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    handleAdd() {
      this.dialogTitle = '新增客户'
      this.formData = {
        id: null,
        name: '',
        contactPerson: '',
        phone: '',
        email: '',
        address: '',
        status: 1
      }
      this.showDialog = true
    },

    handleEdit(row) {
      this.dialogTitle = '编辑客户'
      this.formData = { ...row }
      this.showDialog = true
    },

    handleView(row) {
      this.currentCustomer = row
      this.showDetailDialog = true
    },

    async handleDelete(row) {
      this.$confirm(`确定要删除客户"${row.name}"吗？`, '确认删除', {
        type: 'warning'
      }).then(async () => {
        try {
          await customerApi.deleteCustomer(row.id)
          this.$message.success('删除成功')
          await this.loadCustomers()
        } catch (error) {
          console.error('删除客户失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {
        // 用户取消删除
      })
    },

    handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要删除的客户')
        return
      }

      const customerNames = this.selectedRows.map(row => row.name).join('、')
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个客户吗？\n${customerNames}`, '批量删除', {
        type: 'warning'
      }).then(async () => {
        try {
          // 批量删除
          const deletePromises = this.selectedRows.map(row => customerApi.deleteCustomer(row.id))
          await Promise.all(deletePromises)
          this.$message.success('批量删除成功')
          await this.loadCustomers()
        } catch (error) {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        }
      }).catch(() => {
        // 用户取消删除
      })
    },

    handleExport() {
      try {
        const data = this.filteredCustomers.map(customer => ({
          '客户名称': customer.name,
          '联系人': customer.contactPerson || '',
          '联系电话': customer.phone || '',
          '邮箱': customer.email || '',
          '地址': customer.address || '',
          '状态': customer.status === 1 ? '启用' : '禁用',
          '创建时间': formatDateTime(customer.createdAt)
        }))

        // 使用浏览器导出功能
        const csvContent = this.convertToCSV(data)
        const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `客户列表_${new Date().toLocaleDateString()}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    },

    convertToCSV(data) {
      if (data.length === 0) return ''

      const headers = Object.keys(data[0])
      const csvHeaders = headers.join(',')
      const csvRows = data.map(row =>
        headers.map(header => `"${row[header] || ''}"`).join(',')
      )

      return [csvHeaders, ...csvRows].join('\n')
    },

    async handleSubmit() {
      try {
        await this.$refs.customerForm.validate()
        this.submitting = true

        if (this.formData.id) {
          // 编辑
          await customerApi.updateCustomer(this.formData.id, this.formData)
          this.$message.success('客户信息更新成功')
        } else {
          // 新增
          await customerApi.createCustomer(this.formData)
          this.$message.success('客户添加成功')
        }

        this.showDialog = false
        await this.loadCustomers()
      } catch (error) {
        if (error.message) {
          this.$message.error(error.message)
        }
      } finally {
        this.submitting = false
      }
    },

    handleDialogClose() {
      this.$refs.customerForm && this.$refs.customerForm.resetFields()
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

.customer-name {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.contact-info {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.address-info {
  display: flex;
  align-items: flex-start;
  line-height: 1.4;
}

.text-muted {
  color: #999;
  font-style: italic;
}

.danger-btn {
  color: #f56c6c;
}

.danger-btn:hover {
  color: #f78989;
}

.detail-content {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-form-inline {
    gap: 12px;
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

  .search-actions {
    width: 100%;
    margin-left: 0 !important;
    text-align: right;
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

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style>