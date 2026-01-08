<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decorations">
      <div class="decoration decoration-1"></div>
      <div class="decoration decoration-2"></div>
      <div class="decoration decoration-3"></div>
    </div>

    <!-- 主体内容 -->
    <div class="login-container">
      <!-- 左侧信息面板 -->
      <div class="info-panel">
        <div class="info-content">
          <div class="logo-section">
            <div class="logo-icon">
              <i class="el-icon-box"></i>
            </div>
            <h1 class="brand-title">智能仓库管理系统</h1>
            <p class="brand-subtitle">现代化仓储解决方案，让管理更高效</p>
          </div>

          <div class="features-section">
            <div class="feature-item">
              <div class="feature-icon">
                <i class="el-icon-data-analysis"></i>
              </div>
              <div class="feature-text">
                <h3>实时数据监控</h3>
                <p>全方位追踪库存动态</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <i class="el-icon-setting"></i>
              </div>
              <div class="feature-text">
                <h3>智能操作管理</h3>
                <p>简化出入库流程</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <i class="el-icon-pie-chart"></i>
              </div>
              <div class="feature-text">
                <h3>数据分析报表</h3>
                <p>深度洞察业务趋势</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-panel">
        <div class="login-card">
          <!-- 登录方式切换 -->
          <div class="login-tabs">
            <div
              class="tab-item"
              :class="{ active: activeTab === 'username' }"
              @click="switchTab('username')">
              <i class="el-icon-user"></i>
              <span>账号登录</span>
            </div>
            <div
              class="tab-item"
              :class="{ active: activeTab === 'phone' }"
              @click="switchTab('phone')">
              <i class="el-icon-mobile-phone"></i>
              <span>手机登录</span>
            </div>
          </div>

          <!-- 账号密码登录 -->
          <div v-if="activeTab === 'username'" class="login-form">
            <div class="form-header">
              <h2>欢迎回来</h2>
              <p>请输入您的账号信息登录系统</p>
            </div>

            <form @submit.prevent="handleLogin">
              <div class="form-group">
                <div class="input-wrapper">
                  <div class="input-icon">
                    <i class="el-icon-user"></i>
                  </div>
                  <input
                    v-model="loginForm.username"
                    type="text"
                    class="form-input"
                    placeholder="用户名"
                    @keyup.enter="handleLogin"
                  >
                </div>
              </div>

              <div class="form-group">
                <div class="input-wrapper">
                  <div class="input-icon">
                    <i class="el-icon-lock"></i>
                  </div>
                  <input
                    v-model="loginForm.password"
                    :type="showPassword ? 'text' : 'password'"
                    class="form-input"
                    placeholder="密码"
                    @keyup.enter="handleLogin"
                  >
                  <button
                    type="button"
                    class="password-toggle"
                    @click="showPassword = !showPassword">
                    <i :class="showPassword ? 'el-icon-view' : 'el-icon-hide'"></i>
                  </button>
                </div>
              </div>

              <div class="form-group">
                <div class="captcha-wrapper">
                  <div class="input-wrapper">
                    <div class="input-icon">
                      <i class="el-icon-picture-outline"></i>
                    </div>
                    <input
                      v-model="loginForm.captchaCode"
                      type="text"
                      class="form-input"
                      placeholder="验证码"
                      @keyup.enter="handleLogin"
                    >
                  </div>
                  <div class="captcha-image" @click="refreshCaptcha">
                    <img v-if="captchaImage" :src="captchaImage" alt="验证码">
                    <div v-else class="captcha-placeholder">
                      <i class="el-icon-refresh"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="form-options">
                <label class="checkbox-wrapper">
                  <input v-model="rememberMe" type="checkbox">
                  <span class="checkmark"></span>
                  <span class="checkbox-label">记住我</span>
                </label>
                <a href="#" class="forgot-password">忘记密码？</a>
              </div>

              <button
                type="submit"
                class="login-btn"
                :disabled="loading">
                <span v-if="loading" class="btn-spinner"></span>
                <span v-else>立即登录</span>
              </button>
            </form>

            <!-- 示例账号信息 -->
            <div class="demo-accounts">
              <div class="demo-title">
                <i class="el-icon-info"></i>
                <span>演示账号</span>
              </div>
              <div class="demo-list">
                <div class="demo-item" @click="fillDemoAccount('admin')">
                  <span class="demo-role">管理员</span>
                  <span class="demo-account">admin / admin123</span>
                </div>
                <div class="demo-item" @click="fillDemoAccount('keeper')">
                  <span class="demo-role">库管员</span>
                  <span class="demo-account">keeper / admin123</span>
                </div>
                <div class="demo-item" @click="fillDemoAccount('employee')">
                  <span class="demo-role">员工</span>
                  <span class="demo-account">employee / admin123</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 手机号登录 -->
          <div v-else class="login-form">
            <div class="form-header">
              <h2>手机验证登录</h2>
              <p>请输入您的手机号获取验证码</p>
            </div>

            <form @submit.prevent="handlePhoneLogin">
              <div class="form-group">
                <div class="input-wrapper">
                  <div class="input-icon">
                    <i class="el-icon-mobile-phone"></i>
                  </div>
                  <input
                    v-model="phoneForm.phone"
                    type="tel"
                    class="form-input"
                    placeholder="手机号"
                    maxlength="11"
                  >
                </div>
              </div>

              <div class="form-group">
                <div class="input-wrapper">
                  <div class="input-icon">
                    <i class="el-icon-message"></i>
                  </div>
                  <input
                    v-model="phoneForm.verificationCode"
                    type="text"
                    class="form-input"
                    placeholder="短信验证码"
                    maxlength="6"
                  >
                  <button
                    type="button"
                    class="code-btn"
                    :disabled="codeCountdown > 0 || !phoneForm.phone"
                    @click="sendVerificationCode">
                    {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
                  </button>
                </div>
              </div>

              <div class="form-group">
                <div class="captcha-wrapper">
                  <div class="input-wrapper">
                    <div class="input-icon">
                      <i class="el-icon-picture-outline"></i>
                    </div>
                    <input
                      v-model="phoneForm.captchaCode"
                      type="text"
                      class="form-input"
                      placeholder="验证码"
                      maxlength="4"
                      @keyup.enter="handlePhoneLogin"
                    >
                  </div>
                  <div class="captcha-image" @click="refreshCaptcha">
                    <img v-if="captchaImage" :src="captchaImage" alt="验证码">
                    <div v-else class="captcha-placeholder">
                      <i class="el-icon-refresh"></i>
                    </div>
                  </div>
                </div>
              </div>

              <button
                type="submit"
                class="login-btn"
                :disabled="loading">
                <span v-if="loading" class="btn-spinner"></span>
                <span v-else>登录</span>
              </button>
            </form>

            <div class="phone-tips">
              <i class="el-icon-warning-outline"></i>
              <span>测试环境验证码：123456</span>
            </div>
          </div>

          <!-- 注册链接 -->
          <div class="register-link">
            <span>还没有账号？</span>
            <router-link to="/register" class="link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import { authApi, userContextApi } from '@/api'

export default {
  name: 'ModernLogin',
  data() {
    return {
      activeTab: 'username',
      loading: false,
      sendingCode: false,
      codeCountdown: 0,
      codeTimer: null,
      showPassword: false,
      rememberMe: false,

      // 验证码相关
      captchaImage: '',
      captchaId: '',

      // 账号登录表单
      loginForm: {
        username: '',
        password: '',
        captchaCode: ''
      },

      // 手机登录表单
      phoneForm: {
        phone: '',
        verificationCode: '',
        captchaCode: ''
      }
    }
  },
  created() {
    this.loadRememberedCredentials()
    this.refreshCaptcha()
  },
  beforeDestroy() {
    if (this.codeTimer) {
      clearInterval(this.codeTimer)
    }
  },
  methods: {
    ...mapActions('user', ['login', 'setLoginState']),

    switchTab(tab) {
      this.activeTab = tab
    },

    loadRememberedCredentials() {
      const stored = localStorage.getItem('rememberedCredentials')
      if (stored) {
        try {
          const parsed = JSON.parse(stored)
          this.loginForm.username = parsed.username || ''
          this.loginForm.password = parsed.password ? atob(parsed.password) : ''
          if (this.loginForm.username || this.loginForm.password) {
            this.rememberMe = true
          }
        } catch (error) {
          console.warn('解析记住的登录信息失败', error)
          localStorage.removeItem('rememberedCredentials')
        }
      }
    },

    saveRememberedCredentials() {
      if (this.rememberMe) {
        const payload = {
          username: this.loginForm.username || '',
          password: btoa(this.loginForm.password || '')
        }
        localStorage.setItem('rememberedCredentials', JSON.stringify(payload))
      } else {
        localStorage.removeItem('rememberedCredentials')
      }
    },

    refreshCaptcha() {
      authApi.getCaptcha().then(response => {
        this.captchaImage = response.data.captchaImage
        this.captchaId = response.data.captchaId
        this.loginForm.captchaCode = ''
      }).catch(error => {
        console.error('获取验证码失败:', error)
        this.$message.error('获取验证码失败，请稍后重试')
      })
    },

    fillDemoAccount(role) {
      const accounts = {
        admin: { username: 'admin', password: 'admin123' },
        keeper: { username: 'keeper', password: 'admin123' },
        employee: { username: 'employee', password: 'admin123' }
      }

      const account = accounts[role]
      if (account) {
        this.loginForm.username = account.username
        this.loginForm.password = account.password
        this.$message.success(`已填充${role === 'admin' ? '管理员' : role === 'keeper' ? '库管员' : '员工'}账号`)
      }
    },

    handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password || !this.loginForm.captchaCode) {
        this.$message.warning('请填写完整的登录信息')
        return
      }

      this.loading = true
      const loginData = {
        ...this.loginForm,
        captchaId: this.captchaId
      }

      this.login(loginData).then(response => {
      this.saveRememberedCredentials()
      this.$message.success('登录成功')
      this.$router.push('/app/dashboard')
    }).catch(error => {
        this.$message.error(error.message || '登录失败')
        this.refreshCaptcha()
      }).finally(() => {
        this.loading = false
      })
    },

    sendVerificationCode() {
      if (!this.phoneForm.phone || !/^1[3-9]\d{9}$/.test(this.phoneForm.phone)) {
        this.$message.warning('请输入正确的手机号')
        return
      }

      if (!this.phoneForm.captchaCode) {
        this.$message.warning('请输入图片验证码')
        return
      }

      this.sendingCode = true
      authApi.sendVerificationCode({
        phone: this.phoneForm.phone,
        captchaId: this.captchaId,
        captchaCode: this.phoneForm.captchaCode
      })
        .then(response => {
          this.$message.success('验证码发送成功')
          this.startCountdown()
        })
        .catch(error => {
          this.$message.error(error.message || '验证码发送失败')
          this.refreshCaptcha()  // 刷新验证码
        })
        .finally(() => {
          this.sendingCode = false
        })
    },

    startCountdown() {
      this.codeCountdown = 60
      this.codeTimer = setInterval(() => {
        if (this.codeCountdown > 0) {
          this.codeCountdown--
        } else {
          clearInterval(this.codeTimer)
          this.codeTimer = null
        }
      }, 1000)
    },

    handlePhoneLogin() {
      if (!this.phoneForm.phone || !this.phoneForm.verificationCode || !this.phoneForm.captchaCode) {
        this.$message.warning('请填写完整的登录信息')
        return
      }

      this.loading = true
      const loginData = {
        ...this.phoneForm,
        captchaId: this.captchaId  // 确保传递验证码ID
      }
      authApi.phoneLogin(loginData)
        .then(response => {
          this.setLoginState(response.data).then(() => {
            this.$message.success('登录成功')
            this.$router.push('/app/dashboard')
          })
        })
        .catch(error => {
          this.$message.error(error.message || '登录失败')
          this.refreshCaptcha()  // 刷新验证码
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped>
@import '@/styles/design-system.css';
@import '@/styles/components.css';

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-bg);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.decoration {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.decoration-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.decoration-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  right: -100px;
  animation-delay: 2s;
}

.decoration-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 主体容器 */
.login-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 1200px;
  width: 100%;
  margin: 0 var(--spacing-lg);
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-2xl);
  overflow: hidden;
  z-index: 2;
  min-height: 700px;
}

/* 左侧信息面板 */
.info-panel {
  background: var(--gradient-primary);
  color: var(--text-inverse);
  padding: var(--spacing-3xl);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.info-content {
  max-width: 400px;
}

.logo-section {
  margin-bottom: var(--spacing-3xl);
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin-bottom: var(--spacing-lg);
}

.brand-title {
  font-size: var(--text-3xl);
  font-weight: 700;
  margin-bottom: var(--spacing-sm);
  line-height: var(--leading-tight);
}

.brand-subtitle {
  font-size: var(--text-lg);
  opacity: 0.9;
  line-height: var(--leading-relaxed);
}

.features-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
}

.feature-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.feature-text h3 {
  font-size: var(--text-lg);
  font-weight: 600;
  margin-bottom: var(--spacing-xs);
}

.feature-text p {
  font-size: var(--text-sm);
  opacity: 0.8;
}

/* 右侧登录面板 */
.login-panel {
  padding: var(--spacing-3xl);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

/* 登录方式切换 */
.login-tabs {
  display: flex;
  margin-bottom: var(--spacing-2xl);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xs);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
}

.tab-item.active {
  background: var(--bg-primary);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.tab-item:hover:not(.active) {
  color: var(--text-primary);
}

/* 表单头部 */
.form-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.form-header h2 {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.form-header p {
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

/* 表单样式 */
.login-form {
  margin-bottom: var(--spacing-xl);
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: var(--spacing-md);
  color: var(--text-muted);
  z-index: 2;
  font-size: 1rem;
}

.form-input {
  width: 100%;
  padding: var(--spacing-md) var(--spacing-md) var(--spacing-md) 2.5rem;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.password-toggle {
  position: absolute;
  right: var(--spacing-md);
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  z-index: 2;
  font-size: 1rem;
}

.password-toggle:hover {
  color: var(--primary-color);
}

/* 验证码样式 */
.captcha-wrapper {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}

.captcha-wrapper .input-wrapper {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 48px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.captcha-image:hover {
  border-color: var(--primary-color);
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  color: var(--text-muted);
}

/* 验证码按钮 */
.code-btn {
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--primary-light);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.code-btn:hover:not(:disabled) {
  background: var(--primary-color);
}

.code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-wrapper input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-sm);
  margin-right: var(--spacing-sm);
  position: relative;
  transition: all var(--transition-fast);
}

.checkbox-wrapper input[type="checkbox"]:checked + .checkmark {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.checkbox-wrapper input[type="checkbox"]:checked + .checkmark::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 5px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-label {
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

.forgot-password {
  color: var(--primary-color);
  text-decoration: none;
  font-size: var(--text-sm);
}

.forgot-password:hover {
  text-decoration: underline;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  padding: var(--spacing-md);
  background: var(--gradient-primary);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 演示账号 */
.demo-accounts {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  margin-top: var(--spacing-lg);
}

.demo-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--text-secondary);
  font-size: var(--text-sm);
  margin-bottom: var(--spacing-sm);
}

.demo-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.demo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm);
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.demo-item:hover {
  background: var(--bg-tertiary);
  transform: translateX(4px);
}

.demo-role {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--text-sm);
}

.demo-account {
  color: var(--text-muted);
  font-size: var(--text-xs);
  font-family: var(--font-mono);
}

/* 手机登录提示 */
.phone-tips {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  margin-top: var(--spacing-lg);
}

/* 注册链接 */
.register-link {
  text-align: center;
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

.register-link .link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
  margin-left: var(--spacing-xs);
}

.register-link .link:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    grid-template-columns: 1fr;
    margin: var(--spacing-md);
    min-height: auto;
  }

  .info-panel {
    display: none;
  }

  .login-panel {
    padding: var(--spacing-xl);
  }

  .form-header h2 {
    font-size: var(--text-xl);
  }

  .brand-title {
    font-size: var(--text-2xl);
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: var(--spacing-md);
  }

  .login-container {
    margin: 0;
    border-radius: var(--radius-lg);
  }

  .login-panel {
    padding: var(--spacing-lg);
  }

  .captcha-wrapper {
    flex-direction: column;
    align-items: stretch;
  }

  .captcha-image {
    width: 100%;
  }
}
</style>