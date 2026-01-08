import { ApprovalWebSocketService } from '@/utils/websocket'
import Vue from 'vue'

/**
 * WebSocket插件 - 全局注入审批WebSocket服务
 */
export default {
  install(Vue) {
    // 创建WebSocket服务实例
    const approvalWebSocket = new ApprovalWebSocketService()

    // 注入到Vue原型
    Vue.prototype.$approvalWebSocket = approvalWebSocket

    // 添加全局事件总线
    Vue.prototype.$approvalEventBus = new Vue()

    // 监听待审批数量变化事件
    approvalWebSocket.on('pending-count-change', (data) => {
      Vue.prototype.$approvalEventBus.$emit('pending-count-change', data)
    })

    // 监听审批提交事件
    approvalWebSocket.on('approval-submission', (data) => {
      Vue.prototype.$approvalEventBus.$emit('approval-submission', data)
    })

    // 监听审批结果事件
    approvalWebSocket.on('approval-result', (data) => {
      Vue.prototype.$approvalEventBus.$emit('approval-result', data)
    })
  }
}