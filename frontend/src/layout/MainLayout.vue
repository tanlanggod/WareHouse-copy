<template>
  <div class="modern-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- Logo区域 -->
      <div class="logo-section">
        <div class="logo">
          <div class="logo-icon">
            <i class="el-icon-box"></i>
          </div>
          <transition name="fade">
            <div v-show="!sidebarCollapsed" class="logo-text">
              <h1>智能仓储</h1>
              <span>管理系统</span>
            </div>
          </transition>
        </div>
        <button class="sidebar-toggle" @click="toggleSidebar">
          <i :class="sidebarCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
        </button>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <div class="menu-section">
          <div v-show="!sidebarCollapsed" class="section-title">主要功能</div>
          <router-link
            v-for="item in mainMenuItems"
            :key="item.path"
            :to="item.path"
            class="menu-item"
            :class="{ active: isActiveRoute(item.path) }">
            <div class="menu-icon">
              <i :class="item.icon"></i>
            </div>
            <transition name="fade">
              <span v-show="!sidebarCollapsed" class="menu-label">{{ item.label }}</span>
            </transition>
            <div v-if="item.badge && !sidebarCollapsed" class="menu-badge">{{ item.badge }}</div>
          </router-link>
        </div>

        <div v-if="hasManagementAccess" class="menu-section">
          <div v-show="!sidebarCollapsed" class="section-title">管理功能</div>
          <router-link
            v-for="item in managementMenuItems"
            :key="item.path"
            :to="item.path"
            class="menu-item"
            :class="{ active: isActiveRoute(item.path) }">
            <div class="menu-icon">
              <i :class="item.icon"></i>
            </div>
            <transition name="fade">
              <span v-show="!sidebarCollapsed" class="menu-label">{{ item.label }}</span>
            </transition>
          </router-link>
        </div>

        <div v-if="userInfo.role === 'ADMIN'" class="menu-section">
          <div v-show="!sidebarCollapsed" class="section-title">系统管理</div>
          <router-link
            v-for="item in adminMenuItems"
            :key="item.path"
            :to="item.path"
            class="menu-item"
            :class="{ active: isActiveRoute(item.path) }">
            <div class="menu-icon">
              <i :class="item.icon"></i>
            </div>
            <transition name="fade">
              <span v-show="!sidebarCollapsed" class="menu-label">{{ item.label }}</span>
            </transition>
          </router-link>
        </div>
      </nav>

      <!-- 侧边栏底部 -->
      <div class="sidebar-footer">
        <div class="footer-item" @click="showSettings = true">
          <div class="menu-icon">
            <i class="el-icon-setting"></i>
          </div>
          <transition name="fade">
            <span v-show="!sidebarCollapsed" class="menu-label">设置</span>
          </transition>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <header class="top-header">
        <div class="header-left">
          <!-- 面包屑导航 -->
          <div class="breadcrumb">
            <span class="breadcrumb-item" v-for="(item, index) in breadcrumbs" :key="index">
              <router-link v-if="item.path" :to="item.path">{{ item.label }}</router-link>
              <span v-else>{{ item.label }}</span>
              <i v-if="index < breadcrumbs.length - 1" class="el-icon-arrow-right"></i>
            </span>
          </div>
        </div>

        <div class="header-right">
          <!-- 全局搜索 -->
          <div class="global-search">
            <div class="search-box">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索商品、仓库、报表..."
                @focus="showSearchResults = true"
                @blur="hideSearchResults"
              >
              <div class="search-icon">
                <i class="el-icon-search"></i>
              </div>
            </div>
            <transition name="dropdown">
              <div v-show="showSearchResults && searchQuery" class="search-results">
                <div class="search-item" v-for="result in searchResults" :key="result.id">
                  <i :class="result.icon"></i>
                  <div class="search-content">
                    <div class="search-title">{{ result.title }}</div>
                    <div class="search-desc">{{ result.description }}</div>
                  </div>
                </div>
                <div v-if="searchResults.length === 0" class="search-empty">
                  <i class="el-icon-info"></i>
                  <span>没有找到相关结果</span>
                </div>
              </div>
            </transition>
          </div>

          <!-- 快捷操作按钮组 -->
          <div class="quick-actions">
            <el-tooltip content="快速入库" placement="bottom">
              <div class="action-btn" @click="$router.push('/app/inbounds/add')">
                <i class="el-icon-box"></i>
              </div>
            </el-tooltip>
            <el-tooltip content="快速出库" placement="bottom">
              <div class="action-btn" @click="$router.push('/app/outbounds/add')">
                <i class="el-icon-sold-out"></i>
              </div>
            </el-tooltip>
          </div>

          <!-- 公告通知组件 - 优化位置 -->
          <AnnouncementNotification />
          <!-- 审批通知组件 -->
          <ApprovalNotification />

          <!-- 主题切换 -->
          <div class="theme-toggle" @click="toggleTheme">
            <i :class="isDarkTheme ? 'el-icon-sunny' : 'el-icon-moon'"></i>
          </div>

          <!-- 用户菜单 -->
          <div class="user-menu" @click="showUserMenu = !showUserMenu">
            <div class="user-avatar">
              <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="用户头像">
              <div v-else class="avatar-placeholder">
                <i class="el-icon-user"></i>
              </div>
            </div>
            <div class="user-info">
              <div class="user-name">{{ userInfo.realName || userInfo.username }}</div>
              <div class="user-role">{{ getRoleDisplayName(userInfo.role) }}</div>
            </div>
            <i class="el-icon-arrow-down"></i>

            <transition name="dropdown">
              <div v-show="showUserMenu" class="user-dropdown">
                <div class="dropdown-item" @click="showProfileDialog">
                  <i class="el-icon-user"></i>
                  <span>个人资料</span>
                </div>
                <div class="dropdown-item" @click="showUserDialog = true">
                  <i class="el-icon-setting"></i>
                  <span>账户设置</span>
                </div>
                <div class="dropdown-divider"></div>
                <div class="dropdown-item logout" @click="handleLogout">
                  <i class="el-icon-switch-button"></i>
                  <span>退出登录</span>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </header>

      <!-- 内容区域 -->
      <main class="content-area">
        <transition name="page" mode="out-in">
          <router-view/>
        </transition>
      </main>
    </div>

    <!-- 用户资料对话框 -->
    <UserProfileDialog
      :visible.sync="showProfileDialog"
      :user-info="userInfo"
      @user-updated="onUserUpdated"
    />

    <!-- 用户设置对话框 -->
    <UserProfileDialog
      :visible.sync="showUserDialog"
      :user-info="userInfo"
      @user-updated="onUserUpdated"
    />

    <!-- 设置对话框 -->
    <SettingsDialog :visible.sync="showSettings" />
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import UserProfileDialog from '@/components/UserProfileDialog.vue'
import SettingsDialog from '@/components/SettingsDialog.vue'
import AnnouncementNotification from '@/components/Announcement/AnnouncementNotification.vue'
import ApprovalNotification from '@/components/Approval/ApprovalNotification.vue'

export default {
  name: 'ModernMainLayout',
  components: {
    UserProfileDialog,
    SettingsDialog,
    AnnouncementNotification,
    ApprovalNotification
  },
  data() {
    return {
      sidebarCollapsed: false,
      showUserMenu: false,
      showProfileDialog: false,
      showUserDialog: false,
      showSettings: false,
      showNotifications: false,
      showSearchResults: false,
      searchQuery: '',
      isDarkTheme: false,
      unreadCount: 3,

      mainMenuItems: [
        { path: '/app/dashboard', label: '仪表板', icon: 'el-icon-s-home' },
        { path: '/app/products', label: '商品管理', icon: 'el-icon-goods' },
        { path: '/app/inbounds', label: '入库管理', icon: 'el-icon-box' },
        { path: '/app/outbounds', label: '出库管理', icon: 'el-icon-sold-out' },
        { path: '/app/stock', label: '库存管理', icon: 'el-icon-edit-outline' },
        // { path: '/app/stock/adjustment', label: '库存调整', icon: 'el-icon-edit-outline' },
        { path: '/app/stock/adjustment/list', label: '调整记录', icon: 'el-icon-document' },
        { path: '/app/categories', label: '商品类别', icon: 'el-icon-menu' },
        { path: '/app/reports', label: '报表统计', icon: 'el-icon-data-analysis' }
      ],

      managementMenuItems: [
        { path: '/app/warehouses', label: '仓库管理', icon: 'el-icon-office-building' },
        { path: '/app/warehouse-locations', label: '位置管理', icon: 'el-icon-map-location' },
        { path: '/app/suppliers', label: '供应商管理', icon: 'el-icon-truck' },
        { path: '/app/customers', label: '客户管理', icon: 'el-icon-user-solid' }
      ],

      adminMenuItems: [
        { path: '/app/users', label: '用户管理', icon: 'el-icon-user' },
        { path: '/app/announcements', label: '公告管理', icon: 'el-icon-bell' },
        { path: '/app/operation-logs', label: '操作日志', icon: 'el-icon-document' }
      ]
    }
  },
  computed: {
    ...mapState('user', ['userInfo']),
    ...mapGetters('user', ['hasRole']),

    hasManagementAccess() {
      return this.hasRole(['ADMIN', 'WAREHOUSE_KEEPER'])
    },

    breadcrumbs() {
      const routes = this.$route.matched.filter(route => route.meta && route.meta.title)
      const breadcrumbs = routes.map(route => ({
        label: route.meta.title,
        path: route.path
      }))

      // 添加首页面包屑
      if (breadcrumbs.length > 0 && this.$route.path !== '/app/dashboard') {
        breadcrumbs.unshift({ label: '首页', path: '/app/dashboard' })
      }

      return breadcrumbs
    },

    searchResults() {
      if (!this.searchQuery) return []

      // 模拟搜索结果
      return [
        { id: 1, title: '商品A', description: '电子产品类', icon: 'el-icon-goods' },
        { id: 2, title: '仓库1', description: '主要存储区域', icon: 'el-icon-office-building' },
        { id: 3, title: '报表统计', description: '本月入库统计', icon: 'el-icon-data-analysis' }
      ].filter(item =>
        item.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        item.description.toLowerCase().includes(this.searchQuery.toLowerCase())
      )
    }
  },
  mounted() {
    this.loadThemePreference()
    this.initializeData()
  },
  methods: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    toggleTheme() {
      this.isDarkTheme = !this.isDarkTheme
      document.documentElement.setAttribute('data-theme', this.isDarkTheme ? 'dark' : 'light')
      localStorage.setItem('theme', this.isDarkTheme ? 'dark' : 'light')
    },

    loadThemePreference() {
      const savedTheme = localStorage.getItem('theme')
      if (savedTheme) {
        this.isDarkTheme = savedTheme === 'dark'
        document.documentElement.setAttribute('data-theme', savedTheme)
      }
    },

    isActiveRoute(path) {
      return this.$route.path === path || this.$route.path.startsWith(path + '/')
    },

    getRoleDisplayName(role) {
      const roleMap = {
        'ADMIN': '系统管理员',
        'WAREHOUSE_KEEPER': '仓库管理员',
        'EMPLOYEE': '普通员工'
      }
      return roleMap[role] || '未知角色'
    },

    hideSearchResults() {
      setTimeout(() => {
        this.showSearchResults = false
      }, 200)
    },

    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('user/logout').then(() => {
          this.$router.replace('/login')
          this.$message.success('已安全退出')
        })
      })
    },

    onUserUpdated(updatedUser) {
      this.$store.commit('user/SET_USER_INFO', updatedUser)
      this.$message.success('用户信息更新成功')
    },

    async initializeData() {
      try {
        // 加载通知数量
        // await this.$store.dispatch('notifications/fetchUnreadCount')
      } catch (error) {
        console.error('初始化数据失败:', error)
      }
    }
  },
  watch: {
    '$route'(to, from) {
      // 路由变化时关闭下拉菜单
      this.showUserMenu = false
      this.showSearchResults = false
    }
  }
}
</script>

<style scoped>
@import '@/styles/design-system.css';
@import '@/styles/components.css';

.modern-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-secondary);
  color: var(--text-primary);
}

/* 侧边栏样式 */
.sidebar {
  width: 260px;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal);
  position: relative;
  z-index: var(--z-fixed);
}

.sidebar.collapsed {
  width: 80px;
}

/* Logo区域 */
.logo-section {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  font-size: 1.2rem;
  flex-shrink: 0;
}

.logo-text h1 {
  font-size: var(--text-lg);
  font-weight: 700;
  margin: 0;
  line-height: var(--leading-tight);
}

.logo-text span {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.sidebar-toggle {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.sidebar-toggle:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md) 0;
}

.menu-section {
  margin-bottom: var(--spacing-xl);
}

.section-title {
  padding: 0 var(--spacing-lg);
  margin-bottom: var(--spacing-sm);
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
  position: relative;
  gap: var(--spacing-md);
}

.menu-item:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.menu-item.active {
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary-color);
}

.menu-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--primary-color);
}

.menu-icon {
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  flex-shrink: 0;
}

.menu-label {
  font-size: var(--text-sm);
  font-weight: 500;
}

.menu-badge {
  margin-left: auto;
  background: var(--danger-color);
  color: var(--text-inverse);
  font-size: var(--text-xs);
  padding: 2px 6px;
  border-radius: var(--radius-full);
  font-weight: 600;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: var(--spacing-md);
  border-top: 1px solid var(--border-color);
}

.footer-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-secondary);
}

.footer-item:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部导航栏 */
.top-header {
  height: 70px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  position: relative;
  z-index: var(--z-sticky);
}

.header-left {
  flex: 1;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--text-sm);
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--text-secondary);
}

.breadcrumb-item a {
  color: var(--text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.breadcrumb-item a:hover {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

/* 快捷操作按钮组 */
.quick-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-size: 18px;
}

.action-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: var(--text-inverse);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 全局搜索 */
.global-search {
  position: relative;
}

.search-box {
  position: relative;
  width: 300px;
}

.search-box input {
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md) var(--spacing-sm) 2.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  font-size: var(--text-sm);
  transition: all var(--transition-fast);
}

.search-box input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: var(--bg-primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.search-icon {
  position: absolute;
  left: var(--spacing-md);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  pointer-events: none;
}

.search-results {
  position: absolute;
  top: calc(100% + var(--spacing-sm));
  left: 0;
  right: 0;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  max-height: 300px;
  overflow-y: auto;
  z-index: var(--z-dropdown);
}

.search-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.search-item:hover {
  background: var(--bg-secondary);
}

.search-content {
  flex: 1;
}

.search-title {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.search-desc {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-top: 2px;
}

.search-empty {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  color: var(--text-muted);
  font-size: var(--text-sm);
}

/* 通知中心 */
.notification-center {
  position: relative;
  cursor: pointer;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.notification-icon:hover {
  background: var(--primary-color);
  color: var(--text-inverse);
}

.notification-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: var(--danger-color);
  color: var(--text-inverse);
  font-size: 10px;
  width: 18px;
  height: 18px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

/* 主题切换 */
.theme-toggle {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.theme-toggle:hover {
  background: var(--primary-color);
  color: var(--text-inverse);
}

/* 用户菜单 */
.user-menu {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
}

.user-menu:hover {
  background: var(--bg-tertiary);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  overflow: hidden;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: var(--primary-color);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  line-height: var(--leading-tight);
}

.user-role {
  font-size: var(--text-xs);
  color: var(--text-muted);
  line-height: var(--leading-tight);
}

.user-dropdown {
  position: absolute;
  top: calc(100% + var(--spacing-sm));
  right: 0;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  min-width: 180px;
  z-index: var(--z-dropdown);
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.dropdown-item:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.dropdown-item.logout {
  color: var(--danger-color);
}

.dropdown-item.logout:hover {
  background: rgba(239, 68, 68, 0.1);
}

.dropdown-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--spacing-xs) 0;
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
}

/* 动画效果 */
.fade-enter-active, .fade-leave-active {
  transition: opacity var(--transition-fast);
}

.fade-enter, .fade-leave-to {
  opacity: 0;
}

.dropdown-enter-active, .dropdown-leave-active {
  transition: all var(--transition-fast);
}

.dropdown-enter, .dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.page-enter-active, .page-leave-active {
  transition: all var(--transition-normal);
}

.page-enter {
  opacity: 0;
  transform: translateX(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: var(--z-modal);
    transform: translateX(-100%);
    transition: transform var(--transition-normal);
  }

  .sidebar.mobile-open {
    transform: translateX(0);
  }

  .sidebar.collapsed {
    width: 260px;
  }

  .search-box {
    width: 200px;
  }

  .user-info {
    display: none;
  }

  .breadcrumb {
    display: none;
  }
}

@media (max-width: 480px) {
  .top-header {
    padding: 0 var(--spacing-md);
  }

  .search-box {
    width: 150px;
  }

  .content-area {
    padding: var(--spacing-md);
  }
}
</style>