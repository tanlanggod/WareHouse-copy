<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="visible"
    width="500px"
    :before-close="handleClose"
    @close="handleClose">
    <el-form
      ref="approvalForm"
      :model="approvalForm"
      :rules="rules"
      label-width="100px">

      <!-- 业务信息 -->
      <el-form-item label="业务单号">
        <el-input
          v-model="approvalForm.businessNo"
          readonly
          style="width: 100%">
        </el-input>
      </el-form-item>

      <el-form-item label="业务类型">
        <el-input
          v-model="approvalForm.businessType"
          readonly
          style="width: 100%">
        </el-input>
      </el-form-item>

      <el-form-item label="提交人">
        <el-input
          v-model="approvalForm.submitter"
          readonly
          style="width: 100%">
        </el-input>
      </el-form-item>

      <el-form-item label="提交时间">
        <el-input
          v-model="approvalForm.submitTime"
          readonly
          style="width: 100%">
        </el-input>
      </el-form-item>

      <!-- 审批信息 -->
      <el-divider content-position="left">审批信息</el-divider>

      <el-form-item
        v-if="action === 'approve'"
        label="审批意见"
        prop="remark"
        required>
        <el-input
          v-model="approvalForm.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入审批意见（可选）"
          maxlength="500"
          show-word-limit>
        </el-input>
      </el-form-item>

      <el-form-item
        v-if="action === 'reject'"
        label="拒绝原因"
        prop="reason"
        required>
        <el-input
          v-model="approvalForm.reason"
          type="textarea"
          :rows="4"
          placeholder="请输入拒绝原因（必填）"
          maxlength="500"
          show-word-limit>
        </el-input>
      </el-form-item>

      <el-form-item
        v-if="action === 'cancel'"
        label="取消原因"
        prop="reason"
        required>
        <el-input
          v-model="approvalForm.reason"
          type="textarea"
          :rows="4"
          placeholder="请输入取消原因（必填）"
          maxlength="500"
          show-word-limit>
        </el-input>
      </el-form-item>

    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button
        v-if="action === 'approve'"
        type="primary"
        :loading="loading"
        @click="handleSubmit">通过</el-button>
      <el-button
        v-if="action === 'reject'"
        type="danger"
        :loading="loading"
        @click="handleSubmit">拒绝</el-button>
      <el-button
        v-if="action === 'cancel'"
        type="warning"
        :loading="loading"
        @click="handleSubmit">取消审批</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'ApprovalDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    action: {
      type: String,
      default: 'approve', // approve, reject, cancel
      validator: value => ['approve', 'reject', 'cancel'].includes(value)
    },
    businessData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      loading: false,
      approvalForm: {
        businessNo: '',
        businessType: '',
        submitter: '',
        submitTime: '',
        remark: '',
        reason: ''
      },
      rules: {
        reason: [
          { required: true, message: '请输入原因', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      const titleMap = {
        'approve': '审批通过',
        'reject': '审批拒绝',
        'cancel': '取消审批'
      }
      return titleMap[this.action] || '审批操作'
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.initForm()
      }
    },
    businessData: {
      handler(newVal) {
        if (this.visible) {
          this.initForm()
        }
      },
      deep: true
    }
  },
  methods: {
    initForm() {
      const { businessNo, businessType, submitter, submitTime } = this.businessData

      this.approvalForm = {
        businessNo,
        businessType: this.getBusinessTypeText(businessType),
        submitter: submitter ? submitter.realName || submitter.username : '-',
        submitTime: this.formatDateTime(submitTime),
        remark: '',
        reason: ''
      }

      // 重置表单验证
      this.$nextTick(() => {
        if (this.$refs.approvalForm) {
          this.$refs.approvalForm.clearValidate()
        }
      })
    },

    getBusinessTypeText(type) {
      const typeMap = {
        'INBOUND': '入库',
        'OUTBOUND': '出库',
        'STOCK_ADJUSTMENT': '库存调整'
      }
      return typeMap[type] || type
    },

    formatDateTime(dateTime) {
      if (!dateTime) return ''
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    handleSubmit() {
      if (this.action === 'approve') {
        this.handleSubmitApprove()
      } else if (this.action === 'reject') {
        this.handleSubmitReject()
      } else if (this.action === 'cancel') {
        this.handleSubmitCancel()
      }
    },

    handleSubmitApprove() {
      this.$refs.approvalForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$emit('approve', {
            businessData: this.businessData,
            remark: this.approvalForm.remark
          })
          setTimeout(() => {
            this.loading = false
            this.handleClose()
          }, 1000)
        }
      })
    },

    handleSubmitReject() {
      this.$refs.approvalForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$emit('reject', {
            businessData: this.businessData,
            reason: this.approvalForm.reason
          })
          setTimeout(() => {
            this.loading = false
            this.handleClose()
          }, 1000)
        }
      })
    },

    handleSubmitCancel() {
      this.$refs.approvalForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$emit('cancel', {
            businessData: this.businessData,
            reason: this.approvalForm.reason
          })
          setTimeout(() => {
            this.loading = false
            this.handleClose()
          }, 1000)
        }
      })
    },

    handleClose() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
  margin-top: 20px;
}

.el-divider {
  margin: 20px 0;
}
</style>