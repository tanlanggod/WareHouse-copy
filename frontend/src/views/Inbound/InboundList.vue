<template>
  <div class="page-container">
    <el-card>
      <div slot="header">
        <span>入库管理</span>
        <el-button style="float: right;" type="primary" @click="handleAdd">商品入库</el-button>
      </div>
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="入库日期">
            <el-date-picker
              v-model="dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd HH:mm:ss">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="inboundNo" label="入库单号" width="150"></el-table-column>
        <el-table-column prop="product.name" label="商品名称"></el-table-column>
        <el-table-column prop="quantity" label="入库数量" width="100"></el-table-column>
        <el-table-column prop="supplier.name" label="供应商" width="150"></el-table-column>
        <el-table-column prop="inboundDate" label="入库时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.inboundDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="operator.realName" label="操作员" width="100"></el-table-column>
        <el-table-column prop="approvalStatus" label="审批状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getApprovalStatusType(scope.row.approvalStatus)">
              {{ getApprovalStatusText(scope.row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approver.realName" label="审批人" width="100">
          <template slot-scope="scope">
            {{ scope.row.approver ? scope.row.approver.realName : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="canSubmitApproval(scope.row)"
              size="mini"
              type="success"
              @click="handleSubmitApproval(scope.row)">
              提交审批
            </el-button>
            <el-button
              v-if="canApprove(scope.row)"
              size="mini"
              type="primary"
              @click="handleApprove(scope.row)">
              审批
            </el-button>
            <el-button
              v-if="canReject(scope.row)"
              size="mini"
              type="danger"
              @click="handleReject(scope.row)">
              拒绝
            </el-button>
            <el-button
              v-if="canCancel(scope.row)"
              size="mini"
              type="warning"
              @click="handleCancel(scope.row)">
              取消
            </el-button>
            <el-button
              v-if="canDelete(scope.row)"
              size="mini"
              type="danger"
              @click="handleDelete(scope.row)">
              删除
            </el-button>
            <el-button
              v-if="canViewHistory(scope.row)"
              size="mini"
              type="info"
              @click="handleViewHistory(scope.row)">
              历史记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right;">
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
import { inboundApi } from '@/api'
import { mapState } from 'vuex'

export default {
  name: 'InboundList',
  data() {
    return {
      searchForm: {},
      dateRange: [],
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      }
    }
  },
  computed: {
    ...mapState('user', ['userInfo'])
  },
  mounted() {
    // 从URL参数恢复状态
    this.restoreFromQueryParams()
    this.loadData()
  },
  watch: {
    '$route'(to, from) {
      if (to.path === '/app/inbounds' && to.query) {
        this.restoreFromQueryParams()
        this.loadData()

        // 检查是否需要自动打开审批对话框
        if (to.query.approve && to.query.action === 'approve') {
          this.$nextTick(() => {
            const record = this.tableData.find(item => item.id === parseInt(to.query.approve))
            if (record && this.$refs.approvalDialog) {
              this.$refs.approvalDialog.show({
                businessData: record,
                action: 'approve'
              })
            }
          })
        }
      }
    }
  },
  methods: {
    restoreFromQueryParams() {
      const query = this.$route.query

      // 恢复分页参数
      if (query.page) {
        this.pagination.page = parseInt(query.page)
      }
      if (query.size) {
        this.pagination.size = parseInt(query.size)
      }

      // 恢复日期范围参数
      if (query.startDate && query.endDate) {
        this.dateRange = [query.startDate, query.endDate]
      } else if (query.startDate) {
        this.dateRange = [query.startDate, '']
      } else if (query.endDate) {
        this.dateRange = ['', query.endDate]
      }
    },
    loadData() {
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : undefined,
        endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : undefined
      }
      inboundApi.getInbounds(params).then(response => {
        this.tableData = response.data.records || []
        this.pagination.total = response.data.total || 0
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.dateRange = []
      this.handleSearch()
    },
    handleAdd() {
      this.$router.push('/app/inbounds/add')
    },
    handleDelete(row) {
      this.$confirm('确定要删除该入库单吗？删除后库存将恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        inboundApi.deleteInbound(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadData()
        }).catch(error => {
          console.error('删除入库单失败:', error)
          this.$message.error(error.message || '删除入库单失败，请检查网络连接或联系管理员')
        })
      }).catch(() => {
        // 用户点击取消按钮，不做任何操作
        // 这是正常的用户行为，不显示错误信息
      })
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    // 审批相关方法
    getApprovalStatusType(status) {
      if (!status) return ''
      const statusMap = {
        'DRAFT': '',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'CANCELLED': 'info'
      }
      return statusMap[status] || ''
    },
    getApprovalStatusText(status) {
      if (!status) return ''
      const statusMap = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已批准',
        'REJECTED': '已拒绝',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },
    // 权限检查方法
    canSubmitApproval(row) {
      return row.approvalStatus === 'DRAFT'
    },
    canApprove(row) {
      if (!this.userInfo) return false
      const userRole = this.userInfo.role
      return row.approvalStatus === 'PENDING' &&
             (userRole === 'ADMIN' || userRole === 'WAREHOUSE_KEEPER')
    },
    canReject(row) {
      if (!this.userInfo) return false
      const userRole = this.userInfo.role
      return row.approvalStatus === 'PENDING' &&
             (userRole === 'ADMIN' || userRole === 'WAREHOUSE_KEEPER')
    },
    canCancel(row) {
      if (!this.userInfo) return false
      return row.approvalStatus === 'PENDING' &&
             row.operator && row.operator.id === this.userInfo.id
    },
    canDelete(row) {
      return row.approvalStatus !== 'APPROVED'
    },
    canViewHistory(row) {
      return true // 历史记录谁都可以查看
    },
    // 审批操作方法
    handleSubmitApproval(row) {
      // 验证入库单ID
      if (!row || row.id === null || row.id === undefined) {
        this.$message.error('无效的入库单ID，无法提交审批')
        return
      }

      // 验证商品信息
      if (!row.product || !row.product.name) {
        this.$message.error('商品信息不完整，无法提交审批')
        return
      }

      this.$confirm('确定要提交审批吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const remark = `提交 ${row.product.name} 入库审批`
        inboundApi.submitForApproval(row.id, remark).then(response => {
          if (response.code === 200) {
            this.$message.success('提交成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '提交失败')
          }
        }).catch(error => {
          console.error('提交审批失败:', error)
          this.$message.error(error.message || '提交审批失败，请检查网络连接或联系管理员')
        })
      }).catch(() => {
        // 用户点击取消按钮，不做任何操作
        // 这是正常的用户行为，不显示错误信息
      })
    },
    handleApprove(row) {
      this.$prompt('请输入审批意见', '审批通过', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入审批意见（可选）'
      }).then(({ value }) => {
        // 修复：传递null而不是空字符串
        const remark = value ? value.trim() : null
        inboundApi.approveInbound(row.id, remark).then(response => {
          if (response.code === 200) {
            this.$message.success('审批通过')
            this.loadData()
          } else {
            this.$message.error(response.message || '审批失败')
          }
        }).catch(error => {
          console.error('审批通过失败:', error)
          this.$message.error(error.message || '审批通过失败，请检查网络连接或联系管理员')
        })
      }).catch(() => {
        // 用户点击取消按钮，不做任何操作
        // 这是正常的用户行为，不显示错误信息
      })
    },
    handleReject(row) {
      this.$prompt('请输入拒绝原因', '审批拒绝', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (value) => {
          if (!value || !value.trim()) {
            return '请输入拒绝原因'
          }
          if (value.length > 200) {
            return '拒绝原因不能超过200个字符'
          }
          return true
        }
      }).then(({ value }) => {
        // 修复：传递trim后的字符串，避免空格问题
        const remark = value ? value.trim() : null
        inboundApi.rejectInbound(row.id, remark).then(response => {
          if (response.code === 200) {
            this.$message.success('审批拒绝')
            this.loadData()
          } else {
            this.$message.error(response.message || '拒绝失败')
          }
        }).catch(error => {
          console.error('拒绝审批失败:', error)
          this.$message.error(error.message || '拒绝审批失败，请检查网络连接或联系管理员')
        })
      }).catch(() => {
        // 用户点击取消按钮，不做任何操作
        // 这是正常的用户行为，不显示错误信息
      })
    },
    handleCancel(row) {
      this.$confirm('确定要取消审批吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const remark = `取消 ${row.product.name} 入库审批`
        inboundApi.cancelInboundApproval(row.id, remark).then(response => {
          if (response.code === 200) {
            this.$message.success('取消成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '取消失败')
          }
        }).catch(error => {
          console.error('取消审批失败:', error)
          this.$message.error(error.message || '取消审批失败，请检查网络连接或联系管理员')
        })
      }).catch(() => {
        // 用户点击取消按钮，不做任何操作
        // 这是正常的用户行为，不显示错误信息
      })
    },
    handleViewHistory(row) {
      // 保存当前页面状态到URL参数中
      const returnParams = new URLSearchParams({
        page: this.pagination.page,
        size: this.pagination.size,
        startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : '',
        endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : ''
      })
      this.$router.push(`/app/inbounds/${row.id}/approval-history?${returnParams.toString()}`)
    },

    // 显示审批对话框
    showApprovalDialog(row, action) {
      if (this.$refs.approvalDialog) {
        this.$refs.approvalDialog.show({
          businessData: row,
          action: action
        })
      }
    }
  }
}
</script>

