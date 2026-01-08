<template>
  <div class="page-container">
    <el-card>
      <div slot="header">库存管理</div>
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="商品编号">
            <el-input v-model="searchForm.code" placeholder="请输入商品编号" clearable></el-input>
          </el-form-item>
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="类别">
            <el-select v-model="searchForm.categoryId" placeholder="请选择类别" clearable style="width: 180px">
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="库存范围">
            <el-input-number v-model="searchForm.minStockQty" :min="0"   placeholder="最小" style="width: 100px" controls-position="right"></el-input-number>
            <span style="margin: 0 8px">-</span>
            <el-input-number v-model="searchForm.maxStockQty" :min="0"   placeholder="最大" value="1000" style="width: 100px" controls-position="right"></el-input-number>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="code" label="商品编号" width="120"></el-table-column>
        <el-table-column prop="name" label="商品名称"></el-table-column>
        <el-table-column prop="category.name" label="类别" width="120"></el-table-column>
        <el-table-column prop="stockQty" label="当前库存" width="100">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.stockQty <= scope.row.minStock ? 'red' : '' }">
              {{ scope.row.stockQty }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="警戒线" width="100"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.stockQty <= scope.row.minStock" type="danger">库存不足</el-tag>
            <el-tag v-else type="success">正常</el-tag>
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
import { productApi, categoryApi } from '@/api'

export default {
  name: 'StockList',
  data() {
    return {
      searchForm: {
        code: '',
        name: '',
        categoryId: null,
        minStockQty: null,
        maxStockQty: 1000,
        status: null
      },
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      categories: [],
      statusOptions: [
        { label: '启用', value: 1 },
        { label: '停用', value: 0 }
      ]
    }
  },
  mounted() {
    this.loadCategories()
    this.loadData()
  },
  methods: {
    loadCategories() {
      categoryApi.getAllCategories().then(response => {
        this.categories = response.data || []
      })
    },
    loadData() {
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        code: this.searchForm.code || undefined,
        name: this.searchForm.name || undefined,
        categoryId: this.searchForm.categoryId || undefined,
        status: this.searchForm.status !== null ? this.searchForm.status : undefined,
        minStockQty: this.searchForm.minStockQty !== null ? this.searchForm.minStockQty : undefined,
        maxStockQty: this.searchForm.maxStockQty !== null ? this.searchForm.maxStockQty : undefined
      }
      productApi.getProducts(params).then(response => {
        this.tableData = response.data.records || []
        this.pagination.total = response.data.total || 0
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        code: '',
        name: '',
        categoryId: null,
        minStockQty: null,
        maxStockQty: 1000,
        status: null
      }
      this.handleSearch()
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

