<template>
  <div class="approval-history-view">
    <ApprovalHistory
      :visible="dialogVisible"
      :business-id="businessId"
      :business-type="'INBOUND'"
      @close="handleClose"
    />
  </div>
</template>

<script>
import ApprovalHistory from '@/components/Approval/ApprovalHistory.vue'

export default {
  name: 'InboundApprovalHistoryView',
  components: {
    ApprovalHistory
  },
  data() {
    return {
      dialogVisible: true
    }
  },
  computed: {
    businessId() {
      return this.$route.params.id ? parseInt(this.$route.params.id) : null
    }
  },
  mounted() {
    // 确保businessId有效
    if (!this.businessId) {
      this.$message.error('无效的入库单ID')
      this.$router.push('/app/inbounds')
    }
  },
  methods: {
    handleClose() {
      // 获取返回参数
      const queryParams = this.$route.query
      const baseUrl = '/app/inbounds'

      if (queryParams.page || queryParams.size || queryParams.startDate || queryParams.endDate) {
        const params = new URLSearchParams()
        if (queryParams.page) params.append('page', queryParams.page)
        if (queryParams.size) params.append('size', queryParams.size)
        if (queryParams.startDate) params.append('startDate', queryParams.startDate)
        if (queryParams.endDate) params.append('endDate', queryParams.endDate)

        // 跳转回原页面并保持状态
        this.$router.push(`${baseUrl}?${params.toString()}`)
      } else {
        this.$router.push(baseUrl)
      }
    }
  }
}
</script>

<style scoped>
.approval-history-view {
  padding: 20px;
  min-height: calc(100vh - 120px);
}
</style>