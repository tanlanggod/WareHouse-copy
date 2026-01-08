<template>
  <div class="modal-overlay" v-if="visible" @click="closeModal">
    <div class="modal settings-modal" @click.stop>
      <div class="modal-header">
        <h3 class="modal-title">系统设置</h3>
        <button class="close-btn" @click="closeModal">
          <i class="el-icon-close"></i>
        </button>
      </div>

      <div class="modal-body">
        <div class="settings-sections">
          <!-- 外观设置 -->
          <div class="settings-section">
            <h4 class="section-title">
              <i class="el-icon-picture"></i>
              外观设置
            </h4>
            <div class="setting-item">
              <div class="setting-label">
                <span>主题模式</span>
                <small>选择您喜欢的界面主题</small>
              </div>
              <div class="setting-control">
                <div class="theme-options">
                  <div
                    class="theme-option"
                    :class="{ active: theme === 'light' }"
                    @click="changeTheme('light')">
                    <div class="theme-preview light">
                      <div class="preview-header"></div>
                      <div class="preview-content"></div>
                    </div>
                    <span>浅色</span>
                  </div>
                  <div
                    class="theme-option"
                    :class="{ active: theme === 'dark' }"
                    @click="changeTheme('dark')">
                    <div class="theme-preview dark">
                      <div class="preview-header"></div>
                      <div class="preview-content"></div>
                    </div>
                    <span>深色</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="setting-item">
              <div class="setting-label">
                <span>侧边栏</span>
                <small>侧边栏显示偏好</small>
              </div>
              <div class="setting-control">
                <label class="switch">
                  <input type="checkbox" v-model="sidebarCollapsed" @change="toggleSidebar">
                  <span class="switch-slider"></span>
                </label>
                <span class="switch-label">自动收起侧边栏</span>
              </div>
            </div>
          </div>

          <!-- 通知设置 -->
          <div class="settings-section">
            <h4 class="section-title">
              <i class="el-icon-bell"></i>
              通知设置
            </h4>
            <div class="setting-item">
              <div class="setting-label">
                <span>桌面通知</span>
                <small>允许系统发送桌面通知</small>
              </div>
              <div class="setting-control">
                <label class="switch">
                  <input type="checkbox" v-model="notifications.desktop" @change="updateNotificationSettings">
                  <span class="switch-slider"></span>
                </label>
              </div>
            </div>

            <div class="setting-item">
              <div class="setting-label">
                <span>声音提醒</span>
                <small>重要事件时播放提示音</small>
              </div>
              <div class="setting-control">
                <label class="switch">
                  <input type="checkbox" v-model="notifications.sound" @change="updateNotificationSettings">
                  <span class="switch-slider"></span>
                </label>
              </div>
            </div>

            <div class="setting-item">
              <div class="setting-label">
                <span>邮件通知</span>
                <small>重要事件通过邮件通知</small>
              </div>
              <div class="setting-control">
                <label class="switch">
                  <input type="checkbox" v-model="notifications.email" @change="updateNotificationSettings">
                  <span class="switch-slider"></span>
                </label>
              </div>
            </div>
          </div>

          <!-- 数据设置 -->
          <div class="settings-section">
            <h4 class="section-title">
              <i class="el-icon-coin"></i>
              数据设置
            </h4>
            <div class="setting-item">
              <div class="setting-label">
                <span>数据刷新频率</span>
                <small>页面数据自动刷新间隔</small>
              </div>
              <div class="setting-control">
                <select v-model="dataRefreshInterval" @change="updateDataSettings">
                  <option value="30">30秒</option>
                  <option value="60">1分钟</option>
                  <option value="300">5分钟</option>
                  <option value="0">手动刷新</option>
                </select>
              </div>
            </div>

            <div class="setting-item">
              <div class="setting-label">
                <span>每页显示数量</span>
                <small>数据表格默认显示行数</small>
              </div>
              <div class="setting-control">
                <select v-model="tablePageSize" @change="updateDataSettings">
                  <option value="10">10行</option>
                  <option value="20">20行</option>
                  <option value="50">50行</option>
                  <option value="100">100行</option>
                </select>
              </div>
            </div>
          </div>

          <!-- 快捷键设置 -->
          <div class="settings-section">
            <h4 class="section-title">
              <i class="el-icon-keyboard"></i>
              快捷键
            </h4>
            <div class="shortcuts-list">
              <div class="shortcut-item">
                <span class="shortcut-desc">全局搜索</span>
                <kbd class="shortcut-key">Ctrl + K</kbd>
              </div>
              <div class="shortcut-item">
                <span class="shortcut-desc">新建</span>
                <kbd class="shortcut-key">Ctrl + N</kbd>
              </div>
              <div class="shortcut-item">
                <span class="shortcut-desc">保存</span>
                <kbd class="shortcut-key">Ctrl + S</kbd>
              </div>
              <div class="shortcut-item">
                <span class="shortcut-desc">侧边栏切换</span>
                <kbd class="shortcut-key">Ctrl + B</kbd>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-secondary" @click="closeModal">取消</button>
        <button class="btn btn-primary" @click="saveSettings">保存设置</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SettingsDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      theme: 'light',
      sidebarCollapsed: false,
      notifications: {
        desktop: true,
        sound: true,
        email: false
      },
      dataRefreshInterval: 60,
      tablePageSize: 20,
      tempSettings: {}
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.loadSettings()
      }
    }
  },
  methods: {
    loadSettings() {
      // 加载保存的设置
      this.theme = localStorage.getItem('theme') || 'light'
      this.sidebarCollapsed = JSON.parse(localStorage.getItem('sidebarCollapsed') || 'false')
      this.notifications = JSON.parse(localStorage.getItem('notifications') || JSON.stringify({
        desktop: true,
        sound: true,
        email: false
      }))
      this.dataRefreshInterval = parseInt(localStorage.getItem('dataRefreshInterval') || '60')
      this.tablePageSize = parseInt(localStorage.getItem('tablePageSize') || '20')

      // 保存临时设置以便取消时恢复
      this.tempSettings = {
        theme: this.theme,
        sidebarCollapsed: this.sidebarCollapsed,
        notifications: { ...this.notifications },
        dataRefreshInterval: this.dataRefreshInterval,
        tablePageSize: this.tablePageSize
      }
    },

    changeTheme(newTheme) {
      this.theme = newTheme
      document.documentElement.setAttribute('data-theme', newTheme)
    },

    toggleSidebar() {
      // 这里可以触发父组件的侧边栏切换方法
      this.$emit('toggle-sidebar', this.sidebarCollapsed)
    },

    updateNotificationSettings() {
      // 通知设置更新
    },

    updateDataSettings() {
      // 数据设置更新
    },

    saveSettings() {
      // 保存所有设置到本地存储
      localStorage.setItem('theme', this.theme)
      localStorage.setItem('sidebarCollapsed', JSON.stringify(this.sidebarCollapsed))
      localStorage.setItem('notifications', JSON.stringify(this.notifications))
      localStorage.setItem('dataRefreshInterval', this.dataRefreshInterval.toString())
      localStorage.setItem('tablePageSize', this.tablePageSize.toString())

      this.$message.success('设置已保存')
      this.$emit('settings-updated', {
        theme: this.theme,
        sidebarCollapsed: this.sidebarCollapsed,
        notifications: this.notifications,
        dataRefreshInterval: this.dataRefreshInterval,
        tablePageSize: this.tablePageSize
      })
      this.closeModal()
    },

    closeModal() {
      // 恢复临时设置
      if (Object.keys(this.tempSettings).length > 0) {
        this.theme = this.tempSettings.theme
        this.sidebarCollapsed = this.tempSettings.sidebarCollapsed
        this.notifications = this.tempSettings.notifications
        this.dataRefreshInterval = this.tempSettings.dataRefreshInterval
        this.tablePageSize = this.tempSettings.tablePageSize
        document.documentElement.setAttribute('data-theme', this.theme)
      }

      this.$emit('update:visible', false)
    }
  }
}
</script>

<style scoped>
@import '@/styles/design-system.css';

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--bg-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal);
  padding: var(--spacing-lg);
}

.modal {
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-2xl);
  max-width: 700px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  padding: var(--spacing-xl);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.close-btn:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.modal-body {
  padding: var(--spacing-xl);
}

.modal-footer {
  padding: var(--spacing-xl);
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: var(--spacing-sm);
  justify-content: flex-end;
}

.settings-sections {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2xl);
}

.settings-section {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
}

.section-title i {
  color: var(--primary-color);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--border-light);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-label {
  flex: 1;
}

.setting-label span {
  display: block;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.setting-label small {
  color: var(--text-muted);
  font-size: var(--text-xs);
}

.setting-control {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* 主题选择器 */
.theme-options {
  display: flex;
  gap: var(--spacing-md);
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.theme-option:hover {
  background: var(--bg-tertiary);
}

.theme-option.active {
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary-color);
}

.theme-preview {
  width: 40px;
  height: 30px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 2px solid var(--border-color);
  transition: border-color var(--transition-fast);
}

.theme-option.active .theme-preview {
  border-color: var(--primary-color);
}

.preview-header {
  height: 8px;
  background: var(--primary-color);
}

.preview-content {
  height: 22px;
  background: var(--bg-secondary);
}

.theme-preview.dark .preview-header {
  background: var(--primary-dark);
}

.theme-preview.dark .preview-content {
  background: var(--gray-800);
}

/* 开关组件 */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--border-color);
  transition: var(--transition-fast);
  border-radius: var(--radius-full);
}

.switch-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: var(--transition-fast);
  border-radius: var(--radius-full);
}

input:checked + .switch-slider {
  background-color: var(--primary-color);
}

input:checked + .switch-slider:before {
  transform: translateX(26px);
}

.switch-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

/* 下拉选择器 */
.setting-control select {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.setting-control select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

/* 快捷键列表 */
.shortcuts-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.shortcut-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
}

.shortcut-desc {
  color: var(--text-primary);
  font-size: var(--text-sm);
}

.shortcut-key {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
  font-family: var(--font-mono);
  border: 1px solid var(--border-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal {
    margin: var(--spacing-md);
    max-height: calc(100vh - var(--spacing-xl));
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: var(--spacing-lg);
  }

  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }

  .setting-control {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>