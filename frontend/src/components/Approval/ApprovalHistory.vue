<template>
  <el-dialog
    title="审批历史记录"
    :visible.sync="visible"
    width="800px"
    :before-close="handleClose"
    @close="handleClose">

    <!-- 业务信息 -->
    <el-card class="business-info">
      <div slot="header">
        <span>业务信息</span>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业务单号">{{ businessData.businessNo }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ businessData.businessType }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(businessData.submitTime) }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ businessData.submitter }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getApprovalStatusType(businessData.approvalStatus)">
            {{ getApprovalStatusText(businessData.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 审批历史表格 -->
    <el-card class="approval-history">
      <div slot="header">
        <span>审批历史</span>
      </div>
      <el-table :data="approvalHistory" style="width: 100%">
        <el-table-column prop="approvalTime" label="审批时间" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.approvalTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="approver.realName" label="审批人" width="120"></el-table-column>
        <el-table-column prop="approvalStatus" label="审批状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getApprovalStatusType(scope.row.approvalStatus)">
              {{ getApprovalStatusText(scope.row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalRemark" label="审批意见" width="200">
          <template slot-scope="scope">
            {{ scope.row.approvalRemark || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 底部按钮 -->
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { approvalApi, inboundApi, outboundApi, stockAdjustmentApi } from '@/api'

export default {
  name: 'ApprovalHistory',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    businessId: {
      type: Number,
      required: true
    },
    businessType: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      businessData: {
        businessNo: '',
        businessType: '',
        submitTime: null,
        submitter: '',
        approvalStatus: ''
      },
      approvalHistory: []
    }
  },
  created() {
    console.log('ApprovalHistory created, visible:', this.visible, 'businessId:', this.businessId, 'businessType:', this.businessType)
  },
  mounted() {
    console.log('ApprovalHistory mounted, visible:', this.visible, 'businessId:', this.businessId, 'businessType:', this.businessType)
    if (this.visible) {
      this.loadApprovalHistory()
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        console.log('ApprovalHistory visible changed, businessId:', this.businessId, 'businessType:', this.businessType)
        this.loadApprovalHistory()
      }
    }
  },
  methods: {
    async loadApprovalHistory() {
      try {
        console.log('开始加载审批历史，businessId:', this.businessId, 'businessType:', this.businessType)

        // 根据业务类型获取业务数据
        await this.loadBusinessData()

        // 加载审批历史
        console.log('调用approvalApi.getApprovalHistory')
        const response = await approvalApi.getApprovalHistory(this.businessType, this.businessId)
        console.log('API响应:', response)
        this.approvalHistory = response.data || []
        console.log('审批历史数据:', this.approvalHistory)
      } catch (error) {
        console.error('加载审批历史失败:', error)
        this.$message.error('加载审批历史失败，请稍后重试')
      }
    },

    async loadBusinessData() {
      try {
        let response

        if (this.businessType === 'INBOUND') {
          response = await inboundApi.getInboundById(this.businessId)
        } else if (this.businessType === 'OUTBOUND') {
          response = await outboundApi.getOutboundById(this.businessId)
        } else if (this.businessType === 'STOCK_ADJUSTMENT') {
          response = await stockAdjustmentApi.getAdjustment({ id: this.businessId })
        }

        const data = response.data

        // 设置业务数据
        this.businessData = {
          businessNo: data.inboundNo || data.outboundNo || data.adjustmentNo || '未知',
          businessType: this.getBusinessTypeText(this.businessType),
          submitTime: data.submitTime,
          submitter: data.operator ? data.operator.realName : '未知',
          approvalStatus: data.approvalStatus || 'DRAFT'
        }
      } catch (error) {
        console.error('加载业务数据失败:', error)
        this.$message.error('加载业务数据失败')
      }
    },

    getBusinessTypeText(type) {
      const typeMap = {
        'INBOUND': '入库',
        'OUTBOUND': '出库',
        'STOCK_ADJUSTMENT': '库存调整'
      }
      return typeMap[type] || type
    },

    getApprovalStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已批准',
        'REJECTED': '已拒绝',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },

    getApprovalStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'CANCELLED': 'info'
      }
      return typeMap[status] || 'info'
    },

    formatDateTime(dateTime) {
      if (!dateTime) return ''
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    handleClose() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.business-info {
  margin-bottom: 20px;
}

.approval-history {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}

.el-descriptions {
  margin-bottom: 20px;
}

.el-table {
  margin-bottom: 20px;
}
</style>