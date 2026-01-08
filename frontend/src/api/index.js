// 统一封装的接口请求模块，各业务域以对象方式导出。
import request from '@/utils/request'


// 认证相关
export const authApi = {
  login(data) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  register(data) {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    })
  },

  // 微信登录
  wechatLogin(data) {
    return request({
      url: '/auth/wechat-login',
      method: 'post',
      data
    })
  },

  // 手机号登录
  phoneLogin(data) {
    return request({
      url: '/auth/phone-login',
      method: 'post',
      data
    })
  },

  // 发送手机验证码
  sendVerificationCode(data) {
    return request({
      url: '/auth/send-verification-code',
      method: 'post',
      data
    })
  },

  // 获取验证码
  getCaptcha() {
    return request({
      url: '/auth/captcha',
      method: 'get'
    })
  }
}

// 用户上下文相关
export const userContextApi = {
    // 设置当前用户上下文
    setUserContext(userId) {
        return request({
            url: '/users/current/context',
            method: 'post',
            data: {userId}
        })
    },

    // 清除当前用户上下文
    clearUserContext() {
        return request({
            url: '/users/current/context',
            method: 'delete'
        })
    }
}
// 用户相关
export const userApi = {
  // 获取当前用户信息
  getCurrentUser() {
    return request({
      url: '/users/current',
      method: 'get'
    })
  },

  // 更新当前用户信息
  updateCurrentUser(data) {
    return request({
      url: '/users/current',
      method: 'put',
      data
    })
  },

  // 上传头像
  uploadAvatar(formData) {
    return request({
      url: '/users/current/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 修改密码
  changePassword(data) {
    return request({
      url: '/users/current/password',
      method: 'put',
      data
    })
  }
}

// 商品相关
export const productApi = {
  getProducts(params) {
    return request({
      url: '/products',
      method: 'get',
      params
    })
  },
  getProductById(id) {
    return request({
      url: `/products/${id}`,
      method: 'get'
    })
  },
  createProduct(data) {
    return request({
      url: '/products',
      method: 'post',
      data
    })
  },
  updateProduct(id, data) {
    return request({
      url: `/products/${id}`,
      method: 'put',
      data
    })
  },
  deleteProduct(id) {
    return request({
      url: `/products/${id}`,
      method: 'delete'
    })
  },
  getLowStockProducts() {
    return request({
      url: '/products/low-stock',
      method: 'get'
    })
  },
  exportProducts() {
    return request({
      url: '/products/export',
      method: 'get',
      responseType: 'blob'
    })
  },
  importProducts(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/products/import',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 商品图片相关
  uploadProductImage(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: `/products/${id}/image`,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  deleteProductImage(id) {
    return request({
      url: `/products/${id}/image`,
      method: 'delete'
    })
  }
}

// 入库相关
export const inboundApi = {
  getInbounds(params) {
    return request({
      url: '/inbounds',
      method: 'get',
      params
    })
  },
  getInboundById(id) {
    return request({
      url: `/inbounds/${id}`,
      method: 'get'
    })
  },
  createInbound(data) {
    return request({
      url: '/inbounds',
      method: 'post',
      data
    })
  },
  deleteInbound(id) {
    return request({
      url: `/inbounds/${id}`,
      method: 'delete'
    })
  },
  exportInbounds(params) {
    return request({
      url: '/inbounds/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },
  submitForApproval(id, remark) {
    return request({
      url: `/inbounds/${id}/submit-approval`,
      method: 'post',
      data: remark,
      headers: {
        'Content-Type': 'text/plain'
      }
    })
  },
  approveInbound(id, approvalRemark) {
    return request({
      url: `/inbounds/${id}/approve`,
      method: 'post',
      data: approvalRemark,
      headers: {
        'Content-Type': 'text/plain'
      }
    })
  },
  rejectInbound(id, approvalRemark) {
    return request({
      url: `/inbounds/${id}/reject`,
      method: 'post',
      data: approvalRemark,
      headers: {
        'Content-Type': 'text/plain'
      }
    })
  },
  cancelInboundApproval(id, remark) {
    return request({
      url: `/inbounds/${id}/cancel-approval`,
      method: 'post',
      data: remark,
      headers: {
        'Content-Type': 'text/plain'
      }
    })
  }
}

// 出库相关
export const outboundApi = {
  getOutbounds(params) {
    return request({
      url: '/outbounds',
      method: 'get',
      params
    })
  },
  getOutboundById(id) {
    return request({
      url: `/outbounds/${id}`,
      method: 'get'
    })
  },
  createOutbound(data) {
    return request({
      url: '/outbounds',
      method: 'post',
      data
    })
  },
  deleteOutbound(id) {
    return request({
      url: `/outbounds/${id}`,
      method: 'delete'
    })
  },
  exportOutbounds(params) {
    return request({
      url: '/outbounds/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}

// 类别相关
export const categoryApi = {
  getAllCategories() {
    return request({
      url: '/categories',
      method: 'get'
    })
  },
  createCategory(data) {
    return request({
      url: '/categories',
      method: 'post',
      data
    })
  },
  updateCategory(id, data) {
    return request({
      url: `/categories/${id}`,
      method: 'put',
      data
    })
  },
  deleteCategory(id) {
    return request({
      url: `/categories/${id}`,
      method: 'delete'
    })
  }
}

// 仓库相关
export const warehouseApi = {
  // 获取所有仓库
  getAllWarehouses() {
    return request({
      url: '/warehouses',
      method: 'get'
    })
  },

  // 分页查询仓库
  getWarehousesPage(params) {
    return request({
      url: '/warehouses/page',
      method: 'get',
      params
    })
  },

  // 根据ID获取仓库
  getWarehouseById(id) {
    return request({
      url: `/warehouses/${id}`,
      method: 'get'
    })
  },

  // 创建仓库
  createWarehouse(data) {
    return request({
      url: '/warehouses',
      method: 'post',
      data
    })
  },

  // 更新仓库
  updateWarehouse(id, data) {
    return request({
      url: `/warehouses/${id}`,
      method: 'put',
      data
    })
  },

  // 删除仓库
  deleteWarehouse(id) {
    return request({
      url: `/warehouses/${id}`,
      method: 'delete'
    })
  },

  // 切换仓库状态
  toggleWarehouseStatus(id) {
    return request({
      url: `/warehouses/${id}/toggle-status`,
      method: 'put'
    })
  },

  // 获取仓库统计信息
  getWarehouseStatistics() {
    return request({
      url: '/warehouses/statistics',
      method: 'get'
    })
  }
}

// 供应商相关
export const supplierApi = {
  // 获取所有供应商
  getAllSuppliers() {
    return request({
      url: '/suppliers',
      method: 'get'
    })
  },

  // 分页查询供应商
  getSuppliersPage(params) {
    return request({
      url: '/suppliers/page',
      method: 'get',
      params
    })
  },

  // 根据ID获取供应商
  getSupplierById(id) {
    return request({
      url: `/suppliers/${id}`,
      method: 'get'
    })
  },

  createSupplier(data) {
    return request({
      url: '/suppliers',
      method: 'post',
      data
    })
  },

  updateSupplier(id, data) {
    return request({
      url: `/suppliers/${id}`,
      method: 'put',
      data
    })
  },

  deleteSupplier(id) {
    return request({
      url: `/suppliers/${id}`,
      method: 'delete'
    })
  },

  // 切换供应商状态
  toggleSupplierStatus(id) {
    return request({
      url: `/suppliers/${id}/toggle-status`,
      method: 'put'
    })
  },

  // 获取供应商统计信息
  getSupplierStatistics() {
    return request({
      url: '/suppliers/statistics',
      method: 'get'
    })
  }
}

// 客户相关
export const customerApi = {
  getAllCustomers() {
    return request({
      url: '/customers',
      method: 'get'
    })
  },
  createCustomer(data) {
    return request({
      url: '/customers',
      method: 'post',
      data
    })
  },
  updateCustomer(id, data) {
    return request({
      url: `/customers/${id}`,
      method: 'put',
      data
    })
  },
  deleteCustomer(id) {
    return request({
      url: `/customers/${id}`,
      method: 'delete'
    })
  }
}

// 库存调整相关
export const stockAdjustmentApi = {
  getAdjustments(params) {
    return request({
      url: '/stock-adjustments',
      method: 'get',
      params
    })
  },
  createAdjustment(data) {
    return request({
      url: '/stock-adjustments',
      method: 'post',
      data
    })
  }
}

// 报表相关
export const reportApi = {
  generateStockReportPdf() {
    return request({
      url: '/reports/stock/pdf',
      method: 'get',
      responseType: 'blob'
    })
  },
  generateInboundReportPdf(params) {
    return request({
      url: '/reports/inbound/pdf',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },
  generateOutboundReportPdf(params) {
    return request({
      url: '/reports/outbound/pdf',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },
  generateLowStockReportPdf() {
    return request({
      url: '/reports/low-stock/pdf',
      method: 'get',
      responseType: 'blob'
    })
  }
}

// 审批相关
export const approvalApi = {
  // 获取待审批列表
  getPendingApprovals(params) {
    return request({
      url: '/approvals/pending',
      method: 'get',
      params
    })
  },

  // 获取用户待审批数量
  getPendingApprovalCount() {
    return request({
      url: '/approvals/pending-count',
      method: 'get'
    })
  },

  // 获取审批记录
  getApprovalRecords(params) {
    return request({
      url: '/approvals/records',
      method: 'get',
      params
    })
  },

  // 获取审批历史
  getApprovalHistory(businessType, businessId) {
    return request({
      url: '/approvals/history',
      method: 'get',
      params: {
        businessType,
        businessId
      }
    })
  },

  // 获取用户提交的审批记录
  getUserSubmissions(params) {
    return request({
      url: '/approvals/user-submissions',
      method: 'get',
      params
    })
  }
}

// 操作日志相关
export const operationLogApi = {
  getOperationLogs(params) {
    return request({
      url: '/api/operation-logs',
      method: 'get',
      params
    })
  },
  getOperationLogById(id) {
    return request({
      url: `/api/operation-logs/${id}`,
      method: 'get'
    })
  },
  getOperationLogByRequestId(requestId) {
    return request({
      url: `/api/operation-logs/request/${requestId}`,
      method: 'get'
    })
  },
  getOperationStatistics(params) {
    return request({
      url: '/api/operation-logs/statistics',
      method: 'get',
      params
    })
  },
  getSlowOperations(params) {
    return request({
      url: '/api/operation-logs/slow-operations',
      method: 'get',
      params
    })
  },
  getErrorOperations(params) {
    return request({
      url: '/api/operation-logs/errors',
      method: 'get',
      params
    })
  },
  getRecentOperations(params) {
    return request({
      url: '/api/operation-logs/recent',
      method: 'get',
      params
    })
  },
  getOperationLogsByUserId(userId, params) {
    return request({
      url: `/api/operation-logs/user/${userId}`,
      method: 'get',
      params
    })
  },
  cleanupOldLogs(params) {
    return request({
      url: '/api/operation-logs/cleanup',
      method: 'delete',
      params
    })
  },
  getOperationTypes() {
    return request({
      url: '/api/operation-logs/operation-types',
      method: 'get'
    })
  },
  getResourceTypes() {
    return request({
      url: '/api/operation-logs/resource-types',
      method: 'get'
    })
  },
  getModules() {
    return request({
      url: '/api/operation-logs/modules',
      method: 'get'
    })
  }
}

// 管理员用户管理相关
export const userAdminApi = {
  // 获取用户列表
  getUsers(params) {
    return request({
      url: '/users',
      method: 'get',
      params
    })
  },

  // 创建用户
  createUser(data) {
    return request({
      url: '/users',
      method: 'post',
      data
    })
  },

  // 更新用户
  updateUser(id, data) {
    return request({
      url: `/users/${id}`,
      method: 'put',
      data
    })
  },

  // 重置密码
  resetPassword(id, data) {
    return request({
      url: `/users/${id}/reset-password`,
      method: 'put',
      data
    })
  },

  // 切换用户状态
  toggleUserStatus(id) {
    return request({
      url: `/users/${id}/status`,
      method: 'put'
    })
  },

  // 删除用户
  deleteUser(id) {
    return request({
      url: `/users/${id}`,
      method: 'delete'
    })
  }
}

// 仓库位置相关
export const warehouseLocationApi = {
  // 获取仓库位置列表
  getWarehouseLocations(params) {
    return request({
      url: '/warehouse-locations',
      method: 'get',
      params
    })
  },

  // 根据仓库ID获取位置列表
  getLocationsByWarehouse(warehouseId) {
    return request({
      url: `/warehouse-locations/warehouse/${warehouseId}`,
      method: 'get'
    })
  },

  // 根据ID获取仓库位置
  getWarehouseLocationById(id) {
    return request({
      url: `/warehouse-locations/${id}`,
      method: 'get'
    })
  },

  // 创建仓库位置
  createWarehouseLocation(data) {
    return request({
      url: '/warehouse-locations',
      method: 'post',
      data
    })
  },

  // 更新仓库位置
  updateWarehouseLocation(id, data) {
    return request({
      url: `/warehouse-locations/${id}`,
      method: 'put',
      data
    })
  },

  // 删除仓库位置
  deleteWarehouseLocation(id) {
    return request({
      url: `/warehouse-locations/${id}`,
      method: 'delete'
    })
  },

  // 切换仓库位置状态
  toggleStatus(id) {
    return request({
      url: `/warehouse-locations/${id}/toggle-status`,
      method: 'put'
    })
  },

  // 批量创建仓库位置
  batchCreateLocations(warehouseId, locations) {
    return request({
      url: `/warehouse-locations/batch/${warehouseId}`,
      method: 'post',
      data: locations
    })
  },

  // 获取仓库位置统计信息
  getStatistics(warehouseId) {
    return request({
      url: `/warehouse-locations/statistics/${warehouseId}`,
      method: 'get'
    })
  }
}

// 系统公告相关
export const announcementApi = {
  // 获取用户可见的公告列表
  getVisibleAnnouncements() {
    return request({
      url: '/announcements/visible',
      method: 'get'
    })
  },

  // 获取置顶公告
  getPinnedAnnouncements() {
    return request({
      url: '/announcements/pinned',
      method: 'get'
    })
  },

  // 分页获取公告列表（管理员功能）
  getAnnouncements(params) {
    return request({
      url: '/announcements',
      method: 'get',
      params
    })
  },

  // 根据ID获取公告详情
  getAnnouncementById(id) {
    return request({
      url: `/announcements/${id}`,
      method: 'get'
    })
  },

  // 创建公告草稿
  createDraft(data) {
    return request({
      url: '/announcements/draft',
      method: 'post',
      data
    })
  },

  // 直接发布公告
  publishAnnouncement(data) {
    return request({
      url: '/announcements',
      method: 'post',
      data
    })
  },

  // 更新公告
  updateAnnouncement(id, data) {
    return request({
      url: `/announcements/${id}`,
      method: 'put',
      data
    })
  },

  // 发布草稿公告
  publishDraft(id) {
    return request({
      url: `/announcements/${id}/publish`,
      method: 'put'
    })
  },

  // 取消公告
  cancelAnnouncement(id) {
    return request({
      url: `/announcements/${id}/cancel`,
      method: 'put'
    })
  },

  // 删除公告
  deleteAnnouncement(id) {
    return request({
      url: `/announcements/${id}`,
      method: 'delete'
    })
  },

  // 获取公告统计信息
  getAnnouncementStatistics() {
    return request({
      url: '/announcements/statistics',
      method: 'get'
    })
  },

  // 搜索公告
  searchAnnouncements(keyword) {
    return request({
      url: '/announcements/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 获取热门公告
  getPopularAnnouncements(limit = 10) {
    return request({
      url: '/announcements/popular',
      method: 'get',
      params: { limit }
    })
  },

  // 批量操作公告状态
  batchUpdateStatus(status, ids) {
    // 构建正确的查询参数
    const params = new URLSearchParams()
    params.append('status', status)

    // 为每个ID添加参数，Spring Boot期望的格式：ids[]=3&ids[]=1
    ids.forEach(id => {
      params.append('ids', id)
    })

    return request({
      url: '/announcements/batch/status',
      method: 'put',
      params: params
    })
  },

  // 批量删除公告
  batchDeleteAnnouncements(ids) {
    // 构建正确的查询参数
    const params = new URLSearchParams()

    // 为每个ID添加参数，Spring Boot期望的格式：ids[]=3&ids[]=1
    ids.forEach(id => {
      params.append('ids', id)
    })

    return request({
      url: '/announcements/batch',
      method: 'delete',
      params: params
    })
  },

  // 获取公告类型列表
  getAnnouncementTypes() {
    return request({
      url: '/announcements/types',
      method: 'get'
    })
  },

  // 获取公告状态列表
  getAnnouncementStatuses() {
    return request({
      url: '/announcements/statuses',
      method: 'get'
    })
  },

  // 获取优先级列表
  getAnnouncementPriorities() {
    return request({
      url: '/announcements/priorities',
      method: 'get'
    })
  },

  // 获取目标受众列表
  getTargetAudiences() {
    return request({
      url: '/announcements/audiences',
      method: 'get'
    })
  },

  // 获取显示类型列表
  getDisplayTypes() {
    return request({
      url: '/announcements/display-types',
      method: 'get'
    })
  },

  // 获取用户角色列表
  getUserRoles() {
    return request({
      url: '/announcements/roles',
      method: 'get'
    })
  }
}

// 为了向后兼容，导出单独的函数
export const getOperationLogs = operationLogApi.getOperationLogs
export const getOperationLogById = operationLogApi.getOperationLogById
export const getOperationStatistics = operationLogApi.getOperationStatistics
export const getOperationTypes = operationLogApi.getOperationTypes
export const getResourceTypes = operationLogApi.getResourceTypes
export const getModules = operationLogApi.getModules
