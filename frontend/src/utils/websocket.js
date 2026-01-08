/**
 * WebSocket客户端工具类
 */
class WebSocketClient {
  constructor(url, onMessage, onError, onClose) {
    this.url = url
    this.onMessage = onMessage
    this.onError = onError
    this.onClose = onClose
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000
    this.heartbeatInterval = 30000
    this.heartbeatTimer = null
    this.ws = null
    this.isConnected = false

    this.connect()
  }

  connect() {
    try {
      // 创建WebSocket连接
      this.ws = new WebSocket(this.url)

      this.ws.onopen = () => {
        console.log('WebSocket连接成功:', this.url)
        this.isConnected = true
        this.reconnectAttempts = 0
        this.startHeartbeat()
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log('收到WebSocket消息:', data)

          if (this.onMessage) {
            this.onMessage(data)
          }
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
          if (this.onError) {
            this.onError(error)
          }
        }
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
        this.isConnected = false
        this.stopHeartbeat()

        if (this.onError) {
          this.onError(error)
        }

        // 尝试重连
        this.attemptReconnect()
      }

      this.ws.onclose = (event) => {
        console.log('WebSocket连接关闭:', event.code, event.reason)
        this.isConnected = false
        this.stopHeartbeat()

        if (this.onClose) {
          this.onClose(event)
        }

        // 如果不是主动关闭，尝试重连
        if (event.code !== 1000) {
          this.attemptReconnect()
        }
      }
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      if (this.onError) {
        this.onError(error)
      }
    }
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)

      setTimeout(() => {
        this.connect()
      }, this.reconnectInterval * this.reconnectAttempts)
    } else {
      console.log('重连次数已达上限，停止重连')
    }
  }

  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        try {
          this.ws.send(JSON.stringify({ type: 'heartbeat', timestamp: Date.now() }))
        } catch (error) {
          console.error('发送心跳包失败:', error)
        }
      }
    }, this.heartbeatInterval)
  }

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      try {
        const message = typeof data === 'string' ? data : JSON.stringify(data)
        this.ws.send(message)
        console.log('发送WebSocket消息:', message)
      } catch (error) {
        console.error('发送WebSocket消息失败:', error)
      }
    } else {
      console.warn('WebSocket未连接，无法发送消息')
    }
  }

  close() {
    if (this.ws) {
      this.isConnected = false
      this.stopHeartbeat()
      this.ws.close(1000, '主动关闭')
      this.ws = null
    }
  }

  getStatus() {
    return this.isConnected ? 'connected' : 'disconnected'
  }
}

/**
 * 审批通知WebSocket服务
 */
class ApprovalWebSocketService {
  constructor() {
    this.wsClient = null
    this.userId = null
    this.messageHandlers = new Map()
    this.pendingCount = 0

    this.setupMessageHandlers()
  }

  // 初始化WebSocket连接
  init(userId) {
    if (this.wsClient) {
      this.close()
    }

    this.userId = userId
    const token = localStorage.getItem('token')
    if (!token) {
      console.error('未找到认证token，无法建立WebSocket连接')
      return
    }

    const wsUrl = `${process.env.VUE_APP_WS_BASE_URL || 'ws://localhost:8080'}/ws/approval?userId=${userId}&token=${token}`

    this.wsClient = new WebSocketClient(
      wsUrl,
      this.handleMessage.bind(this),
      this.handleError.bind(this),
      this.handleClose.bind(this)
    )
  }

  // 设置消息处理器
  setupMessageHandlers() {
    this.messageHandlers.set('approval_submission', this.handleApprovalSubmission.bind(this))
    this.messageHandlers.set('approval_result', this.handleApprovalResult.bind(this))
    this.messageHandlers.set('pending_count_change', this.handlePendingCountChange.bind(this))
    this.messageHandlers.set('heartbeat', this.handleHeartbeat.bind(this))
  }

  // 处理审批提交通知
  handleApprovalSubmission(data) {
    console.log('收到审批提交通知:', data)
    this.$notify({
      title: '新的审批申请',
      message: `${data.businessType} - ${data.businessId}`,
      type: 'info',
      duration: 5000
    })
  }

  // 处理审批结果通知
  handleApprovalResult(data) {
    console.log('收到审批结果通知:', data)
    const type = data.approvalStatus === 'APPROVED' ? 'success' : 'warning'
    const title = data.approvalStatus === 'APPROVED' ? '审批通过' : '审批拒绝'

    this.$notify({
      title: title,
      message: `${data.businessType} - ${data.businessId} ${title}`,
      type: type,
      duration: 5000
    })
  }

  // 处理待审批数量变化
  handlePendingCountChange(data) {
    console.log('收到待审批数量变化:', data)
    this.pendingCount = data.pendingCount || 0

    // 可以通过事件总线通知其他组件
    this.$eventBus.$emit('pending-count-change', this.pendingCount)
  }

  // 处理心跳响应
  handleHeartbeat(data) {
    // 心跳响应处理
  }

  // 处理WebSocket错误
  handleError(error) {
    console.error('WebSocket错误:', error)
    this.$notify({
      title: '连接错误',
      message: 'WebSocket连接出现问题，请检查网络连接',
      type: 'error',
      duration: 0
    })
  }

  // 处理连接关闭
  handleClose(event) {
    console.log('WebSocket连接关闭:', event)

    if (event.code !== 1000) {
      this.$notify({
        title: '连接断开',
        message: 'WebSocket连接已断开，正在尝试重连...',
        type: 'warning',
        duration: 0
      })
    }
  }

  // 发送消息
  send(type, data) {
    if (this.wsClient) {
      this.wsClient.send({
        type,
        ...data,
        timestamp: Date.now()
      })
    }
  }

  // 关闭连接
  close() {
    if (this.wsClient) {
      this.wsClient.close()
      this.wsClient = null
    }
  }

  // 获取连接状态
  getStatus() {
    return this.wsClient ? this.wsClient.getStatus() : 'disconnected'
  }

  // 获取待审批数量
  getPendingCount() {
    return this.pendingCount
  }
}

export {
  WebSocketClient,
  ApprovalWebSocketService
}