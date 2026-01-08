<template>
  <div class="user-list">
    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/真实姓名"
            clearable
            @keyup.enter.native="handleSearch">
          </el-input>
        </el-form-item>

        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="库管员" value="WAREHOUSE_KEEPER"></el-option>
            <el-option label="普通员工" value="EMPLOYEE"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
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

      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
      </div>
    </div>

    <!-- 用户列表表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="userList"
        border
        style="width: 100%">

        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="username" label="用户名" width="120">
          <template slot-scope="scope">
            <el-link type="primary" @click="handleView(scope.row)">
              {{ scope.row.username }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="realName" label="真实姓名" width="120" />

        <el-table-column prop="role" label="角色" width="100">
          <template slot-scope="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />

        <el-table-column prop="phone" label="手机号" width="120" />

        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="handleEdit(scope.row)">
              编辑
            </el-button>

            <el-button
              size="mini"
              type="warning"
              @click="handleResetPassword(scope.row)">
              重置密码
            </el-button>

            <el-button
              size="mini"
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="handleToggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>

            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination">
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

    <!-- 用户表单弹窗 -->
    <user-form
      :visible.sync="formVisible"
      :user-data="currentUser"
      :is-edit="isEdit"
      @success="handleFormSuccess">
    </user-form>
  </div>
</template>

<script>
import { userAdminApi } from '@/api'
import UserForm from './UserForm.vue'

export default {
  name: 'UserList',
  components: {
    UserForm
  },
  data() {
    return {
      loading: false,
      userList: [],
      searchForm: {
        keyword: '',
        role: '',
        status: ''
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      formVisible: false,
      isEdit: false,
      currentUser: null
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    // 获取用户列表
    async fetchUsers() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          size: this.pagination.size,
          keyword: this.searchForm.keyword || undefined,
          role: this.searchForm.role || undefined,
          status: this.searchForm.status || undefined,
          sortBy: 'createdAt',
          sortDirection: 'desc'
        }

        const response = await userAdminApi.getUsers(params)
        if (response.code === 200) {
          this.userList = response.data.records || []
          this.pagination.total = response.data.total || 0
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        this.$message.error('获取用户列表失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索
    handleSearch() {
      this.pagination.page = 1
      this.fetchUsers()
    },

    // 重置搜索
    handleReset() {
      this.searchForm = {
        keyword: '',
        role: '',
        status: ''
      }
      this.pagination.page = 1
      this.fetchUsers()
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.page = 1
      this.fetchUsers()
    },

    // 当前页变化
    handleCurrentChange(page) {
      this.pagination.page = page
      this.fetchUsers()
    },

    // 新增用户
    handleAdd() {
      this.currentUser = null
      this.isEdit = false
      this.formVisible = true
    },

    // 编辑用户
    handleEdit(user) {
      this.currentUser = { ...user }
      this.isEdit = true
      this.formVisible = true
    },

    // 查看用户详情
    handleView(user) {
      this.$message.info(`查看用户 ${user.username} 的详细信息`)
    },

    // 重置密码
    async handleResetPassword(user) {
      try {
        await this.$confirm(
          `确定要重置用户 ${user.username} 的密码吗？`,
          '重置密码',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await userAdminApi.resetPassword(user.id)
        if (response.code === 200) {
          this.$alert(
            `密码重置成功！新密码：${response.data}`,
            '密码重置成功',
            {
              confirmButtonText: '我知道了',
              type: 'success'
            }
          )
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重置密码失败:', error)
          this.$message.error('重置密码失败')
        }
      }
    },

    // 切换用户状态
    async handleToggleStatus(user) {
      const action = user.status === 1 ? '禁用' : '启用'
      try {
        await this.$confirm(
          `确定要${action}用户 ${user.username} 吗？`,
          `${action}用户`,
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await userAdminApi.toggleUserStatus(user.id)
        if (response.code === 200) {
          this.$message.success(`${action}成功`)
          this.fetchUsers()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}用户失败:`, error)
          this.$message.error(`${action}用户失败`)
        }
      }
    },

    // 删除用户
    async handleDelete(user) {
      try {
        await this.$confirm(
          `确定要删除用户 ${user.username} 吗？删除后将禁用该用户账号。`,
          '删除用户',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await userAdminApi.deleteUser(user.id)
        if (response.code === 200) {
          this.$message.success('删除成功')
          this.fetchUsers()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除用户失败:', error)
          this.$message.error('删除用户失败')
        }
      }
    },

    // 表单提交成功
    handleFormSuccess() {
      this.formVisible = false
      this.fetchUsers()
    },

    // 获取角色文本
    getRoleText(role) {
      const roleMap = {
        'ADMIN': '管理员',
        'WAREHOUSE_KEEPER': '库管员',
        'EMPLOYEE': '普通员工'
      }
      return roleMap[role] || '未知'
    },

    // 获取角色标签类型
    getRoleTagType(role) {
      const typeMap = {
        'ADMIN': 'danger',
        'WAREHOUSE_KEEPER': 'warning',
        'EMPLOYEE': 'info'
      }
      return typeMap[role] || 'info'
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
.user-list {
  padding: 20px;
}

.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.action-buttons {
  margin-top: 15px;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-list {
    padding: 10px;
  }

  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>