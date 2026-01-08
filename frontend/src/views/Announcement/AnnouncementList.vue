<template>
  <div class="announcement-list">
    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input
            v-model="searchForm.title"
            placeholder="公告标题"
            clearable
            @keyup.enter.native="handleSearch">
          </el-input>
        </el-form-item>

        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option
              v-for="type in announcementTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option
              v-for="status in announcementStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="全部" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option
              v-for="priority in announcementPriorities"
              :key="priority.value"
              :label="priority.label"
              :value="priority.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="目标受众">
          <el-select v-model="searchForm.targetAudience" placeholder="全部" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option
              v-for="audience in targetAudiences"
              :key="audience.value"
              :label="audience.label"
              :value="audience.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">发布公告</el-button>
        <el-button type="warning" icon="el-icon-document" @click="handleCreateDraft">创建草稿</el-button>
        <el-button type="success" icon="el-icon-refresh" @click="handleRefresh">刷新</el-button>

        <el-dropdown v-if="multipleSelection.length > 0" @command="handleBatchOperation">
          <el-button type="danger" icon="el-icon-s-operation">
            批量操作 <i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="publish">批量发布</el-dropdown-item>
            <el-dropdown-item command="cancel">批量取消</el-dropdown-item>
            <el-dropdown-item command="expired">标记过期</el-dropdown-item>
            <el-dropdown-item command="delete">批量删除</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!-- 公告列表表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="announcementList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange">

        <el-table-column
          type="selection"
          width="55">
        </el-table-column>

        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="title-cell">
              <el-tag v-if="scope.row.isPinned" type="danger" size="mini" class="pinned-tag">
                <i class="el-icon-top"></i> 置顶
              </el-tag>
              <el-link type="primary" @click="handleView(scope.row)">
                {{ scope.row.title }}
              </el-link>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="priority" label="优先级" width="80">
          <template slot-scope="scope">
            <el-tag :type="getPriorityTagType(scope.row.priority)" size="mini">
              {{ getPriorityText(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="targetAudience" label="目标受众" width="100">
          <template slot-scope="scope">
            <span>{{ getTargetAudienceText(scope.row.targetAudience) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="readCount" label="阅读次数" width="80">
          <template slot-scope="scope">
            <span v-if="scope.row.readCount">{{ scope.row.readCount }}</span>
            <span v-else class="text-muted">0</span>
          </template>
        </el-table-column>

        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.publishTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="effectiveEndTime" label="有效期至" width="160">
          <template slot-scope="scope">
            <span v-if="scope.row.effectiveEndTime" :class="{ 'text-danger': isExpired(scope.row.effectiveEndTime) }">
              {{ formatDateTime(scope.row.effectiveEndTime) }}
            </span>
            <span v-else class="text-muted">永久</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="handleView(scope.row)">
              查看
            </el-button>

            <el-button
              v-if="scope.row.status === 'DRAFT'"
              size="mini"
              type="success"
              @click="handlePublish(scope.row)">
              发布
            </el-button>

            <el-button
              size="mini"
              type="warning"
              @click="handleEdit(scope.row)">
              编辑
            </el-button>

            <el-button
              v-if="scope.row.status === 'PUBLISHED'"
              size="mini"
              type="info"
              @click="handleCancel(scope.row)">
              取消
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

    <!-- 公告详情弹窗 -->
    <el-dialog
      title="公告详情"
      :visible.sync="detailVisible"
      width="80%"
      :close-on-click-modal="false">
      <div v-if="currentAnnouncement" class="announcement-detail">
        <h2>{{ currentAnnouncement.title }}</h2>
        <div class="detail-meta">
          <el-tag :type="getTypeTagType(currentAnnouncement.type)">{{ getTypeText(currentAnnouncement.type) }}</el-tag>
          <el-tag :type="getPriorityTagType(currentAnnouncement.priority)" size="mini">{{ getPriorityText(currentAnnouncement.priority) }}</el-tag>
          <el-tag :type="getStatusTagType(currentAnnouncement.status)">{{ getStatusText(currentAnnouncement.status) }}</el-tag>
          <span v-if="currentAnnouncement.isPinned" class="pinned-info"><i class="el-icon-top"></i> 置顶公告</span>
        </div>
        <div class="detail-content" v-html="currentAnnouncement.content"></div>
        <div class="detail-footer">
          <p><strong>创建时间：</strong>{{ formatDateTime(currentAnnouncement.createdAt) }}</p>
          <p><strong>发布时间：</strong>{{ formatDateTime(currentAnnouncement.publishTime) }}</p>
          <p v-if="currentAnnouncement.effectiveEndTime"><strong>有效期至：</strong>{{ formatDateTime(currentAnnouncement.effectiveEndTime) }}</p>
          <p v-if="currentAnnouncement.readCount !== undefined"><strong>阅读次数：</strong>{{ currentAnnouncement.readCount }}</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 公告表单弹窗 -->
    <announcement-form
      :visible.sync="formVisible"
      :announcement-data="currentAnnouncement"
      :is-edit="isEdit"
      :is-draft="isDraft"
      @success="handleFormSuccess">
    </announcement-form>
  </div>
</template>

<script>
import { announcementApi } from '@/api'
import AnnouncementForm from './AnnouncementForm.vue'

export default {
  name: 'AnnouncementList',
  components: {
    AnnouncementForm
  },
  data() {
    return {
      loading: false,
      announcementList: [],
      searchForm: {
        title: '',
        type: '',
        status: '',
        priority: '',
        targetAudience: ''
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      formVisible: false,
      detailVisible: false,
      isEdit: false,
      isDraft: false,
      currentAnnouncement: null,
      multipleSelection: [],

      // 下拉选项数据
      announcementTypes: [],
      announcementStatuses: [],
      announcementPriorities: [],
      targetAudiences: []
    }
  },
  created() {
    this.fetchData()
    this.loadDropdownData()
  },
  methods: {
    // 获取公告列表
    async fetchAnnouncements() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          size: this.pagination.size,
          title: this.searchForm.title || undefined,
          type: this.searchForm.type || undefined,
          status: this.searchForm.status || undefined,
          priority: this.searchForm.priority || undefined,
          targetAudience: this.searchForm.targetAudience || undefined,
          sortBy: 'createdAt',
          sortDirection: 'desc'
        }

        const response = await announcementApi.getAnnouncements(params)
        if (response.code === 200) {
          this.announcementList = response.data.records || []
          this.pagination.total = response.data.total || 0
        }
      } catch (error) {
        console.error('获取公告列表失败:', error)
        this.$message.error('获取公告列表失败')
      } finally {
        this.loading = false
      }
    },

    // 加载下拉选项数据
    async loadDropdownData() {
      try {
        const [typesRes, statusesRes, prioritiesRes, audiencesRes] = await Promise.all([
          announcementApi.getAnnouncementTypes(),
          announcementApi.getAnnouncementStatuses(),
          announcementApi.getAnnouncementPriorities(),
          announcementApi.getTargetAudiences()
        ])

        if (typesRes.code === 200) this.announcementTypes = typesRes.data
        if (statusesRes.code === 200) this.announcementStatuses = statusesRes.data
        if (prioritiesRes.code === 200) this.announcementPriorities = prioritiesRes.data
        if (audiencesRes.code === 200) this.targetAudiences = audiencesRes.data
      } catch (error) {
        console.error('加载下拉选项失败:', error)
      }
    },

    // 获取数据
    fetchData() {
      this.fetchAnnouncements()
    },

    // 搜索
    handleSearch() {
      this.pagination.page = 1
      this.fetchAnnouncements()
    },

    // 重置搜索
    handleReset() {
      this.searchForm = {
        title: '',
        type: '',
        status: '',
        priority: '',
        targetAudience: ''
      }
      this.pagination.page = 1
      this.fetchAnnouncements()
    },

    // 刷新
    handleRefresh() {
      this.fetchData()
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.page = 1
      this.fetchAnnouncements()
    },

    // 当前页变化
    handleCurrentChange(page) {
      this.pagination.page = page
      this.fetchAnnouncements()
    },

    // 多选变化
    handleSelectionChange(val) {
      this.multipleSelection = val
    },

    // 批量操作
    async handleBatchOperation(command) {
      if (this.multipleSelection.length === 0) {
        this.$message.warning('请选择要操作的公告')
        return
      }

      const ids = this.multipleSelection.map(item => item.id)
      let actionName = ''

      try {
        switch (command) {
          case 'publish':
            actionName = '批量发布'
            await announcementApi.batchUpdateStatus('PUBLISHED', ids)
            break
          case 'cancel':
            actionName = '批量取消'
            await announcementApi.batchUpdateStatus('CANCELLED', ids)
            break
          case 'expired':
            actionName = '标记过期'
            await announcementApi.batchUpdateStatus('EXPIRED', ids)
            break
          case 'delete':
            actionName = '批量删除'
            await announcementApi.batchDeleteAnnouncements(ids)
            break
        }

        this.$message.success(`${actionName}成功`)
        this.fetchAnnouncements()
      } catch (error) {
        console.error(`${actionName}失败:`, error)
        this.$message.error(`${actionName}失败`)
      }
    },

    // 新增公告
    handleAdd() {
      this.currentAnnouncement = null
      this.isEdit = false
      this.isDraft = false
      this.formVisible = true
    },

    // 创建草稿
    handleCreateDraft() {
      this.currentAnnouncement = null
      this.isEdit = false
      this.isDraft = true
      this.formVisible = true
    },

    // 编辑公告
    handleEdit(announcement) {
      this.currentAnnouncement = { ...announcement }
      this.isEdit = true
      this.isDraft = announcement.status === 'DRAFT'
      this.formVisible = true
    },

    // 查看公告详情
    async handleView(announcement) {
      this.currentAnnouncement = announcement
      this.detailVisible = true

      // 增加阅读次数
      try {
        await announcementApi.getAnnouncementById(announcement.id)
      } catch (error) {
        console.error('更新阅读次数失败:', error)
      }
    },

    // 发布公告
    async handlePublish(announcement) {
      try {
        await this.$confirm(
          `确定要发布公告 "${announcement.title}" 吗？`,
          '发布公告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await announcementApi.publishDraft(announcement.id)
        if (response.code === 200) {
          this.$message.success('发布公告成功')
          this.fetchAnnouncements()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('发布公告失败:', error)
          this.$message.error('发布公告失败')
        }
      }
    },

    // 取消公告
    async handleCancel(announcement) {
      try {
        await this.$confirm(
          `确定要取消公告 "${announcement.title}" 吗？`,
          '取消公告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await announcementApi.cancelAnnouncement(announcement.id)
        if (response.code === 200) {
          this.$message.success('取消公告成功')
          this.fetchAnnouncements()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消公告失败:', error)
          this.$message.error('取消公告失败')
        }
      }
    },

    // 删除公告
    async handleDelete(announcement) {
      try {
        await this.$confirm(
          `确定要删除公告 "${announcement.title}" 吗？删除后将无法恢复。`,
          '删除公告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await announcementApi.deleteAnnouncement(announcement.id)
        if (response.code === 200) {
          this.$message.success('删除公告成功')
          this.fetchAnnouncements()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除公告失败:', error)
          this.$message.error('删除公告失败')
        }
      }
    },

    // 表单提交成功
    handleFormSuccess() {
      this.formVisible = false
      this.fetchAnnouncements()
    },

    // 获取类型文本
    getTypeText(type) {
      const typeMap = {
        'SYSTEM': '系统公告',
        'MAINTENANCE': '维护通知',
        'FEATURE': '功能更新',
        'HOLIDAY': '节假日通知',
        'URGENT': '紧急通知'
      }
      return typeMap[type] || type
    },

    // 获取类型标签类型
    getTypeTagType(type) {
      const typeMap = {
        'SYSTEM': 'primary',
        'MAINTENANCE': 'warning',
        'FEATURE': 'success',
        'HOLIDAY': 'info',
        'URGENT': 'danger'
      }
      return typeMap[type] || 'info'
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'PUBLISHED': '已发布',
        'EXPIRED': '已过期',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },

    // 获取状态标签类型
    getStatusTagType(status) {
      const statusMap = {
        'DRAFT': 'info',
        'PUBLISHED': 'success',
        'EXPIRED': 'warning',
        'CANCELLED': 'danger'
      }
      return statusMap[status] || 'info'
    },

    // 获取优先级文本
    getPriorityText(priority) {
      const priorityMap = {
        'LOW': '低',
        'NORMAL': '普通',
        'HIGH': '高',
        'URGENT': '紧急'
      }
      return priorityMap[priority] || priority
    },

    // 获取优先级标签类型
    getPriorityTagType(priority) {
      const priorityMap = {
        'LOW': 'info',
        'NORMAL': '',
        'HIGH': 'warning',
        'URGENT': 'danger'
      }
      return priorityMap[priority] || ''
    },

    // 获取目标受众文本
    getTargetAudienceText(audience) {
      const audienceMap = {
        'ALL': '全体用户',
        'ROLE_BASED': '基于角色',
        'DEPARTMENT': '基于部门',
        'SPECIFIC_USERS': '特定用户'
      }
      return audienceMap[audience] || audience
    },

    // 检查是否已过期
    isExpired(endTime) {
      if (!endTime) return false
      return new Date(endTime) < new Date()
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
.announcement-list {
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

.action-buttons .el-button {
  margin-right: 10px;
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

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pinned-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 14px;
}

.text-muted {
  color: #999;
}

.text-danger {
  color: #f56c6c;
}

.announcement-detail h2 {
  margin-top: 0;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 10px;
}

.detail-meta {
  margin: 15px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.pinned-info {
  color: #f56c6c;
  font-weight: bold;
}

.detail-content {
  margin: 20px 0;
  line-height: 1.6;
  min-height: 200px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.detail-footer {
  border-top: 1px solid #e4e7ed;
  padding-top: 15px;
  margin-top: 20px;
}

.detail-footer p {
  margin: 5px 0;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcement-list {
    padding: 10px;
  }

  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 10px;
  }

  .action-buttons .el-button {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>