<template>
  <div class="page-container">
    <el-card>
      <div slot="header">
        <span>出库管理</span>
        <el-button style="float: right;" type="primary" @click="handleAdd">商品出库</el-button>
      </div>
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="出库日期">
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
        <el-table-column prop="outboundNo" label="出库单号" width="180"></el-table-column>
        <el-table-column prop="product.name" label="商品名称"></el-table-column>
        <el-table-column prop="quantity" label="出库数量" width="100"></el-table-column>
        <el-table-column prop="customer.name" label="客户" width="150"></el-table-column>
        <el-table-column prop="outboundDate" label="出库时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.outboundDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="operator.realName" label="操作员" width="100"></el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
import { outboundApi } from '@/api'

export default {
  name: 'OutboundList',
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
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] : undefined,
        endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] : undefined
      }
      outboundApi.getOutbounds(params).then(response => {
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
      this.$router.push('/app/outbounds/add')
    },
    handleDelete(row) {
      this.$confirm('确定要删除该出库单吗？删除后库存将恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        outboundApi.deleteOutbound(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadData()
        })
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
    }
  }
}
</script>

