<template>
  <el-dialog
    title="账号详情"
    :visible.sync="dialogVisible"
    width="500px"
    :before-close="handleClose"
    class="user-profile-dialog">

    <div class="profile-content">
      <!-- 头像上传区域 -->
      <div class="avatar-section">
        <div class="avatar-container">
          <img v-if="formData.avatar" :src="formData.avatar" alt="头像" class="avatar-img" />
          <div v-else class="avatar-placeholder">
            <i class="el-icon-user-solid"></i>
          </div>

          <el-upload
            class="avatar-uploader"
            action=""
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="uploadAvatar"
            accept="image/*">
            <div class="upload-btn">
              <i class="el-icon-camera"></i>
              更换头像
            </div>
          </el-upload>
        </div>
      </div>

      <!-- 用户信息表单 -->
      <el-form
        ref="profileForm"
        :model="formData"
        :rules="rules"
        label-width="80px"
        class="profile-form">

        <el-form-item label="用户名">
          <el-input v-model="formData.username" disabled></el-input>
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱地址"></el-input>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>

        <el-form-item label="角色">
          <el-input :value="getRoleText(formData.role)" disabled></el-input>
        </el-form-item>
      </el-form>

      <!-- 修改密码区域 -->
      <div class="password-section">
        <el-divider content-position="left">
          <span class="divider-text">修改密码</span>
        </el-divider>

        <el-form
          ref="passwordForm"
          :model="passwordData"
          :rules="passwordRules"
          label-width="80px">

          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordData.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password>
            </el-input>
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordData.newPassword"
              type="password"
              placeholder="请输入新密码 (至少6位)"
              show-password>
            </el-input>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordData.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password>
            </el-input>
          </el-form-item>
        </el-form>

        <el-button
          type="primary"
          size="small"
          @click="changePassword"
          :loading="passwordLoading"
          style="width: 100%;">
          修改密码
        </el-button>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="saveProfile" :loading="saveLoading">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { userApi } from '@/api'

export default {
  name: 'UserProfileDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      dialogVisible: false,
      saveLoading: false,
      passwordLoading: false,
      formData: {
        username: '',
        realName: '',
        email: '',
        phone: '',
        role: '',
        avatar: ''
      },
      passwordData: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
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
    initFormData() {
      this.formData = { ...this.userInfo }
      this.passwordData = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    },

    getRoleText(role) {
      const roleMap = {
        'ADMIN': '管理员',
        'WAREHOUSE_KEEPER': '库管员',
        'EMPLOYEE': '普通员工'
      }
      return roleMap[role] || '未知'
    },

    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过5MB!')
        return false
      }
      return true
    },

    async uploadAvatar(options) {
      try {
        const formData = new FormData()
        formData.append('file', options.file)

        const response = await userApi.uploadAvatar(formData)
        if (response.code === 200) {
          this.formData.avatar = response.data
          this.$message.success('头像上传成功')
        } else {
          this.$message.error(response.message || '头像上传失败')
        }
      } catch (error) {
        console.error('头像上传失败:', error)
        this.$message.error('头像上传失败')
      }
    },

    async saveProfile() {
      try {
        await this.$refs.profileForm.validate()
        this.saveLoading = true

        const response = await userApi.updateCurrentUser(this.formData)
        if (response.code === 200) {
          this.$message.success('用户信息更新成功')
          this.$emit('user-updated', response.data)
          this.handleClose()
        } else {
          this.$message.error(response.message || '更新失败')
        }
      } catch (error) {
        console.error('保存用户信息失败:', error)
        if (error !== false) { // 表单验证失败时error为false
          this.$message.error('保存失败')
        }
      } finally {
        this.saveLoading = false
      }
    },

    async changePassword() {
      try {
        await this.$refs.passwordForm.validate()
        this.passwordLoading = true

        const response = await userApi.changePassword({
          oldPassword: this.passwordData.oldPassword,
          newPassword: this.passwordData.newPassword
        })

        if (response.code === 200) {
          this.$message.success('密码修改成功')
          // 清空密码表单
          this.passwordData = {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
          }
        } else {
          this.$message.error(response.message || '密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        if (error !== false) {
          this.$message.error('修改密码失败')
        }
      } finally {
        this.passwordLoading = false
      }
    },

    validateConfirmPassword(rule, value, callback) {
      if (value !== this.passwordData.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    },

    handleClose() {
      this.$refs.profileForm?.clearValidate()
      this.$refs.passwordForm?.clearValidate()
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped>
.user-profile-dialog ::v-deep .el-dialog {
  border-radius: 8px;
}

.profile-content {
  max-height: 600px;
  overflow-y: auto;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.avatar-container {
  position: relative;
  text-align: center;
}

.avatar-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e6e6e6;
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #e6e6e6;
}

.avatar-placeholder i {
  font-size: 40px;
  color: #c0c4cc;
}

.avatar-uploader {
  margin-top: 10px;
}

.upload-btn {
  display: inline-block;
  padding: 6px 12px;
  background-color: #409eff;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.upload-btn:hover {
  background-color: #66b1ff;
}

.upload-btn i {
  margin-right: 4px;
}

/* 表单区域 */
.profile-form {
  margin-bottom: 20px;
}

/* 密码区域 */
.password-section {
  margin-top: 20px;
}

.divider-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-dialog ::v-deep .el-dialog {
    width: 90% !important;
    margin: 0 auto;
  }

  .avatar-img,
  .avatar-placeholder {
    width: 80px;
    height: 80px;
  }
}
</style>