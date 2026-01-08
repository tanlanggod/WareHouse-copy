import { authApi, userApi, userContextApi } from '@/api'

const state = {
  token: localStorage.getItem('token') || '',
  userInfo: (() => {
    try {
      const userInfo = localStorage.getItem('userInfo')
      return userInfo ? JSON.parse(userInfo) : null
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return null
    }
  })()
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    localStorage.setItem('token', token)

    // 登录成功后，UserContext 会被设置为当前用户ID，InboundService 中会获取到正确的操作员
    console.log('Token设置成功')
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  CLEAR_USER(state) {
    state.token = ''
    state.userInfo = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
}

const actions = {
  login({ commit, dispatch }, loginForm) {
    return new Promise((resolve, reject) => {
      authApi.login(loginForm).then(response => {
        const { token, userId, username, role, realName } = response.data
        commit('SET_TOKEN', token)
        commit('SET_USER_INFO', { userId, username, role, realName })

        // 设置用户上下文到后端
        if (userId) {
          userContextApi.setUserContext(userId).catch(error => {
            console.warn('设置用户上下文失败:', error)
          })
        }

        // 登录后获取完整的用户信息
        dispatch('fetchUserInfo')
        resolve(response)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // 设置登录状态（用于手机号登录和微信登录）
  setLoginState({ commit, dispatch }, loginData) {
    return new Promise((resolve, reject) => {
      const { token, userId, username, role, realName, avatar, loginType } = loginData
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', { userId, username, role, realName, avatar, loginType })

      // 设置用户上下文到后端
      if (userId) {
        userContextApi.setUserContext(userId).catch(error => {
          console.warn('设置用户上下文失败:', error)
        })
      }

      // 登录后获取完整的用户信息
      dispatch('fetchUserInfo')
      resolve()
    })
  },

  // 获取完整用户信息
  fetchUserInfo({ commit }) {
    return new Promise((resolve, reject) => {
      userApi.getCurrentUser().then(response => {
        if (response.code === 200) {
          commit('SET_USER_INFO', response.data)
        } else {
          // 如果返回的不是200，清除可能不完整的用户信息
          commit('CLEAR_USER')
        }
        resolve(response)
      }).catch(error => {
        console.error('获取用户信息失败:', error)
        // 获取用户信息失败时，尝试恢复用户状态而不是直接清除
        checkAndRestoreUserState()
        reject(error)
      })
    })
  },

  logout({ commit, state }) {
    return new Promise((resolve) => {
      // 清除后端用户上下文（使用当前的token）
      userContextApi.clearUserContext()
        .catch(error => {
          console.warn('清除用户上下文失败:', error)
        })
        .finally(() => {
          // 无论成功失败，都清除本地用户状态
          commit('CLEAR_USER')
          resolve()
        })
    })
  }
}

const getters = {
  userInfo: state => state.userInfo,
  token: state => state.token,
  hasRole: (state) => (roles) => {
    if (!state.userInfo || !state.userInfo.role) return false
    return roles.includes(state.userInfo.role)
  },
  isAuthenticated: state => !!state.token
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

