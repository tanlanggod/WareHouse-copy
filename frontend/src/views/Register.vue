<template>
  <div class="register-container">
    <div class="register-box">
      <h2>商品出入库管理系统</h2>
      <h3>用户注册</h3>

      <el-form :model="registerForm" :rules="rules" ref="registerForm" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            prefix-icon="el-icon-user"
            placeholder="用户名（4-20位字母数字下划线）"
            @keyup.enter.native="handleRegister">
          </el-input>
        </el-form-item>

        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            prefix-icon="el-icon-user-solid"
            placeholder="真实姓名"
            @keyup.enter.native="handleRegister">
          </el-input>
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            prefix-icon="el-icon-message"
            placeholder="邮箱地址（可选）"
            @keyup.enter.native="handleRegister">
          </el-input>
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            prefix-icon="el-icon-phone"
            placeholder="手机号（可选）"
            @keyup.enter.native="handleRegister">
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            prefix-icon="el-icon-lock"
            placeholder="密码（至少6位，包含字母和数字）"
            show-password
            @keyup.enter.native="handleRegister">
          </el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            prefix-icon="el-icon-lock"
            placeholder="确认密码"
            show-password
            @keyup.enter.native="handleRegister">
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="register-btn"
            @click="handleRegister">
            注册
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="link">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { authApi } from '@/api'

export default {
  name: 'Register',
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
      if (!value) {
        callback(new Error('请输入密码'))
      } else if (value.length < 6 || value.length > 20) {
        callback(new Error('密码长度必须在6-20位之间'))
      } else if (!/^(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
        callback(new Error('密码必须包含至少一个字母和一个数字'))
      } else {
        callback()
      }
    }

    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请确认密码'))
      } else if (value !== this.registerForm.password) {
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
      registerForm: {
        username: '',
        realName: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: ''
      },
      loading: false,
      rules: {
        username: [
          { validator: validateUsername, trigger: 'blur' }
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
        password: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async handleRegister() {
      try {
        await this.$refs.registerForm.validate()
        this.loading = true

        const response = await authApi.register(this.registerForm)

        if (response.code === 200) {
          this.$message.success('注册成功！请登录')
          // 注册成功后跳转到登录页面
          this.$router.push('/login')
        } else {
          this.$message.error(response.message || '注册失败')
        }
      } catch (error) {
        console.error('注册失败:', error)
        if (error !== false) { // 表单验证失败时error为false
          this.$message.error(error.message || '注册失败，请重试')
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 10px;
  font-size: 24px;
}

h3 {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
  font-size: 18px;
  font-weight: normal;
}

.register-form {
  margin-top: 20px;
}

.register-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  border-radius: 5px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.link {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}

.link:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-box {
    width: 90%;
    margin: 20px;
    padding: 30px 20px;
  }
}
</style>