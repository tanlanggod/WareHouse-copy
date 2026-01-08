<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="header-content">
        <div class="header-left">
          <el-button type="text" icon="el-icon-arrow-left" @click="handleBack">
            返回客户列表
          </el-button>
          <span class="page-title">{{ isEdit ? '编辑客户' : '新增客户' }}</span>
        </div>
      </div>

      <div class="form-container">
        <el-form
          ref="customerForm"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          size="medium"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="客户名称" prop="name">
                <el-input
                  v-model="formData.name"
                  placeholder="请输入客户名称"
                  maxlength="100"
                  show-word-limit
                  clearable
                >
                  <i slot="prefix" class="el-icon-office-building"></i>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系人" prop="contactPerson">
                <el-input
                  v-model="formData.contactPerson"
                  placeholder="请输入联系人姓名"
                  maxlength="50"
                  clearable
                >
                  <i slot="prefix" class="el-icon-user"></i>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input
                  v-model="formData.phone"
                  placeholder="请输入联系电话"
                  maxlength="20"
                  clearable
                >
                  <i slot="prefix" class="el-icon-phone"></i>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱地址" prop="email">
                <el-input
                  v-model="formData.email"
                  placeholder="请输入邮箱地址"
                  maxlength="100"
                  clearable
                >
                  <i slot="prefix" class="el-icon-message"></i>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="客户地址" prop="address">
            <el-input
              v-model="formData.address"
              type="textarea"
              :rows="3"
              placeholder="请输入详细地址"
              maxlength="255"
              show-word-limit
            >
            </el-input>
          </el-form-item>

          <el-form-item label="客户状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="1">
                <i class="el-icon-success" style="color: #67c23a;"></i>
                启用
              </el-radio>
              <el-radio :label="0">
                <i class="el-icon-error" style="color: #f56c6c;"></i>
                禁用
              </el-radio>
            </el-radio-group>
            <div class="form-tip">
              <i class="el-icon-info"></i>
              启用状态下的客户可以正常用于出库操作，禁用状态下无法选择该客户
            </div>
          </el-form-item>

          <!-- 分割线 -->
          <el-divider content-position="left">其他信息</el-divider>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="创建时间">
                <el-input
                  :value="formData.createdAt ? formatDateTime(formData.createdAt) : '新增时自动生成'"
                  disabled
                >
                  <i slot="prefix" class="el-icon-time"></i>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最后更新">
                <el-input
                  :value="formData.updatedAt ? formatDateTime(formData.updatedAt) : '保存后自动更新'"
                  disabled
                >
                  <i slot="prefix" class="el-icon-refresh"></i>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <!-- 表单操作按钮 -->
        <div class="form-actions">
          <el-button size="large" @click="handleBack">
            <i class="el-icon-close"></i>
            取消
          </el-button>
          <el-button
            type="primary"
            size="large"
            @click="handleSubmit"
            :loading="submitting"
          >
            <i class="el-icon-check"></i>
            {{ isEdit ? '更新客户' : '创建客户' }}
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { customerApi } from '@/api'
import { formatDateTime } from '@/utils/date'

export default {
  name: 'CustomerForm',
  data() {
    return {
      submitting: false,
      formData: {
        id: null,
        name: '',
        contactPerson: '',
        phone: '',
        email: '',
        address: '',
        status: 1,
        createdAt: null,
        updatedAt: null
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
        ],
        address: [
          { max: 255, message: '地址长度不能超过255个字符', trigger: 'blur' }
        ]
      }
    }
  },

  computed: {
    isEdit() {
      return !!this.formData.id
    }
  },

  async created() {
    await this.initFormData()
  },

  methods: {
    formatDateTime,

    async initFormData() {
      const customerId = this.$route.params.id

      if (customerId && customerId !== 'add') {
        try {
          const response = await customerApi.getAllCustomers()
          if (response.code === 200) {
            const customer = response.data.find(c => c.id === parseInt(customerId))
            if (customer) {
              this.formData = { ...customer }
            } else {
              this.$message.error('客户不存在')
              this.handleBack()
            }
          } else {
            this.$message.error('获取客户信息失败')
            this.handleBack()
          }
        } catch (error) {
          console.error('获取客户信息失败:', error)
          this.$message.error('获取客户信息失败')
          this.handleBack()
        }
      }
    },

    async handleSubmit() {
      try {
        await this.$refs.customerForm.validate()
        this.submitting = true

        if (this.isEdit) {
          await customerApi.updateCustomer(this.formData.id, this.formData)
          this.$message.success('客户信息更新成功')
        } else {
          await customerApi.createCustomer(this.formData)
          this.$message.success('客户创建成功')
        }

        // 延迟返回列表，让用户看到成功提示
        setTimeout(() => {
          this.handleBack()
        }, 1000)
      } catch (error) {
        if (error.message) {
          this.$message.error(error.message)
        }
      } finally {
        this.submitting = false
      }
    },

    handleBack() {
      this.$router.push('/customers')
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
}

/* 表单样式优化 */
.el-form-item {
  margin-bottom: 24px;
}

.el-form-item__label {
  font-weight: 500;
  color: #606266;
  padding-right: 12px;
}

.el-input__inner,
.el-textarea__inner {
  border-radius: 6px;
  border-color: #dcdfe6;
  transition: all 0.3s ease;
}

.el-input__inner:focus,
.el-textarea__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.el-input__prefix {
  color: #909399;
}

.el-divider {
  margin: 32px 0 24px 0;
}

.el-divider__text {
  background-color: #f5f7fa;
  color: #909399;
  font-weight: 500;
  padding: 8px 16px;
}

/* 单选框组样式 */
.el-radio {
  margin-right: 24px;
}

.el-radio__label {
  padding-left: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 提示信息样式 */
.form-tip {
  margin-top: 8px;
  padding: 12px 16px;
  background-color: #f0f9ff;
  border: 1px solid #e1f5fe;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.form-tip i {
  color: #409eff;
  margin-top: 2px;
  flex-shrink: 0;
}

/* 表单操作按钮 */
.form-actions {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
  text-align: center;
}

.form-actions .el-button {
  min-width: 120px;
  margin: 0 12px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.form-actions .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.form-actions .el-button--primary:hover {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 输入框图标样式 */
.el-input__prefix i {
  font-size: 16px;
}

/* 禁用输入框样式 */
.el-input.is-disabled .el-input__inner {
  background-color: #f5f7fa;
  color: #909399;
  border-color: #e4e7ed;
}

/* 加载状态 */
.el-button.is-loading {
  position: relative;
}

.el-button.is-loading::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: inherit;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-container {
    padding: 16px 0;
    max-width: 100%;
  }

  .el-row {
    margin: 0 !important;
  }

  .el-col {
    padding: 0 !important;
    margin-bottom: 0;
  }

  .form-actions {
    margin-top: 24px;
    padding-top: 20px;
  }

  .form-actions .el-button {
    width: calc(50% - 12px);
    margin: 0 6px 12px 6px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .form-actions .el-button {
    width: 100%;
    margin: 0 0 12px 0;
  }

  .form-actions .el-button:last-child {
    margin-bottom: 0;
  }

  .el-form-item__label {
    text-align: left;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-container {
  animation: fadeInUp 0.4s ease-out;
}

/* 表单项悬停效果 */
.el-form-item:hover .el-input__inner:not(:focus),
.el-form-item:hover .el-textarea__inner:not(:focus) {
  border-color: #c0c4cc;
}

/* 验证错误状态 */
.el-form-item.is-error .el-input__inner,
.el-form-item.is-error .el-textarea__inner {
  border-color: #f56c6c;
  box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.1);
}

.el-form-item.is-error .el-form-item__error {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
}
</style>