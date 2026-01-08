<template>
  <div class="page-container">
    <el-card>
      <div slot="header">
        <span>报表统计</span>
        <div style="float: right;">
          <el-button type="primary" size="small" @click="generateStockReport">库存报表PDF</el-button>
          <el-button type="success" size="small" @click="generateInboundReport">入库报表PDF</el-button>
          <el-button type="warning" size="small" @click="generateOutboundReport">出库报表PDF</el-button>
          <el-button type="danger" size="small" @click="generateLowStockReport">低库存报表PDF</el-button>
        </div>
      </div>
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd">
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <div slot="header">入库统计</div>
            <el-table :data="inboundStats" style="width: 100%">
              <el-table-column prop="productName" label="商品名称"></el-table-column>
              <el-table-column prop="totalQuantity" label="入库总量"></el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">出库统计</div>
            <el-table :data="outboundStats" style="width: 100%">
              <el-table-column prop="productName" label="商品名称"></el-table-column>
              <el-table-column prop="totalQuantity" label="出库总量"></el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { inboundApi, outboundApi, reportApi } from '@/api'

export default {
  name: 'ReportList',
  data() {
    return {
      searchForm: {},
      dateRange: [],
      inboundStats: [],
      outboundStats: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      const params = {
        page: 1,
        size: 1000,
        startDate: this.dateRange && this.dateRange[0] ? this.dateRange[0] + ' 00:00:00' : undefined,
        endDate: this.dateRange && this.dateRange[1] ? this.dateRange[1] + ' 23:59:59' : undefined
      }
      inboundApi.getInbounds(params).then(response => {
        const inbounds = response.data.records || []
        const stats = {}
        inbounds.forEach(item => {
          const key = item.product.name
          if (!stats[key]) {
            stats[key] = { productName: key, totalQuantity: 0 }
          }
          stats[key].totalQuantity += item.quantity
        })
        this.inboundStats = Object.values(stats)
      })
      outboundApi.getOutbounds(params).then(response => {
        const outbounds = response.data.records || []
        const stats = {}
        outbounds.forEach(item => {
          const key = item.product.name
          if (!stats[key]) {
            stats[key] = { productName: key, totalQuantity: 0 }
          }
          stats[key].totalQuantity += item.quantity
        })
        this.outboundStats = Object.values(stats)
      })
    },
    handleSearch() {
      this.loadData()
    },
    downloadPdf(blob, filename) {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      link.click()
      window.URL.revokeObjectURL(url)
    },
    generateStockReport() {
      const loading = this.$loading({
        lock: true,
        text: '生成中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      reportApi.generateStockReportPdf().then(response => {
        const blob = new Blob([response.data], { type: 'application/pdf' })
        this.downloadPdf(blob, '库存报表_' + new Date().getTime() + '.pdf')
        loading.close()
        this.$message.success('生成成功')
      }).catch(() => {
        loading.close()
        this.$message.error('生成失败')
      })
    },
    generateInboundReport() {
      const params = {}
      if (this.dateRange && this.dateRange[0]) {
        params.startDate = this.dateRange[0] + ' 00:00:00'
        params.endDate = this.dateRange[1] + ' 23:59:59'
      }
      const loading = this.$loading({
        lock: true,
        text: '生成中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      reportApi.generateInboundReportPdf(params).then(response => {
        const blob = new Blob([response.data], { type: 'application/pdf' })
        this.downloadPdf(blob, '入库报表_' + new Date().getTime() + '.pdf')
        loading.close()
        this.$message.success('生成成功')
      }).catch(() => {
        loading.close()
        this.$message.error('生成失败')
      })
    },
    generateOutboundReport() {
      const params = {}
      if (this.dateRange && this.dateRange[0]) {
        params.startDate = this.dateRange[0] + ' 00:00:00'
        params.endDate = this.dateRange[1] + ' 23:59:59'
      }
      const loading = this.$loading({
        lock: true,
        text: '生成中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      reportApi.generateOutboundReportPdf(params).then(response => {
        const blob = new Blob([response.data], { type: 'application/pdf' })
        this.downloadPdf(blob, '出库报表_' + new Date().getTime() + '.pdf')
        loading.close()
        this.$message.success('生成成功')
      }).catch(() => {
        loading.close()
        this.$message.error('生成失败')
      })
    },
    generateLowStockReport() {
      const loading = this.$loading({
        lock: true,
        text: '生成中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      reportApi.generateLowStockReportPdf().then(response => {
        const blob = new Blob([response.data], { type: 'application/pdf' })
        this.downloadPdf(blob, '低库存报表_' + new Date().getTime() + '.pdf')
        loading.close()
        this.$message.success('生成成功')
      }).catch(() => {
        loading.close()
        this.$message.error('生成失败')
      })
    }
  }
}
</script>

