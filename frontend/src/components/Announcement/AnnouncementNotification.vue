<template>
  <div class="announcement-notification">
    <!-- 置顶公告区域 -->
    <div v-if="pinnedAnnouncements.length > 0" class="pinned-announcements">
      <el-carousel
        :interval="5000"
        :height="pinnedCarouselHeight"
        :autoplay="pinnedAnnouncements.length > 1"
        indicator-position="outside">
        <el-carousel-item v-for="announcement in pinnedAnnouncements" :key="announcement.id">
          <div class="pinned-announcement-item" @click="handleViewAnnouncement(announcement)">
            <div class="pinned-content">
              <el-tag type="danger" size="mini" class="pinned-tag">
                <i class="el-icon-top"></i> 置顶
              </el-tag>
              <span class="pinned-title">{{ announcement.title }}</span>
              <el-tag :type="getPriorityTagType(announcement.priority)" size="mini">
                {{ getPriorityText(announcement.priority) }}
              </el-tag>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 通知图标和数量 -->
    <div class="notification-trigger" @click="showNotificationPanel">
      <div class="notification-icon-wrapper" :class="{ 'has-unread': unreadCount > 0 }">
        <i class="el-icon-bell notification-bell"></i>
        <transition name="bounce">
          <span v-if="unreadCount > 0" class="notification-count">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </transition>
      </div>
    </div>

    <!-- 通知面板 -->
    <el-drawer
      :visible.sync="drawerVisible"
      direction="rtl"
      size="420px"
      :with-header="false"
      :append-to-body="true"
      class="notification-drawer">
      
      <!-- 自定义头部 -->
      <div class="drawer-header">
        <div class="header-content">
          <div class="header-title">
            <i class="el-icon-bell"></i>
            <span>系统公告</span>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="header-badge"></el-badge>
          </div>
          <el-button 
            type="text" 
            icon="el-icon-close" 
            @click="drawerVisible = false"
            class="close-btn">
          </el-button>
        </div>
        
        <!-- Tab切换 -->
        <div class="drawer-tabs">
          <div 
            class="tab-item" 
            :class="{ 'active': activeTab === 'all' }"
            @click="activeTab = 'all'">
            全部
          </div>
          <div 
            class="tab-item" 
            :class="{ 'active': activeTab === 'unread' }"
            @click="activeTab = 'unread'">
            未读 <span v-if="unreadCount > 0" class="tab-count">({{ unreadCount }})</span>
          </div>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="search-section" v-if="visibleAnnouncements.length > 0">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索公告..."
          prefix-icon="el-icon-search"
          clearable
          @input="handleSearch">
        </el-input>
      </div>

      <!-- 公告列表 -->
      <div class="announcement-list" v-loading="loading">
        <div v-if="filteredAnnouncements.length === 0" class="empty-message">
          <i class="el-icon-info"></i>
          <p>暂无公告</p>
        </div>

        <div v-else class="announcement-items">
          <div
            v-for="announcement in filteredAnnouncements"
            :key="announcement.id"
            class="announcement-item"
            :class="{ 'is-unread': !announcement.isRead, 'is-urgent': announcement.priority === 'URGENT' }"
            @click="handleViewAnnouncement(announcement)">

            <div class="announcement-header">
              <div class="announcement-meta">
                <el-tag :type="getTypeTagType(announcement.type)" size="mini">
                  {{ getTypeText(announcement.type) }}
                </el-tag>
                <el-tag :type="getPriorityTagType(announcement.priority)" size="mini">
                  {{ getPriorityText(announcement.priority) }}
                </el-tag>
                <span v-if="announcement.isPinned" class="pinned-indicator">
                  <i class="el-icon-top"></i>
                </span>
              </div>
              <div class="announcement-time">
                {{ formatTime(announcement.publishTime || announcement.createdAt) }}
              </div>
            </div>

            <div class="announcement-content">
              <h4 class="announcement-title">{{ announcement.title }}</h4>
              <p class="announcement-summary" v-if="announcement.content">
                {{ stripHtml(announcement.content).substring(0, 100) }}
                <span v-if="stripHtml(announcement.content).length > 100">...</span>
              </p>
            </div>

            <div class="announcement-footer" v-if="announcement.effectiveEndTime">
              <span class="effective-time" :class="{ 'expired': isExpired(announcement.effectiveEndTime) }">
                有效期至: {{ formatDate(announcement.effectiveEndTime) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 查看更多 -->
      <div class="view-more" v-if="visibleAnnouncements.length > 0">
        <el-button type="text" @click="handleViewMore" style="width: 100%;">
          查看全部公告 <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>
    </el-drawer>

    <!-- 公告详情弹窗 -->
    <el-dialog
      title="公告详情"
      :visible.sync="detailVisible"
      width="60%"
      :close-on-click-modal="true"
      :before-close="handleDetailClose"
      class="announcement-detail-dialog">
      <div v-if="currentAnnouncement" class="announcement-detail">
        <div class="detail-header">
          <h2>{{ currentAnnouncement.title }}</h2>
          <div class="detail-meta">
            <el-tag :type="getTypeTagType(currentAnnouncement.type)">
              {{ getTypeText(currentAnnouncement.type) }}
            </el-tag>
            <el-tag :type="getPriorityTagType(currentAnnouncement.priority)" size="mini">
              {{ getPriorityText(currentAnnouncement.priority) }}
            </el-tag>
            <el-tag type="success" v-if="currentAnnouncement.isPinned" size="mini">
              <i class="el-icon-top"></i> 置顶
            </el-tag>
          </div>
        </div>

        <div class="detail-content" v-html="currentAnnouncement.content"></div>

        <div class="detail-footer">
          <div class="detail-info">
            <p><strong>发布时间：</strong>{{ formatDateTime(currentAnnouncement.publishTime) }}</p>
            <p v-if="currentAnnouncement.effectiveEndTime">
              <strong>有效期至：</strong>{{ formatDateTime(currentAnnouncement.effectiveEndTime) }}
            </p>
            <p v-if="currentAnnouncement.readCount !== undefined">
              <strong>阅读次数：</strong>{{ currentAnnouncement.readCount }}
            </p>
          </div>
          <div class="detail-actions">
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button v-if="currentAnnouncement.attachmentUrl" type="primary" @click="handleDownloadAttachment">
              <i class="el-icon-download"></i> 下载附件
            </el-button>
          </div>
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
      drawerVisible: false,
      detailVisible: false,
      searchKeyword: '',
      activeTab: 'all', // 新增：当前激活的标签页

      pinnedAnnouncements: [],
      visibleAnnouncements: [],
      readAnnouncementIds: [],

      currentAnnouncement: null,
      refreshTimer: null
    }
  },
  computed: {
    unreadCount() {
      return this.visibleAnnouncements.filter(announcement =>
        !this.readAnnouncementIds.includes(announcement.id)
      ).length
    },

    pinnedCarouselHeight() {
      return this.pinnedAnnouncements.length > 0 ? '50px' : '0px'
    },

    filteredAnnouncements() {
      let announcements = this.visibleAnnouncements

      // 根据标签页筛选
      if (this.activeTab === 'unread') {
        announcements = announcements.filter(announcement =>
          !this.readAnnouncementIds.includes(announcement.id)
        )
      }

      // 关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        announcements = announcements.filter(announcement =>
          announcement.title.toLowerCase().includes(keyword) ||
          announcement.content.toLowerCase().includes(keyword)
        )
      }

      return announcements
    }
  },
  mounted() {
    this.loadAnnouncements()
    this.loadReadStatus()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    this.stopAutoRefresh()
  },
  methods: {
    // 加载公告数据
    async loadAnnouncements() {
      try {
        this.loading = true

        const [pinnedRes, visibleRes] = await Promise.all([
          announcementApi.getPinnedAnnouncements(),
          announcementApi.getVisibleAnnouncements()
        ])

        if (pinnedRes.code === 200) {
          this.pinnedAnnouncements = pinnedRes.data || []
        }

        if (visibleRes.code === 200) {
          this.visibleAnnouncements = visibleRes.data || []
        }
      } catch (error) {
        console.error('加载公告失败:', error)
      } finally {
        this.loading = false
      }
    },

    // 加载已读状态
    loadReadStatus() {
      const stored = localStorage.getItem('readAnnouncementIds')
      if (stored) {
        try {
          this.readAnnouncementIds = JSON.parse(stored)
        } catch (error) {
          console.error('解析已读状态失败:', error)
          this.readAnnouncementIds = []
        }
      }
    },

    // 保存已读状态
    saveReadStatus() {
      try {
        localStorage.setItem('readAnnouncementIds', JSON.stringify(this.readAnnouncementIds))
      } catch (error) {
        console.error('保存已读状态失败:', error)
      }
    },

    // 开始自动刷新
    startAutoRefresh() {
      this.refreshTimer = setInterval(() => {
        this.loadAnnouncements()
      }, 5 * 60 * 1000) // 5分钟刷新一次
    },

    // 停止自动刷新
    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },

    // 显示通知面板
    showNotificationPanel() {
      this.drawerVisible = true
    },

    // 搜索处理
    handleSearch() {
      // 搜索逻辑在计算属性中实现
    },

    // 查看公告详情
    async handleViewAnnouncement(announcement) {
      this.currentAnnouncement = announcement
      this.detailVisible = true

      // 标记为已读
      if (!this.readAnnouncementIds.includes(announcement.id)) {
        this.readAnnouncementIds.push(announcement.id)
        this.saveReadStatus()

        // 调用接口增加阅读次数
        try {
          await announcementApi.getAnnouncementById(announcement.id)
        } catch (error) {
          console.error('更新阅读次数失败:', error)
        }
      }
    },

    // 查看更多公告
    handleViewMore() {
      this.drawerVisible = false
      this.$router.push('/app/announcements')
    },

    // 下载附件
    handleDownloadAttachment() {
      if (this.currentAnnouncement && this.currentAnnouncement.attachmentUrl) {
        window.open(this.currentAnnouncement.attachmentUrl, '_blank')
      }
    },

    // 关闭详情弹窗
    handleDetailClose() {
      this.detailVisible = false
      this.currentAnnouncement = null
    },

    // 去除HTML标签
    stripHtml(html) {
      if (!html) return ''
      return html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ')
    },

    // 检查是否已过期
    isExpired(endTime) {
      if (!endTime) return false
      return new Date(endTime) < new Date()
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      const now = new Date()
      const date = new Date(time)
      const diff = now - date

      if (diff < 60 * 1000) {
        return '刚刚'
      } else if (diff < 60 * 60 * 1000) {
        return Math.floor(diff / (60 * 1000)) + '分钟前'
      } else if (diff < 24 * 60 * 60 * 1000) {
        return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
      } else if (diff < 7 * 24 * 60 * 60 * 1000) {
        return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前'
      } else {
        return this.formatDate(time)
      }
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },

    // 格式化日期时间
    formatDateTime(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
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
    }
  }
}
</script>

<style scoped>
.announcement-notification {
  position: relative;
  display: flex;
  align-items: center;
}

/* 置顶公告样式 - 优化为顶部横幅样式 */
.pinned-announcements {
  position: fixed;
  top: 70px;
  left: 280px;
  right: 20px;
  z-index: 999;
  max-width: calc(100% - 300px);
  transition: all 0.3s ease;
}

.pinned-announcement-item {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  height: 100%;
}

.pinned-announcement-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.pinned-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.pinned-tag {
  background: rgba(255, 255, 255, 0.25) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: white !important;
  font-weight: 500;
  padding: 2px 8px;
}

.pinned-title {
  flex: 1;
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.3px;
}

/* 通知图标样式 - 现代化设计 */
.notification-trigger {
  cursor: pointer;
  position: relative;
}

.notification-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--bg-secondary, #f5f7fa);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.notification-icon-wrapper:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: scale(1.05);
}

.notification-icon-wrapper.has-unread {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(102, 126, 234, 0);
  }
}

.notification-bell {
  font-size: 20px;
  color: #606266;
  transition: all 0.3s ease;
}

.notification-icon-wrapper:hover .notification-bell {
  color: white;
  transform: rotate(15deg);
}

.notification-count {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 18px;
  height: 18px;
  background: linear-gradient(135deg, #f56c6c, #ff8585);
  color: white;
  border-radius: 9px;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
  border: 2px solid white;
}

.bounce-enter-active {
  animation: bounce-in 0.5s;
}
.bounce-leave-active {
  animation: bounce-in 0.3s reverse;
}
@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 通知抽屉样式 - 全新设计 */
.notification-drawer >>> .el-drawer {
  border-radius: 20px 0 0 20px;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.1);
}

.notification-drawer >>> .el-drawer__body {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 自定义头部 */
.drawer-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
}

.header-title i {
  font-size: 22px;
}

.header-badge >>> .el-badge__content {
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
  border: none;
  font-weight: 600;
}

.close-btn {
  color: white !important;
  font-size: 20px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

/* Tab切换 */
.drawer-tabs {
  display: flex;
  padding: 0 24px;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.tab-item {
  padding: 12px 20px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tab-item:hover {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.tab-item.active {
  color: white;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: white;
  border-radius: 3px 3px 0 0;
}

.tab-count {
  font-size: 12px;
  opacity: 0.8;
}

.search-section {
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8eaed;
}

.search-section >>> .el-input__inner {
  border-radius: 20px;
  border: 1px solid #e8eaed;
  background: white;
  transition: all 0.3s ease;
}

.search-section >>> .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.announcement-list {
  flex: 1;
  overflow-y: auto;
  background: white;
}

/* 自定义滚动条 */
.announcement-list::-webkit-scrollbar {
  width: 6px;
}

.announcement-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.announcement-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.announcement-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.empty-message {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-message i {
  font-size: 64px;
  margin-bottom: 16px;
  display: block;
  color: #dcdfe6;
}

.empty-message p {
  font-size: 14px;
  margin: 0;
}

.announcement-items {
  padding: 0;
}

.announcement-item {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item:hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.05) 0%, transparent 100%);
}

.announcement-item.is-unread {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.08) 0%, rgba(102, 126, 234, 0.02) 100%);
}

.announcement-item.is-unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
}

.announcement-item.is-urgent {
  border-left: 4px solid #f56c6c;
  background: linear-gradient(90deg, rgba(245, 108, 108, 0.05) 0%, transparent 100%);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.announcement-meta >>> .el-tag {
  border-radius: 4px;
  font-size: 11px;
  padding: 0 6px;
  height: 20px;
  line-height: 20px;
}

.pinned-indicator {
  color: #f56c6c;
  font-size: 14px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

.announcement-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.announcement-title {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-summary {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-footer {
  margin-top: 8px;
}

.effective-time {
  font-size: 11px;
  color: #909399;
}

.effective-time.expired {
  color: #f56c6c;
}

.view-more {
  padding: 16px 24px;
  border-top: 1px solid #e8eaed;
  background: #fafbfc;
}

.view-more >>> .el-button {
  color: #667eea;
  font-weight: 500;
  transition: all 0.3s ease;
}

.view-more >>> .el-button:hover {
  color: #764ba2;
  background: rgba(102, 126, 234, 0.05);
}

/* 公告详情弹窗样式 */
.announcement-detail-dialog >>> .el-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.announcement-detail-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
}

.announcement-detail-dialog >>> .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.announcement-detail-dialog >>> .el-dialog__headerbtn .el-dialog__close {
  color: white;
  font-size: 20px;
}

.announcement-detail-dialog >>> .el-dialog__body {
  padding: 24px;
}

.detail-header {
  border-bottom: 2px solid #e8eaed;
  padding-bottom: 16px;
  margin-bottom: 24px;
}

.detail-header h2 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-meta >>> .el-tag {
  border-radius: 6px;
  font-size: 12px;
  padding: 4px 10px;
  font-weight: 500;
}

.detail-content {
  line-height: 1.8;
  min-height: 200px;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 8px;
  border-left: 4px solid #667eea;
  font-size: 14px;
  color: #606266;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.detail-footer {
  border-top: 2px solid #e8eaed;
  padding-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.detail-info p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.detail-info strong {
  color: #303133;
  margin-right: 8px;
}

.detail-actions {
  display: flex;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .pinned-announcements {
    left: 100px;
    max-width: calc(100% - 120px);
  }
}

@media (max-width: 768px) {
  .pinned-announcements {
    position: static;
    left: auto;
    right: auto;
    max-width: 100%;
    margin: 10px;
  }

  .notification-drawer >>> .el-drawer {
    width: 100% !important;
    border-radius: 0;
  }

  .announcement-item {
    padding: 14px 16px;
  }

  .search-section {
    padding: 12px 16px;
  }

  .view-more {
    padding: 12px 16px;
  }

  .detail-footer {
    flex-direction: column;
    gap: 16px;
  }

  .detail-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .announcement-detail-dialog >>> .el-dialog {
    width: 95% !important;
    margin-top: 5vh;
  }
}

@media (max-width: 480px) {
  .notification-icon-wrapper {
    width: 36px;
    height: 36px;
  }

  .notification-bell {
    font-size: 18px;
  }

  .header-title {
    font-size: 16px;
  }

  .tab-item {
    padding: 10px 12px;
    font-size: 13px;
  }
}
</style>