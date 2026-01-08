import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router'

// 创建 axios 实例，并统一设置基础地址与超时时间。
const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = store.state.user.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果是二进制文件下载，直接返回响应
    if (response.config.responseType === 'blob') {
      return response
    }

    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      Message.error(res?.message || '请求失败')
      return Promise.reject(new Error(res?.message || '请求失败'))
    }
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      if (error.response.status === 401) {
        // 检查是否是用户上下文清理请求的401错误，避免循环调用
        if (error.config.url && error.config.url.includes('/users/current/context') && error.config.method === 'delete') {
          console.warn('用户上下文清理请求返回401，可能是正常的登出流程')
          return Promise.reject(error)
        }

        Message.error('未授权，请重新登录')
        store.dispatch('user/logout').then(() => {
          router.replace('/login')
        })
      } else if (error.response.status === 403) {
        // 403错误处理 - 不立即退出，可能是权限不足
        const errorMessage = error.response.data?.message || '访问被拒绝'
        Message.error(`${errorMessage}，可能的原因：用户已被禁用或权限不足`)

        // 只有明确的认证失败才退出登录
        if (error.response.data?.code === 'AUTHENTICATION_FAILED') {
          store.dispatch('user/logout').then(() => {
            setTimeout(() => {
              router.replace('/login')
            }, 2000)
          })
        }
      } else {
        Message.error(error.response.data?.message || '请求失败')
      }
    } else {
      Message.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default service

