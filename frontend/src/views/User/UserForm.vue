<template>
  <el-dialog
    :title="isEdit ? '编辑用户' : '新增用户'"
    :visible.sync="dialogVisible"
    width="600px"
    :before-close="handleClose"
    @close="handleClose">

    <el-form
      ref="userForm"
      :model="formData"
      :rules="rules"
      label-width="80px"
      class="user-form">

      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="formData.username"
          placeholder="请输入用户名（4-20位字母数字下划线）"
          :disabled="isEdit">
        </el-input>
      </el-form-item>

      <el-form-item label="密码" prop="password" v-if="!isEdit">
        <el-input
          v-model="formData.password"
          type="password"
          placeholder="请输入密码（至少6位）"
          show-password>
        </el-input>
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEdit">
        <el-input
          v-model="formData.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          show-password>
        </el-input>
      </el-form-item>

      <el-form-item label="真实姓名" prop="realName">
        <el-input
          v-model="formData.realName"
          placeholder="请输入真实姓名">
        </el-input>
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="formData.email"
          placeholder="请输入邮箱地址">
        </el-input>
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="formData.phone"
          placeholder="请输入手机号">
        </el-input>
      </el-form-item>

      <el-form-item label="角色" prop="role">
        <el-select
          v-model="formData.role"
          placeholder="请选择角色"
          style="width: 100%">
          <el-option label="管理员" value="ADMIN"></el-option>
          <el-option label="库管员" value="WAREHOUSE_KEEPER"></el-option>
          <el-option label="普通员工" value="EMPLOYEE"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { userAdminApi } from '@/api'

export default {
  name: 'UserForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userData: {
      type: Object,
      default: () => ({})
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  data() {
    // 自定义验证规则
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入用户名'))
      } else if (!/^[a-zA-Z0-9_]{4,20}$/.test(value)) {
        callback(new Error('用户名长度必须在4-20位之间，只能包含字母、数字和下划线'))
      } else {
        callback()
      }
    }

    const validatePassword = (rule, value, callback) => {
      if (!this.isEdit && !value) {
        callback(new Error('请输入密码'))
      } else if (value && (value.length < 6 || value.length > 20)) {
        callback(new Error('密码长度必须在6-20位之间'))
      } else if (value && !/^(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
        callback(new Error('密码必须包含至少一个字母和一个数字'))
      } else {
        callback()
      }
    }

    const validateConfirmPassword = (rule, value, callback) => {
      if (!this.isEdit && !value) {
        callback(new Error('请确认密码'))
      } else if (value && value !== this.formData.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    const validateEmail = (rule, value, callback) => {
      if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error('邮箱格式不正确'))
      } else {
        callback()
      }
    }

    const validatePhone = (rule, value, callback) => {
      if (value && !/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('手机号格式不正确'))
      } else {
        callback()
      }
    }

    return {
      dialogVisible: false,
      loading: false,
      formData: {
        username: '',
        password: '',
        confirmPassword: '',
        realName: '',
        email: '',
        phone: '',
        role: 'EMPLOYEE',
        status: 1
      },
      rules: {
        username: [
          { validator: validateUsername, trigger: 'blur' }
        ],
        password: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' },
          { max: 50, message: '真实姓名长度不能超过50位', trigger: 'blur' }
        ],
        email: [
          { validator: validateEmail, trigger: 'blur' }
        ],
        phone: [
          { validator: validatePhone, trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      }
    }
  },
  watch: {
    visible: {
      handler(val) {
        this.dialogVisible = val
        if (val) {
          this.initFormData()
        }
      },
      immediate: true
    },
    dialogVisible(val) {
      this.$emit('update:visible', val)
    }
  },
  methods: {
    // 初始化表单数据
    initFormData() {
      if (this.isEdit && this.userData) {
        this.formData = {
          id: this.userData.id,
          username: this.userData.username,
          password: '', // 编辑时不显示密码
          confirmPassword: '',
          realName: this.userData.realName,
          email: this.userData.email,
          phone: this.userData.phone,
          role: this.userData.role,
          status: this.userData.status
        }
      } else {
        this.formData = {
          username: '',
          password: '',
          confirmPassword: '',
          realName: '',
          email: '',
          phone: '',
          role: 'EMPLOYEE',
          status: 1
        }
      }
    },

    // 提交表单
    async handleSubmit() {
      try {
        await this.$refs.userForm.validate()
        this.loading = true

        let response
        if (this.isEdit) {
          // 编辑用户
          const updateData = {
            realName: this.formData.realName,
            email: this.formData.email,
            phone: this.formData.phone,
            role: this.formData.role,
            status: this.formData.status
          }
          response = await userAdminApi.updateUser(this.formData.id, updateData)
        } else {
          // 创建用户
          response = await userAdminApi.createUser(this.formData)
        }

        if (response.code === 200) {
          this.$message.success(this.isEdit ? '用户更新成功' : '用户创建成功')
          this.$emit('success')
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        if (error !== false) { // 表单验证失败时error为false
          this.$message.error('操作失败，请重试')
        }
      } finally {
        this.loading = false
      }
    },

    // 关闭弹窗
    handleClose() {
      this.$refs.userForm?.clearValidate()
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped>
.user-form {
  padding: 0 20px;
}

.dialog-footer {
  text-align: right;
}

/* 表单项间距调整 */
.el-form-item {
  margin-bottom: 22px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .el-dialog {
    width: 90% !important;
    margin: 0 auto;
  }
}
</style>