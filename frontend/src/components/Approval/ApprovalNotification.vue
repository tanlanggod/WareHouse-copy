<template>
  <div class="approval-notification" v-if="showNotification">
    <!-- 待审批数量提醒 -->
    <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="notification-badge">
      <i class="el-icon-bell notification-icon" @click="showPendingDialog"></i>
    </el-badge>

    <!-- 审批通知弹窗 -->
    <el-dialog
      title="审批通知"
      :visible.sync="notificationDialogVisible"
      width="400px"
      :modal-append-to-body="false"
      :close-on-click-modal="true"
      :before-close="handleCloseNotification">
      <div v-if="latestNotification" class="notification-content">
        <div class="notification-header">
          <el-tag :type="getNotificationType(latestNotification)" size="small">
            {{ getNotificationTitle(latestNotification) }}
          </el-tag>
          <span class="notification-time">{{ formatTime(latestNotification.timestamp) }}</span>
        </div>
        <div class="notification-body">
          <div class="notification-item">
            <span class="label">业务类型:</span>
            <span>{{ latestNotification.businessType }}</span>
          </div>
          <div class="notification-item">
            <span class="label">业务单号:</span>
            <span>{{ latestNotification.businessNo }}</span>
          </div>
          <div class="notification-item">
            <span class="label">提交人:</span>
            <span>{{ latestNotification.submitter }}</span>
          </div>
          <div v-if="latestNotification.approver" class="notification-item">
            <span class="label">审批人:</span>
            <span>{{ latestNotification.approver }}</span>
          </div>
          <div v-if="latestNotification.approvalStatus" class="notification-item">
            <span class="label">审批状态:</span>
            <el-tag :type="getApprovalStatusType(latestNotification.approvalStatus)" size="small">
              {{ getApprovalStatusText(latestNotification.approvalStatus) }}
            </el-tag>
          </div>
          <div v-if="latestNotification.approvalRemark" class="notification-item">
            <span class="label">审批意见:</span>
            <span>{{ latestNotification.approvalRemark }}</span>
          </div>
        </div>
        <div class="notification-actions">
          <el-button size="mini" @click="viewDetails(latestNotification)">查看详情</el-button>
          <el-button size="mini" type="primary" @click="markAsRead">标记已读</el-button>
        </div>
      </div>
      <div v-else class="no-notification">
        <i class="el-icon-info"></i>
        <span>暂无审批通知</span>
      </div>
    </el-dialog>

    <!-- 待审批列表弹窗 -->
    <el-dialog
      title="待审批列表"
      :visible.sync="pendingDialogVisible"
      width="800px"
      :modal-append-to-body="false"
      :close-on-click-modal="true"
      :before-close="handleClosePendingDialog">
      <el-table :data="pendingApprovals" style="width: 100%">
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template slot-scope="scope">
            {{ getBusinessTypeText(scope.row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column label="业务单号" width="120">
          <template slot-scope="scope">
            {{ getBusinessNo(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="提交人" width="100">
          <template slot-scope="scope">
            {{ scope.row.submitter ? scope.row.submitter.realName : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="150">
          <template slot-scope="scope">
            {{ formatTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="handleApproval(scope.row)">
              审批
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { approvalApi } from '@/api'
import { mapState } from 'vuex'

export default {
  name: 'ApprovalNotification',
  data() {
    return {
      showNotification: false,
      notificationDialogVisible: false,
      pendingDialogVisible: false,
      pendingApprovals: [],
      pendingCount: 0,
      latestNotification: null,
      hasNotificationPermission: false,
      reconnectAttempts: 0,
      maxReconnectAttempts: 5,
      reconnectInterval: null
    }
  },
  computed: {
    ...mapState('user', ['userInfo'])
  },
  mounted() {
    this.initNotification()
    this.setupEventListeners()
  },
  beforeDestroy() {
    this.cleanup()
  },
  methods: {
    initNotification() {
      // 检查用户权限
      if (this.hasApprovalPermission()) {
        this.showNotification = true
        this.connectWebSocket()
        this.loadPendingApprovals()
      }
    },

    hasApprovalPermission() {
      if (!this.userInfo) return false
      const userRole = this.userInfo.role
      return userRole === 'ADMIN' || userRole === 'WAREHOUSE_KEEPER'
    },

    connectWebSocket() {
      try {
        if (this.$approvalWebSocket) {
          this.$approvalWebSocket.init(this.userInfo.id)
          console.log('审批WebSocket服务初始化成功')
        } else {
          console.warn('审批WebSocket服务未初始化')
        }
      } catch (error) {
        console.error('初始化审批通知服务失败:', error)
      }
    },

    setupEventListeners() {
      // 监听待审批数量变化
      this.$approvalEventBus.$on('pending-count-change', this.handlePendingCountChange)

      // 监听审批提交事件
      this.$approvalEventBus.$on('approval-submission', this.handleApprovalSubmission)

      // 监听审批结果事件
      this.$approvalEventBus.$on('approval-result', this.handleApprovalResult)

      // 监听连接状态变化
      if (this.$approvalWebSocket) {
        this.$approvalWebSocket.on('connection-change', this.handleConnectionChange)
      }
    },

    handlePendingCountChange(data) {
      this.pendingCount = data.pendingCount || 0
      console.log('待审批数量更新:', this.pendingCount)
    },

    handleApprovalSubmission(data) {
      this.latestNotification = data
      this.notificationDialogVisible = true

      // 显示通知
      this.$notify({
        title: '新审批申请',
        message: `${data.businessType} - ${data.businessNo}`,
        type: 'info',
        duration: 5000
      })
    },

    handleApprovalResult(data) {
      if (data.submitterId === this.userInfo.id) {
        // 审批结果通知给提交人
        const type = data.approvalStatus === 'APPROVED' ? 'success' : 'warning'
        const title = data.approvalStatus === 'APPROVED' ? '审批通过' : '审批拒绝'

        this.$notify({
          title,
          message: `${data.businessType} - ${data.businessNo} ${title}`,
          type,
          duration: 5000
        })
      }
    },

    handleConnectionChange(isConnected) {
      if (isConnected) {
        console.log('WebSocket连接已建立')
        this.loadPendingApprovals()
      } else {
        console.log('WebSocket连接已断开')
      }
    },

    async loadPendingApprovals() {
      try {
        const response = await approvalApi.getPendingApprovals({
          page: 1,
          size: 100
        })

        if (response.code === 200) {
          this.pendingApprovals = response.data.records || []
          this.pendingCount = response.data.total || 0
        }
      } catch (error) {
        console.error('加载待审批列表失败:', error)
        this.$message.error('加载待审批列表失败')
      }
    },

    showPendingDialog() {
      this.pendingDialogVisible = true
      this.loadPendingApprovals()
    },

    handleCloseNotification() {
      this.notificationDialogVisible = false
    },

    handleClosePendingDialog() {
      this.pendingDialogVisible = false
    },

    handleApproval(row) {
      // 关闭待审批列表弹窗
      this.pendingDialogVisible = false

      // 跳转到相应的业务列表页面，并打开审批对话框
      const routeMap = {
        'INBOUND': '/app/inbounds',
        'OUTBOUND': '/app/outbounds',
        'STOCK_ADJUSTMENT': '/app/stock/adjustment/list'
      }

      const route = routeMap[row.businessType]
      if (route) {
        // 跳转到列表页面，通过URL参数触发审批对话框
        this.$router.push({
          path: route,
          query: {
            approve: row.businessId,
            action: 'approve'
          }
        })
      } else {
        this.$message.error('未找到对应的业务页面')
      }
    },

    viewDetails(notification) {
      // 跳转到业务详情页面
      const routeMap = {
        'INBOUND': '/app/inbounds',
        'OUTBOUND': '/app/outbounds',
        'STOCK_ADJUSTMENT': '/app/stock/adjustment'
      }

      const route = routeMap[notification.businessType]
      if (route) {
        this.$router.push(`${route}/${notification.businessId}`)
        this.notificationDialogVisible = false
      } else {
        this.$message.error('未找到对应的业务页面')
      }
    },

    markAsRead() {
      this.latestNotification = null
      this.notificationDialogVisible = false
    },

    getNotificationType(notification) {
      if (notification.type === 'approval_submission') return 'info'
      if (notification.type === 'approval_result') {
        return notification.approvalStatus === 'APPROVED' ? 'success' : 'warning'
      }
      return 'info'
    },

    getNotificationTitle(notification) {
      if (notification.type === 'approval_submission') return '新审批申请'
      if (notification.type === 'approval_result') {
        return notification.approvalStatus === 'APPROVED' ? '审批通过' : '审批拒绝'
      }
      return '审批通知'
    },

    getApprovalStatusType(status) {
      const statusMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'CANCELLED': 'info'
      }
      return statusMap[status] || 'info'
    },

    getApprovalStatusText(status) {
      const statusMap = {
        'PENDING': '待审批',
        'APPROVED': '已批准',
        'REJECTED': '已拒绝',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },

    getBusinessTypeText(type) {
      const typeMap = {
        'INBOUND': '入库',
        'OUTBOUND': '出库',
        'STOCK_ADJUSTMENT': '库存调整'
      }
      return typeMap[type] || type
    },

    getBusinessNo(record) {
      // 对于入库单，显示inboundNo
      // 对于出库单，显示outboundNo
      // 对于库存调整，显示adjustmentNo
      // 由于后端没有返回这些字段，暂时显示businessId
      return `${record.businessId}`
    },

    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    },

    cleanup() {
      // 清理事件监听
      if (this.$approvalEventBus) {
        this.$approvalEventBus.$off('pending-count-change', this.handlePendingCountChange)
        this.$approvalEventBus.$off('approval-submission', this.handleApprovalSubmission)
        this.$approvalEventBus.$off('approval-result', this.handleApprovalResult)

        if (this.$approvalWebSocket) {
          this.$approvalWebSocket.off('connection-change', this.handleConnectionChange)
        }
      }
    }
  }
}
</script>

<style scoped>
.approval-notification {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 2000;
}

.notification-badge {
  cursor: pointer;
}

.notification-icon {
  font-size: 18px;
  color: #409EFF;
}

.notification-content {
  max-height: 400px;
  overflow-y: auto;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-body {
  line-height: 1.6;
}

.notification-item {
  margin-bottom: 8px;
}

.notification-item .label {
  color: #606266;
  font-weight: 500;
  margin-right: 8px;
}

.notification-actions {
  margin-top: 20px;
  text-align: right;
}

.no-notification {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.no-notification i {
  font-size: 48px;
  margin-right: 10px;
}
</style>