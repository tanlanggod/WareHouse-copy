/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {string|Date} date - 日期或日期字符串
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return '-'

  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期（不包含时间）
 * @param {string|Date} date - 日期或日期字符串
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  return formatDateTime(date, format)
}

/**
 * 格式化时间（不包含日期）
 * @param {string|Date} date - 日期或日期字符串
 * @param {string} format - 格式化模板，默认 'HH:mm:ss'
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(date, format = 'HH:mm:ss') {
  return formatDateTime(date, format)
}

/**
 * 获取相对时间描述
 * @param {string|Date} date - 日期或日期字符串
 * @returns {string} 相对时间描述（如：刚刚、5分钟前、2小时前等）
 */
export function getRelativeTime(date) {
  if (!date) return '-'

  const now = new Date()
  const target = new Date(date)
  const diff = now.getTime() - target.getTime()

  if (diff < 0) return '未来时间'

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  if (months < 12) return `${months}个月前`
  return `${years}年前`
}

/**
 * 检查日期是否为今天
 * @param {string|Date} date - 日期或日期字符串
 * @returns {boolean} 是否为今天
 */
export function isToday(date) {
  if (!date) return false

  const today = new Date()
  const target = new Date(date)

  return today.getFullYear() === target.getFullYear() &&
         today.getMonth() === target.getMonth() &&
         today.getDate() === target.getDate()
}

/**
 * 检查日期是否为昨天
 * @param {string|Date} date - 日期或日期字符串
 * @returns {boolean} 是否为昨天
 */
export function isYesterday(date) {
  if (!date) return false

  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)

  const target = new Date(date)

  return yesterday.getFullYear() === target.getFullYear() &&
         yesterday.getMonth() === target.getMonth() &&
         yesterday.getDate() === target.getDate()
}

/**
 * 获取日期范围的开始和结束时间
 * @param {string|Date} startDate - 开始日期
 * @param {string|Date} endDate - 结束日期
 * @returns {object} 包含开始和结束时间的对象
 */
export function getDateRange(startDate, endDate) {
  const start = startDate ? new Date(startDate) : new Date()
  const end = endDate ? new Date(endDate) : new Date()

  // 设置开始时间为 00:00:00
  start.setHours(0, 0, 0, 0)

  // 设置结束时间为 23:59:59
  end.setHours(23, 59, 59, 999)

  return {
    start: start.toISOString(),
    end: end.toISOString()
  }
}

/**
 * 添加天数到指定日期
 * @param {string|Date} date - 原始日期
 * @param {number} days - 要添加的天数
 * @returns {Date} 新的日期对象
 */
export function addDays(date, days) {
  const result = new Date(date)
  result.setDate(result.getDate() + days)
  return result
}

/**
 * 减少天数到指定日期
 * @param {string|Date} date - 原始日期
 * @param {number} days - 要减少的天数
 * @returns {Date} 新的日期对象
 */
export function subtractDays(date, days) {
  return addDays(date, -days)
}