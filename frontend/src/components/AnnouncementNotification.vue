<template>
  <div class="announcement-notification" :class="{ 'has-pinned': pinnedAnnouncements.length > 0 }">
    <!-- 页面顶部间距，避免被公告遮挡 -->
    <div
      class="announcement-spacer"
      :class="{ 'with-pinned': pinnedAnnouncements.length > 0 }">
    </div>

    <!-- 置顶公告轮播 -->
    <div v-if="pinnedAnnouncements.length > 0" class="pinned-announcements">
      <el-carousel height="60px" direction="vertical" :autoplay="true" :interval="5000" indicator-position="none">
        <el-carousel-item v-for="announcement in pinnedAnnouncements" :key="announcement.id">
          <div class="announcement-item" @click="showDetail(announcement)">
            <i class="el-icon-bell announcement-icon"></i>
            <span class="announcement-title">{{ announcement.title }}</span>
            <el-tag :type="getPriorityType(announcement.priority)" size="mini">
              {{ getPriorityText(announcement.priority) }}
            </el-tag>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 公告通知按钮 -->
    <div class="announcement-trigger" @click="showDrawer = true">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="announcement-badge">
        <i class="el-icon-bell"></i>
      </el-badge>
    </div>

    <!-- 公告详情抽屉 -->
    <el-drawer
      title="系统公告"
      :visible.sync="showDrawer"
      direction="rtl"
      :size="drawerSize"
      :modal-append-to-body="false"
      :wrapperClosable="true"
      :before-close="handleClose"
      custom-class="announcement-drawer-wrapper">
      <div class="announcement-drawer">
        <div v-if="loading" class="loading-container">
          <el-loading :loading="true"></el-loading>
        </div>

        <div v-else>
          <!-- 置顶公告 -->
          <div v-if="pinnedAnnouncements.length > 0" class="announcement-section">
            <h3 class="section-title">
              <i class="el-icon-star-on"></i> 置顶公告
            </h3>
            <div
              v-for="announcement in pinnedAnnouncements"
              :key="announcement.id"
              class="announcement-card pinned"
              @click="showDetail(announcement)">
              <div class="card-header">
                <span class="card-title">{{ announcement.title }}</span>
                <el-tag :type="getPriorityType(announcement.priority)" size="mini">
                  {{ getPriorityText(announcement.priority) }}
                </el-tag>
              </div>
              <div class="card-meta">
                <span>{{ formatDate(announcement.publishTime) }}</span>
                <span>阅读 {{ announcement.readCount || 0 }}</span>
              </div>
            </div>
          </div>

          <!-- 普通公告 -->
          <div v-if="normalAnnouncements.length > 0" class="announcement-section">
            <h3 class="section-title">
              <i class="el-icon-bell"></i> 最新公告
            </h3>
            <div
              v-for="announcement in normalAnnouncements"
              :key="announcement.id"
              class="announcement-card"
              @click="showDetail(announcement)">
              <div class="card-header">
                <span class="card-title">{{ announcement.title }}</span>
                <el-tag :type="getPriorityType(announcement.priority)" size="mini">
                  {{ getPriorityText(announcement.priority) }}
                </el-tag>
              </div>
              <div class="card-meta">
                <span>{{ formatDate(announcement.publishTime) }}</span>
                <span>阅读 {{ announcement.readCount || 0 }}</span>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="pinnedAnnouncements.length === 0 && normalAnnouncements.length === 0" class="empty-state">
            <i class="el-icon-bell empty-icon"></i>
            <p>暂无公告</p>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 公告详情对话框 -->
    <el-dialog
      title="公告详情"
      :visible.sync="showDetailDialog"
      width="600px"
      :before-close="handleDetailClose">
      <div v-if="currentAnnouncement" class="announcement-detail">
        <h2 class="detail-title">{{ currentAnnouncement.title }}</h2>

        <div class="detail-meta">
          <el-tag :type="getPriorityType(currentAnnouncement.priority)">
            {{ getPriorityText(currentAnnouncement.priority) }}
          </el-tag>
          <span class="publish-time">发布时间：{{ formatDate(currentAnnouncement.publishTime) }}</span>
          <span class="read-count">阅读次数：{{ currentAnnouncement.readCount || 0 }}</span>
        </div>

        <div class="detail-content" v-html="currentAnnouncement.content"></div>

        <div v-if="currentAnnouncement.attachmentUrl" class="detail-attachment">
          <el-link :href="currentAnnouncement.attachmentUrl" target="_blank" type="primary">
            <i class="el-icon-paperclip"></i>
            {{ currentAnnouncement.attachmentName || '下载附件' }}
          </el-link>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { announcementApi } from '@/api'

export default {
  name: 'AnnouncementNotification',
  data() {
    return {
      loading: false,
      showDrawer: false,
      showDetailDialog: false,
      currentAnnouncement: null,
      pinnedAnnouncements: [],
      normalAnnouncements: [],
      refreshTimer: null
    }
  },
  computed: {
    hasAnnouncements() {
      return this.pinnedAnnouncements.length > 0 || this.normalAnnouncements.length > 0
    },
    unreadCount() {
      // 这里可以根据实际业务逻辑计算未读数量
      // 暂时返回总数量作为演示
      return this.pinnedAnnouncements.length + this.normalAnnouncements.length
    },
    drawerSize() {
      // 根据屏幕尺寸动态调整抽屉大小
      if (window.innerWidth <= 480) {
        return '85%'
      } else if (window.innerWidth <= 768) {
        return '80%'
      }
      return '420px'
    }
  },
  mounted() {
    this.loadAnnouncements()
    this.startAutoRefresh()
    this.updateBodyClass()
  },
  beforeDestroy() {
    this.stopAutoRefresh()
    this.cleanupBodyClass()
  },
  methods: {
    async loadAnnouncements() {
      try {
        this.loading = true

        // 并行加载置顶公告和可见公告
        const [pinnedResponse, visibleResponse] = await Promise.all([
          announcementApi.getPinnedAnnouncements(),
          announcementApi.getVisibleAnnouncements()
        ])

        this.pinnedAnnouncements = pinnedResponse.data || []

        // 过滤掉置顶公告，避免重复显示
        const pinnedIds = this.pinnedAnnouncements.map(item => item.id)
        this.normalAnnouncements = (visibleResponse.data || [])
          .filter(item => !pinnedIds.includes(item.id))
          .slice(0, 10) // 最多显示10条普通公告

      } catch (error) {
        console.error('加载公告失败:', error)
        this.$message.error('加载公告失败')
      } finally {
        this.loading = false
      }
    },

    startAutoRefresh() {
      // 每5分钟自动刷新一次
      this.refreshTimer = setInterval(() => {
        this.loadAnnouncements()
      }, 5 * 60 * 1000)
    },

    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },

    showDetail(announcement) {
      this.currentAnnouncement = announcement
      this.showDetailDialog = true

      // 增加阅读次数（异步调用，不阻塞用户操作）
      this.incrementReadCount(announcement.id)
    },

    async incrementReadCount(announcementId) {
      try {
        await announcementApi.incrementReadCount(announcementId)
      } catch (error) {
        console.warn('增加阅读次数失败:', error)
      }
    },

    handleClose() {
      this.showDrawer = false
    },

    handleDetailClose() {
      this.showDetailDialog = false
      this.currentAnnouncement = null
    },

    getPriorityType(priority) {
      const map = {
        'URGENT': 'danger',
        'HIGH': 'warning',
        'NORMAL': 'info',
        'LOW': ''
      }
      return map[priority] || 'info'
    },

    getPriorityText(priority) {
      const map = {
        'URGENT': '紧急',
        'HIGH': '高',
        'NORMAL': '普通',
        'LOW': '低'
      }
      return map[priority] || '普通'
    },

    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },

    updateBodyClass() {
      this.cleanupBodyClass()
      if (this.pinnedAnnouncements.length > 0) {
        document.body.classList.add('has-pinned-announcement')
      }
    },

    cleanupBodyClass() {
      document.body.classList.remove('has-pinned-announcement')
    }
  },
  watch: {
    pinnedAnnouncements: {
      handler(newVal) {
        this.$nextTick(() => {
          this.updateBodyClass()
        })
      },
      deep: true,
      immediate: true
    }
  }
}
</script>

<style scoped>
.announcement-notification {
  position: relative;
}

.pinned-announcements {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1001;
  max-width: 600px;
  width: 90%;
  pointer-events: auto;
}

.announcement-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: var(--radius-lg);
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.announcement-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-xl);
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.announcement-icon {
  margin-right: 10px;
  font-size: 18px;
  animation: ring 2s infinite;
}

@keyframes ring {
  0%, 20%, 50%, 80%, 100% { transform: rotate(0deg); }
  10% { transform: rotate(-10deg); }
  30% { transform: rotate(10deg); }
  60% { transform: rotate(-5deg); }
  70% { transform: rotate(5deg); }
}

.announcement-title {
  flex: 1;
  margin-right: 10px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.announcement-trigger {
  position: relative;
  cursor: pointer;
  z-index: 1002;
}

.announcement-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: 18px;
  border-radius: var(--radius-lg);
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
}

.announcement-badge:hover {
  background: var(--primary-color);
  color: var(--text-inverse);
  transform: scale(1.05);
  border-color: var(--primary-color);
}

/* 适配主题色彩 */
[data-theme="dark"] .announcement-badge {
  background: var(--bg-tertiary);
  border-color: var(--border-color);
}

[data-theme="dark"] .announcement-badge:hover {
  background: var(--primary-color);
}

.announcement-drawer {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

/* 移动端抽屉优化 */
@media (max-width: 768px) {
  .announcement-drawer {
    padding: 15px;
  }
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.announcement-section {
  margin-bottom: 30px;
}

.section-title {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.section-title i {
  margin-right: 8px;
  color: #409EFF;
}

.announcement-card {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.announcement-card:hover {
  background: #e9ecef;
  border-color: #409EFF;
  transform: translateX(-4px);
}

.announcement-card.pinned {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  border-color: #fdcb6e;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-title {
  font-weight: 500;
  color: #303133;
  flex: 1;
  margin-right: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  display: block;
}

.announcement-detail {
  padding: 20px 0;
}

.detail-title {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
  text-align: center;
}

.detail-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
  flex-wrap: wrap;
}

.publish-time, .read-count {
  color: #606266;
  font-size: 14px;
}

.detail-content {
  line-height: 1.8;
  color: #303133;
  margin-bottom: 20px;
}

.detail-content::v-deep img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.detail-content::v-deep p {
  margin-bottom: 15px;
}

.detail-attachment {
  text-align: center;
  padding-top: 15px;
  border-top: 1px solid #e9ecef;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .pinned-announcements {
    max-width: 500px;
  }
}

@media (max-width: 768px) {
  .pinned-announcements {
    position: fixed;
    top: 70px;
    left: 20px;
    right: 20px;
    transform: none;
    max-width: none;
    width: auto;
    z-index: 998;
  }

  .announcement-trigger {
    position: relative;
  }

  .announcement-badge {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .announcement-card {
    padding: 12px;
  }

  .section-title {
    font-size: 14px;
  }

  .card-title {
    font-size: 14px;
  }

  .card-meta {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .pinned-announcements {
    top: 65px;
    left: 10px;
    right: 10px;
  }

  .announcement-item {
    padding: 12px 16px;
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
  }

  .announcement-title {
    margin: 8px 0;
    white-space: normal;
    line-height: 1.4;
  }

  .announcement-icon {
    align-self: flex-end;
    margin-right: 0;
    margin-bottom: 8px;
  }

  .announcement-drawer {
    padding: 10px;
  }

  .announcement-section {
    margin-bottom: 20px;
  }

  .section-title {
    font-size: 13px;
  }

  .announcement-card {
    padding: 10px;
    margin-bottom: 8px;
  }

  .card-title {
    font-size: 13px;
    line-height: 1.3;
  }

  .card-meta {
    font-size: 10px;
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
}

/* 抽屉样式优化 */
.announcement-drawer-wrapper {
  z-index: 3000 !important;
}

.announcement-drawer-wrapper .el-drawer__header {
  padding: 20px 20px 10px 20px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 0;
}

.announcement-drawer-wrapper .el-drawer__body {
  padding: 0 !important;
}

/* 置顶公告在不同主题下的背景色调整 */
[data-theme="dark"] .pinned-announcements {
  z-index: 998;
}

[data-theme="dark"] .announcement-item {
  background: linear-gradient(135deg, #4c1d95 0%, #5b21b6 50%, #6d28d9 100%);
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .announcement-item:hover {
  background: linear-gradient(135deg, #6d28d9 0%, #5b21b6 50%, #4c1d95 100%);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.4);
}

/* 添加页面顶部间距，避免被公告遮挡 */
.announcement-spacer {
  height: 0;
  transition: height 0.3s ease;
  display: block;
  width: 100%;
}

.announcement-spacer.with-pinned {
  height: 70px;
}

@media (max-width: 768px) {
  .announcement-spacer.with-pinned {
    height: 100px;
  }
}

@media (max-width: 480px) {
  .announcement-spacer.with-pinned {
    height: 120px;
  }
}

/* 确保公告组件有足够的空间 */
.announcement-notification {
  position: relative;
  z-index: auto;
}

/* 防止页面内容被固定的公告遮挡 - 备选方案 */
body.has-pinned-announcement .content-area,
body.has-pinned-announcement main {
  padding-top: 70px;
}

@media (max-width: 768px) {
  body.has-pinned-announcement .content-area,
  body.has-pinned-announcement main {
    padding-top: 100px;
  }
}

@media (max-width: 480px) {
  body.has-pinned-announcement .content-area,
  body.has-pinned-announcement main {
    padding-top: 120px;
  }
}

/* 对于支持:has()的浏览器 */
@supports selector(:has(*)) {
  body:has(.pinned-announcements) .content-area,
  body:has(.pinned-announcements) main {
    padding-top: 70px;
  }

  @media (max-width: 768px) {
    body:has(.pinned-announcements) .content-area,
    body:has(.pinned-announcements) main {
      padding-top: 100px;
    }
  }

  @media (max-width: 480px) {
    body:has(.pinned-announcements) .content-area,
    body:has(.pinned-announcements) main {
      padding-top: 120px;
    }
  }
}
</style>