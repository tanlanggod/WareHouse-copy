import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/login',
    meta: { requiresAuth: false }
  },
  {
    path: '/app',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/Product/ProductList.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'products/add',
        name: 'ProductAdd',
        component: () => import('@/views/Product/ProductForm.vue'),
        meta: { title: '添加商品' }
      },
      {
        path: 'products/edit/:id',
        name: 'ProductEdit',
        component: () => import('@/views/Product/ProductForm.vue'),
        meta: { title: '编辑商品' }
      },
      {
        path: 'inbounds',
        name: 'Inbounds',
        component: () => import('@/views/Inbound/InboundList.vue'),
        meta: { title: '入库管理' }
      },
      {
        path: 'inbounds/add',
        name: 'InboundAdd',
        component: () => import('@/views/Inbound/InboundForm.vue'),
        meta: { title: '商品入库' }
      },
      {
        path: 'inbounds/:id/approval-history',
        name: 'InboundApprovalHistory',
        component: () => import('@/views/Inbound/ApprovalHistoryView.vue'),
        meta: { title: '入库审批历史' }
      },
      {
        path: 'outbounds',
        name: 'Outbounds',
        component: () => import('@/views/Outbound/OutboundList.vue'),
        meta: { title: '出库管理' }
      },
      {
        path: 'outbounds/add',
        name: 'OutboundAdd',
        component: () => import('@/views/Outbound/OutboundForm.vue'),
        meta: { title: '商品出库' }
      },
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('@/views/Stock/StockList.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: 'stock/adjustment',
        name: 'StockAdjustment',
        component: () => import('@/views/Stock/StockAdjustment.vue'),
        meta: { title: '库存调整' }
      },
      {
        path: 'stock/adjustment/list',
        name: 'StockAdjustmentList',
        component: () => import('@/views/Stock/StockAdjustmentList.vue'),
        meta: { title: '库存调整记录' }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/Category/CategoryList.vue'),
        meta: { title: '商品类别' }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/Report/ReportList.vue'),
        meta: { title: '报表统计' }
      },
      {
        path: 'operation-logs',
        name: 'OperationLogs',
        component: () => import('@/views/OperationLog/OperationLogList.vue'),
        meta: { title: '操作日志', roles: ['ADMIN'] } // 只有管理员可以访问
      },
      {
        path: 'warehouses',
        name: 'Warehouses',
        component: () => import('@/views/Warehouse/WarehouseList.vue'),
        meta: { title: '仓库管理', roles: ['ADMIN', 'WAREHOUSE_KEEPER'] } // 管理员和库管员可以访问
      },
      {
        path: 'warehouse-locations',
        name: 'WarehouseLocations',
        component: () => import('@/views/WarehouseLocation/WarehouseLocationList.vue'),
        meta: { title: '位置管理', roles: ['ADMIN', 'WAREHOUSE_KEEPER'] } // 管理员和库管员可以访问
      },
      {
        path: 'suppliers',
        name: 'Suppliers',
        component: () => import('@/views/Supplier/SupplierList.vue'),
        meta: { title: '供应商管理', roles: ['ADMIN', 'WAREHOUSE_KEEPER'] } // 管理员和库管员可以访问
      },
      {
        path: 'customers',
        name: 'Customers',
        component: () => import('@/views/Customer/CustomerList.vue'),
        meta: { title: '客户管理', roles: ['ADMIN', 'WAREHOUSE_KEEPER', 'EMPLOYEE'] } // 所有用户都可以访问
      },
      {
        path: 'customers/add',
        name: 'CustomerAdd',
        component: () => import('@/views/Customer/CustomerForm.vue'),
        meta: { title: '新增客户', roles: ['ADMIN', 'WAREHOUSE_KEEPER', 'EMPLOYEE'] } // 所有用户都可以访问
      },
      {
        path: 'customers/edit/:id',
        name: 'CustomerEdit',
        component: () => import('@/views/Customer/CustomerForm.vue'),
        meta: { title: '编辑客户', roles: ['ADMIN', 'WAREHOUSE_KEEPER', 'EMPLOYEE'] } // 所有用户都可以访问
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/User/UserList.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] } // 只有管理员可以访问
      },
      {
        path: 'announcements',
        name: 'AnnouncementList',
        component: () => import('@/views/Announcement/AnnouncementList.vue'),
        meta: { title: '公告管理', roles: ['ADMIN'] } // 只有管理员可以访问
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = store.state.user.token
  const userInfo = store.state.user.userInfo

  // 处理从受保护路由到登录页的重定向
  if (from.meta.requiresAuth && to.path === '/login' && !token) {
    next()
    return
  }

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    // 如果已登录用户访问登录页，重定向到首页
    next('/app/dashboard')
  } else if (to.meta.roles && to.meta.roles.length > 0) {
    // 检查角色权限
    const userRole = userInfo?.role
    if (!userRole || !to.meta.roles.includes(userRole)) {
      console.warn(`权限不足: 需要 ${to.meta.roles.join(', ')}，当前角色 ${userRole}`)
      // 权限不足，跳转到首页
      next('/app/dashboard')
    } else {
      next()
    }
  } else {
    next()
  }
})

// 处理重复导航错误
const originalPush = VueRouter.prototype.push
const originalReplace = VueRouter.prototype.replace

VueRouter.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalPush.call(this, location, onResolve, onReject)
  }
  return originalPush.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') {
      throw err
    }
  })
}

VueRouter.prototype.replace = function replace(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalReplace.call(this, location, onResolve, onReject)
  }
  return originalReplace.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') {
      throw err
    }
  })
}

export default router

