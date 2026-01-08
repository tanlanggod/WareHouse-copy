/**
 * 认证相关工具函数
 */

/**
 * 获取token
 */
export function getToken() {
  return localStorage.getItem('token')
}

/**
 * 设置token
 */
export function setToken(token) {
  localStorage.setItem('token', token)
}

/**
 * 移除token
 */
export function removeToken() {
  localStorage.removeItem('token')
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  try {
    const userInfo = localStorage.getItem('userInfo')
    return userInfo ? JSON.parse(userInfo) : null
  } catch (error) {
    console.error('解析用户信息失败:', error)
    return null
  }
}

/**
 * 设置用户信息
 */
export function setUserInfo(userInfo) {
  localStorage.setItem('userInfo', JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  localStorage.removeItem('userInfo')
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  removeToken()
  removeUserInfo()
}

/**
 * 检查是否已登录
 */
export function isAuthenticated() {
  return !!getToken()
}

/**
 * 获取当前用户角色
 */
export function getCurrentUserRole() {
  const userInfo = getUserInfo()
  return userInfo ? userInfo.role : null
}

/**
 * 检查并恢复用户状态
 */
export function checkAndRestoreUserState() {
  const token = getToken()
  const userInfo = getUserInfo()

  if (token && !userInfo) {
    // 有token但无用户信息，尝试重新获取
    console.log('检测到异常状态，正在重新获取用户信息...')
    return store.dispatch('user/fetchUserInfo').catch(error => {
      console.error('重新获取用户信息失败:', error)
      // 如果重新获取也失败，再清除状态
      store.dispatch('user/logout')
    })
  }
  return !!token
}